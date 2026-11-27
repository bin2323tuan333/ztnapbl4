# Build và chạy ZTNA Server (REST 443, WS Connector 8443, WS Agent 9443)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ztna-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 443 8443 9443
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-jar", "app.jar"]
