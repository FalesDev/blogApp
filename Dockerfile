# --- Etapa de construcción (generar el .jar) ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa final ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Configuración flexible
ENV PORT=8080
EXPOSE $PORT

# Sin perfil hardcodeado - se define por variable de entorno
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar /app/app.jar"]