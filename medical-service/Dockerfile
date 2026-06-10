# Compile le service dans Docker pour que docker compose up --build soit autonome.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY common/pom.xml ./common/pom.xml
COPY discovery-service/pom.xml ./discovery-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY profile-service/pom.xml ./profile-service/pom.xml
COPY medical-service/pom.xml ./medical-service/pom.xml
COPY reimbursement-service/pom.xml ./reimbursement-service/pom.xml
COPY common ./common
COPY medical-service ./medical-service
RUN --mount=type=cache,id=csi-maven,target=/root/.m2 \
    mvn -pl medical-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/medical-service/target/medical-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=10s --timeout=3s --start-period=35s --retries=12 CMD curl -fsS http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
