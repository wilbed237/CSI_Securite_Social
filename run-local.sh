#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

TMUX_SESSION="csi-local"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

MODE="full"
BUILD=false
JAVA_TOOL_OPTIONS_DEFAULT="${JAVA_TOOL_OPTIONS:--Xms128m -Xmx384m -XX:+UseG1GC}"

JARS=(
  "discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar"
  "auth-service/target/auth-service-0.1.0-SNAPSHOT.jar"
  "profile-service/target/profile-service-0.1.0-SNAPSHOT.jar"
  "medical-service/target/medical-service-0.1.0-SNAPSHOT.jar"
  "reimbursement-service/target/reimbursement-service-0.1.0-SNAPSHOT.jar"
  "api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar"
)

LITE_JARS=(
  "discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar"
  "auth-service/target/auth-service-0.1.0-SNAPSHOT.jar"
  "profile-service/target/profile-service-0.1.0-SNAPSHOT.jar"
  "api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar"
)

function usage() {
  cat <<EOF
Usage: ./run-local.sh [--lite|--full] [--build]

Options:
  --lite      Lance un backend minimal: discovery + auth + profile + gateway.
  --full      Lance toute la stack backend (valeur par defaut).
  --build     Compile d'abord le backend (mvn clean package -DskipTests).
  -h, --help  Affiche cette aide.

Notes:
  - Sans --build, le script reutilise les JAR existants pour demarrer plus vite.
  - Memoire JVM par defaut: $JAVA_TOOL_OPTIONS_DEFAULT
EOF
}

function parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --lite)
        MODE="lite"
        shift
        ;;
      --full)
        MODE="full"
        shift
        ;;
      --build)
        BUILD=true
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Argument non reconnu: $1"
        usage
        exit 1
        ;;
    esac
  done
}

function build_project() {
  echo "Compilation du backend avec Maven..."
  if [[ ! -x "$ROOT/mvnw" ]]; then
    echo "Erreur: mvnw introuvable ou non executable."
    exit 1
  fi

  "$ROOT/mvnw" clean package -DskipTests
}

function check_jars() {
  local missing=0
  local -a required_jars=()

  if [[ "$MODE" == "lite" ]]; then
    required_jars=("${LITE_JARS[@]}")
  else
    required_jars=("${JARS[@]}")
  fi

  for jar in "${required_jars[@]}"; do
    if [[ ! -f "$ROOT/$jar" ]]; then
      echo "Erreur: jar introuvable: $jar"
      missing=1
    fi
  done

  if [[ $missing -eq 1 ]]; then
    echo
    echo "Compilation incomplete : les JAR attendus n'ont pas ete generes."
    echo "Relance avec --build ou execute './mvnw clean package -DskipTests'."
    exit 1
  fi
}

function start_tmux() {
  echo "Demarrage en session tmux '$TMUX_SESSION' (mode: $MODE)..."
  if tmux has-session -t "$TMUX_SESSION" >/dev/null 2>&1; then
    echo "Session tmux existante '$TMUX_SESSION' trouvee. Elle sera reutilisee."
  else
    tmux new-session -d -s "$TMUX_SESSION" -n discovery \
      "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar"

    tmux new-window -t "$TMUX_SESSION" -n auth -c "$ROOT" \
      "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_auth SPRING_DATASOURCE_USERNAME=csi_auth SPRING_DATASOURCE_PASSWORD=csi_auth PROFILE_SERVICE_URL=http://localhost:8082 INTERNAL_SERVICE_SECRET=dev-internal-secret JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/auth-service/target/auth-service-0.1.0-SNAPSHOT.jar"

    tmux new-window -t "$TMUX_SESSION" -n profile -c "$ROOT" \
      "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_profile SPRING_DATASOURCE_USERNAME=csi_profile SPRING_DATASOURCE_PASSWORD=csi_profile INTERNAL_SERVICE_SECRET=dev-internal-secret JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/profile-service/target/profile-service-0.1.0-SNAPSHOT.jar"

    if [[ "$MODE" == "full" ]]; then
      tmux new-window -t "$TMUX_SESSION" -n medical -c "$ROOT" \
        "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_medical SPRING_DATASOURCE_USERNAME=csi_medical SPRING_DATASOURCE_PASSWORD=csi_medical PROFILE_SERVICE_URL=http://localhost:8082 REIMBURSEMENT_SERVICE_URL=http://localhost:8084 JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/medical-service/target/medical-service-0.1.0-SNAPSHOT.jar"

      tmux new-window -t "$TMUX_SESSION" -n reimbursement -c "$ROOT" \
        "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_reimbursement SPRING_DATASOURCE_USERNAME=csi_reimbursement SPRING_DATASOURCE_PASSWORD=csi_reimbursement MEDICAL_SERVICE_URL=http://localhost:8083 JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/reimbursement-service/target/reimbursement-service-0.1.0-SNAPSHOT.jar"
    fi

    tmux new-window -t "$TMUX_SESSION" -n gateway -c "$ROOT" \
      "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar"

    tmux new-window -t "$TMUX_SESSION" -n frontend -c "$ROOT/frontend" \
      "if [[ ! -d node_modules ]]; then npm install; fi && npm run dev"
  fi

  echo "Session tmux prete. Rejoins-la avec :"
  echo "  tmux attach -t $TMUX_SESSION"
  echo "Ou liste les fenetres avec :"
  echo "  tmux list-windows -t $TMUX_SESSION"
}

function start_background() {
  echo "tmux introuvable, demarrage en arriere-plan (mode: $MODE), logs: $LOG_DIR"

  nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/discovery.log" 2>&1 &
  echo "$!" > "$LOG_DIR/discovery.pid"

  nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_auth SPRING_DATASOURCE_USERNAME=csi_auth SPRING_DATASOURCE_PASSWORD=csi_auth PROFILE_SERVICE_URL=http://localhost:8082 INTERNAL_SERVICE_SECRET=dev-internal-secret JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/auth-service/target/auth-service-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/auth.log" 2>&1 &
  echo "$!" > "$LOG_DIR/auth.pid"

  nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_profile SPRING_DATASOURCE_USERNAME=csi_profile SPRING_DATASOURCE_PASSWORD=csi_profile INTERNAL_SERVICE_SECRET=dev-internal-secret JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/profile-service/target/profile-service-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/profile.log" 2>&1 &
  echo "$!" > "$LOG_DIR/profile.pid"

  if [[ "$MODE" == "full" ]]; then
    nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_medical SPRING_DATASOURCE_USERNAME=csi_medical SPRING_DATASOURCE_PASSWORD=csi_medical PROFILE_SERVICE_URL=http://localhost:8082 REIMBURSEMENT_SERVICE_URL=http://localhost:8084 JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/medical-service/target/medical-service-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/medical.log" 2>&1 &
    echo "$!" > "$LOG_DIR/medical.pid"

    nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/csi_reimbursement SPRING_DATASOURCE_USERNAME=csi_reimbursement SPRING_DATASOURCE_PASSWORD=csi_reimbursement MEDICAL_SERVICE_URL=http://localhost:8083 JWT_SECRET=change-me-change-me-change-me-change-me-256-bit-secret EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/reimbursement-service/target/reimbursement-service-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/reimbursement.log" 2>&1 &
    echo "$!" > "$LOG_DIR/reimbursement.pid"
  fi

  nohup sh -c "JAVA_TOOL_OPTIONS='$JAVA_TOOL_OPTIONS_DEFAULT' EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/ java -jar $ROOT/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar" > "$LOG_DIR/gateway.log" 2>&1 &
  echo "$!" > "$LOG_DIR/gateway.pid"

  (cd "$ROOT/frontend" && if [[ ! -d node_modules ]]; then npm install; fi && nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 &)
  echo "$!" > "$LOG_DIR/frontend.pid"

  echo "Services demarres en arriere-plan. Logs :"
  ls -1 "$LOG_DIR"/*.log
  echo "Pour arreter les services, utilise ./stop-local.sh."
}

parse_args "$@"

if [[ "$BUILD" == "true" ]]; then
  build_project
else
  echo "Build ignore (utilise --build pour recompiler)."
fi

check_jars

if command -v tmux >/dev/null 2>&1; then
  start_tmux
else
  start_background
fi
