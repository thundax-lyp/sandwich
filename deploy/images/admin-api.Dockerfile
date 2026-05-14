ARG SANDWISH_JRE_IMAGE=eclipse-temurin:8-jre
FROM ${SANDWISH_JRE_IMAGE}

WORKDIR /app

ARG JAR_FILE=sandwish-admin-api/target/sandwish-admin-api.jar

COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
