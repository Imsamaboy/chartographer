FROM openjdk:11

ARG JAR_FILE=./target/chartographer-0.0.1.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

ENV PATH=./src/main/resources/static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
