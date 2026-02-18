FROM java:8
MAINTAINER niyang
VOLUME /tmp
COPY sdp-compose-0.0.1-SNAPSHOT.jar  compose.jar
COPY /lib  /lib
EXPOSE 30462
ENTRYPOINT   ["java", "-jar","-Dspring.profiles.active=dev","compose.jar"]

