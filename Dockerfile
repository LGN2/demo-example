FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:17-jre
RUN groupadd --system bayt && useradd --system --gid bayt bayt && mkdir /data && chown bayt:bayt /data
WORKDIR /app
COPY --from=build /build/target/property-management-1.0.0-SNAPSHOT.jar app.jar
USER bayt
ENV STORAGE_PATH=/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
