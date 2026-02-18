FROM java:8
MAINTAINER niyang
VOLUME /tmp
COPY sdp-admin-1.0-SNAPSHOT.jar  admin.jar
COPY /lib  /lib
EXPOSE 31200
ENTRYPOINT   ["java", "-jar","-Dspring.profiles.active=dev","admin.jar"]

