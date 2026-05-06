FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S nexabank && adduser -S nexabank -G nexabank
COPY --from=builder /app/target/*.jar app.jar
RUN chown nexabank:nexabank app.jar
USER nexabank
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-XX:+UseG1GC","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 CMD wget -q -O- http://localhost:8080/actuator/health || exit 1
