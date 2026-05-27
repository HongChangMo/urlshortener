FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
COPY common/build.gradle.kts common/
COPY common/src/ common/src/
COPY shortener/build.gradle.kts shortener/
COPY shortener/src/ shortener/src/
RUN chmod +x gradlew && ./gradlew :shortener:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/shortener/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
