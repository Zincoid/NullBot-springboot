# nullbot-jre-chrome:21 由 Dockerfile.base 构建
FROM nullbot-jre-chrome:21
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]