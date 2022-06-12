FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir -p /app /tmp/html /tmp/image
COPY --from=build /home/gradle/src/build/libs/*.jar /app/robot-v0.jar
ENTRYPOINT ["java","-jar","/app/robot-v0.jar"]