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
RUN mvn -pl api-gateway -am dependency:go-offline -DskipTests

# Seuls le module common et le service cible sont necessaires pour produire ce JAR.
COPY common ./common
COPY api-gateway ./api-gateway
RUN mvn -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
