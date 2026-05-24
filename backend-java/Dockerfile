FROM mcr.microsoft.com/devcontainers/java:8 AS build
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM mcr.microsoft.com/devcontainers/java:8
WORKDIR /app
COPY --from=build /app/target/chem-guardian-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
