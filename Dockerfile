FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

RUN groupadd --system identity \
    && useradd --system \
       --gid identity \
       --no-create-home \
       identity

COPY --from=build \
    /workspace/target/identity-service-0.0.1-SNAPSHOT.jar \
    app.jar

RUN chown identity:identity /app/app.jar

USER identity

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]