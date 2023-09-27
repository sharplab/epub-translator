# syntax=docker/dockerfile:1
FROM gradle:jdk11 AS builder

WORKDIR /app

COPY . .
RUN ./gradlew quarkusBuild -Dquarkus.package.type=uber-jar


FROM adoptopenjdk/openjdk11

RUN useradd -m user
RUN mkdir -p /app/config
WORKDIR /app

COPY --from=builder --chown=user:user /app/build/epub-translator-runner.jar /app/epub-translator-runner.jar

USER user

ENTRYPOINT [ "java", "-jar", "/app/epub-translator-runner.jar" ]
