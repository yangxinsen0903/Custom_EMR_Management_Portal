# FROM --platform=linux/amd64 maven:3.8.5-openjdk-17-slim
FROM --platform=linux/arm64/v8 maven:3.8.5-openjdk-17-slim
ENV MAVEN_ARGS="-q -Dmaven.test.skip=true -Dmaven.javadoc.skip=true"
COPY ["." ,"/data"]
RUN  mvn ${MAVEN_ARGS} package -f /data/pom.xml

FROM --platform=linux/amd64 openjdk:17-alpine
EXPOSE 8082
COPY --from=0 "/data/ansible-api/target/ansible-api-0.0.1-SNAPSHOT.jar" "/app/ansible-api-0.0.1-SNAPSHOT.jar"
COPY --from=0 "/data/ansible-api/target/lib/" "/app/lib/"
ENTRYPOINT java -jar /app/ansible-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=aks
