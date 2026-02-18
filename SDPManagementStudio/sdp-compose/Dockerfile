FROM --platform=linux/amd64 maven:3.6-jdk-8-alpine
ENV MAVEN_ARGS="-q -Dmaven.test.skip=true -Dmaven.javadoc.skip=true "
COPY ["." ,"/data"]
RUN  mvn ${MAVEN_ARGS}  package -f /data/pom.xml



FROM --platform=linux/amd64 openjdk:8-alpine3.9
EXPOSE 30462
##RUN apk add tini
RUN apk add curl
ENV MODULE_NAME=sdp-compose
ENV VERSION=1.0
ENV JVM_OPT=""
ENV REGSERVER_URL=http://rs
ENV APP_MEM_RATIO=0.6
COPY --from=0 "/data/sdp-compose/target/${MODULE_NAME}-${VERSION}.jar" "/app/app.jar"
COPY --from=0 "/data/${MODULE_NAME}/target/lib/" "/app/lib/"
COPY --from=0 "/data/${MODULE_NAME}/target/start-app.sh" "/app/start-app.sh"
COPY --from=0 "/data/${MODULE_NAME}/target/arthas-boot.jar" "/app/arthas-boot.jar"
RUN chmod +x /app/start-app.sh
CMD ["sh","/app/start-app.sh"]