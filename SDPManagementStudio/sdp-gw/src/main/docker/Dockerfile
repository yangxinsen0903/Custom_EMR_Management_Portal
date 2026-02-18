FROM java:8
MAINTAINER niyang
VOLUME /tmp
COPY igs-gw-1.0-SNAPSHOT.jar  gw.jar
COPY /lib  /lib
EXPOSE 31000
ENTRYPOINT   ["java", "-jar","-Dsrping.profiles.active=${profile}", "gw.jar"]

