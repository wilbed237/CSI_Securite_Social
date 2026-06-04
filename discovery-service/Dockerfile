# Compile le microservice dans Docker pour ne pas dependre du JDK installe sur la machine hote.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copie d'abord les POM pour mieux reutiliser le cache des dependances Maven.
COPY pom.xml ./pom.xml
COPY common/pom.xml ./common/pom.xml
COPY discovery-service/pom.xml ./discovery-service/pom.xml
RUN mvn -pl discovery-service -am dependency:go-offline -DskipTests

# Copie le code source puis genere le JAR executable du service cible.
COPY common ./common
COPY discovery-service ./discovery-service
RUN mvn -pl discovery-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
