FROM --platform=linux/amd64 maven:3.6-jdk-8-alpine
ENV MAVEN_ARGS="-q -Dmaven.test.skip=true -Dmaven.javadoc.skip=true "
COPY ["." ,"/data"]
RUN  mvn ${MAVEN_ARGS}  package -f /data/pom.xml

FROM --platform=linux/amd64 openjdk:8-alpine3.9
EXPOSE 25000
RUN apk add curl
ENV MODULE_NAME=sdp-config
ENV VERSION=1.0
ENV JVM_OPT=""
ENV DB_URL=jdbc:mysql://localhost/sdp
ENV DB_USERNAME=root
ENV DB_PASSWORD=abcd
ENV DB_KEY=a
ENV REGSERVER_URL=http://rs
COPY --from=0 "/data/sdp-config/target/${MODULE_NAME}-${VERSION}.jar" "/app/app.jar"
COPY --from=0  "/data/${MODULE_NAME}/target/lib/" "/app/lib/"
ENTRYPOINT   ["java", "-jar","/app/app.jar"]