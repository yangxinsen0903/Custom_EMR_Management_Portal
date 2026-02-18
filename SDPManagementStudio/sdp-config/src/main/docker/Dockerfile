FROM java:8
MAINTAINER niyang
VOLUME /tmp
COPY sdp-config-1.0.jar  config.jar
COPY /lib  /lib
EXPOSE 25000
ENTRYPOINT   ["java", "-jar", "config.jar"]