FROM openjdk:8
ADD build/libs/campsite-0.0.1-SNAPSHOT.jar campsite.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "campsite.jar"]