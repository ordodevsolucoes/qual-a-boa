# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
# baixa as dependencias antes de copiar o codigo-fonte para reaproveitar essa camada entre builds
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre AS runtime
RUN useradd --system --create-home --home-dir /app --shell /usr/sbin/nologin appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER appuser

# Cloud Run define PORT em tempo de execucao; o padrao so vale para rodar a imagem fora dele
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
