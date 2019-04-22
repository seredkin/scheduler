FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
COPY build/libs/rest-scheduler-0.1.jar /rest-scheduler.jar
COPY data/*.csv /data/
EXPOSE 8081
CMD java  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar rest-scheduler.jar debug