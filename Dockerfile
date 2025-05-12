FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Add environment variable configuration
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","/app.jar"]