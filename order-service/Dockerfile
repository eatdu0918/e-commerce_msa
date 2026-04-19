FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY build/libs/application.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
