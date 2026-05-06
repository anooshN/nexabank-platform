FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
RUN wget -q https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz && \
    tar xzf apache-maven-3.9.6-bin.tar.gz && \
    mv apache-maven-3.9.6 /opt/maven && \
    rm apache-maven-3.9.6-bin.tar.gz
ENV PATH="/opt/maven/bin:${PATH}"
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S nexabank && adduser -S nexabank -G nexabank
COPY --from=builder /app/target/*.jar app.jar
RUN chown nexabank:nexabank app.jar
USER nexabank
EXPOSE 8083
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]