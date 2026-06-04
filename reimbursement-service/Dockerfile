# Compile le microservice dans Docker pour ne pas dependre du JDK installe sur la machine hote.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Maven lit tous les modules declares par le parent, meme lorsque -pl cible un seul service.
# On copie donc tous les POM afin que le reactor soit valide, tout en gardant le cache efficace.
COPY pom.xml ./pom.xml
COPY common/pom.xml ./common/pom.xml
COPY discovery-service/pom.xml ./discovery-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY profile-service/pom.xml ./profile-service/pom.xml
COPY medical-service/pom.xml ./medical-service/pom.xml
COPY reimbursement-service/pom.xml ./reimbursement-service/pom.xml
RUN mvn -pl reimbursement-service -am dependency:go-offline -DskipTests

# Seuls le module common et le service cible sont necessaires pour produire ce JAR.
COPY common ./common
COPY reimbursement-service ./reimbursement-service
RUN mvn -pl reimbursement-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/reimbursement-service/target/reimbursement-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=10s --timeout=3s --start-period=35s --retries=12 CMD curl -fsS http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
