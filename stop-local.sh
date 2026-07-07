#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

if [[ -d "$ROOT/logs" ]]; then
  for pidfile in "$ROOT"/logs/*.pid; do
    [[ -f "$pidfile" ]] || continue
    pid=$(<"$pidfile")
    if [[ -n "$pid" ]] && ps -p "$pid" >/dev/null 2>&1; then
      echo "Arrêt du pid $pid ($pidfile)"
      kill "$pid"
      sleep 1
      if ps -p "$pid" >/dev/null 2>&1; then
        echo "Le pid $pid ne s'est pas arrêté, kill -9"
        kill -9 "$pid" >/dev/null 2>&1 || true
      fi
    else
      echo "PID non trouvé ou déjà arrêté pour $pidfile"
    fi
    rm -f "$pidfile"
  done
fi

for target in discovery-service auth-service profile-service medical-service reimbursement-service api-gateway frontend/node_modules/.bin/vite; do
  pkill -f "$ROOT/$target" >/dev/null 2>&1 || true
done

echo "Vérifie les ports avec :"
echo "  ss -ltnp | grep -E '8761|8080|8081|8082|8083|8084|5173'"