FROM openjdk:21
COPY build/libs/SmartMatch-0.0.1-SNAPSHOT.jar /smartmatch.jar
EXPOSE 8080
CMD ["java", "-jar", "/smartmatch.jar"]