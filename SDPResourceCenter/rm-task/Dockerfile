# FROM --platform=linux/amd64 maven:3.8.5-openjdk-17-slim
FROM --platform=linux/arm64/v8 maven:3.8.5-openjdk-17-slim
ENV MAVEN_ARGS="-q -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"
COPY ["." ,"/data"]
RUN  mvn ${MAVEN_ARGS} package -f /data/pom.xml

FROM --platform=linux/amd64 openjdk:17-alpine
EXPOSE 8081
COPY --from=0 "/data/rm-task/target/rm-task-0.0.1-SNAPSHOT.jar" "/app/rm-task-0.0.1-SNAPSHOT.jar"
COPY --from=0 "/data/rm-task/target/lib/" "/app/lib/"
ENTRYPOINT java -jar /app/rm-task-0.0.1-SNAPSHOT.jar --spring.profiles.active=aks
