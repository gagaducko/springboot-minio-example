FROM openjdk:17

LABEL authors="gagaduck"

COPY ./skywalking-agent /usr/local/server/skywalking

COPY ./target/minio-service-0.0.1-SNAPSHOT.jar /app.jar

#ENTRYPOINT ["java", "-jar", "/app.jar"]

ENTRYPOINT ["java", "-javaagent:/usr/local/server/skywalking/skywalking-agent.jar", "-Dskywalking.agent.service_name=minio-service", "-Dskywalking.collector.backend_service=server:11800", "-jar", "/app.jar"]