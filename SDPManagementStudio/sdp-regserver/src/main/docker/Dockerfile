FROM java:8
MAINTAINER niyang
VOLUME /tmp
COPY sdp-regserver-1.0.jar  regserver.jar
COPY /lib  /lib
EXPOSE 30000
ENTRYPOINT   ["java", "-jar", "regserver.jar"]