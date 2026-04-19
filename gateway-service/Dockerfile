# Alpine(musl) 대신 Jammy: 일부 환경에서 JVM이 느린 client/emulated 경로를 덜 타는 편
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY build/libs/application.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
