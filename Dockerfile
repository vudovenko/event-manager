FROM openjdk:21
COPY ./target/event-manager-0.0.1-SNAPSHOT.jar /app/start.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/start.jar"]