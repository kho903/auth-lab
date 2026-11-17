
# Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew --no-daemon bootJar


# Runtime
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /workspace/build/libs/app.jar ./app.jar

EXPOSE 18080

ENTRYPOINT ["java", "-jar", "app.jar"]
