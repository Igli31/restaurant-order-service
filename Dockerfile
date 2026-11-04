# Build and test using the project's Maven Wrapper.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sh ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
RUN sh ./mvnw --batch-mode --no-transfer-progress verify

# Run the JAR without including the compiler or Maven.
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring spring \
    && mkdir -p /app/data \
    && chown -R spring:spring /app

COPY --from=build /build/target/*.jar app.jar

USER spring:spring
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
