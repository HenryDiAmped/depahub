# Etapa 1: Compilación (Build)
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo de configuración de Maven para descargar dependencias primero
COPY pom.xml .

# Descargar las dependencias en caché (optimiza futuras compilaciones)
RUN mvn dependency:go-offline -B

# Copiar el código fuente del proyecto
COPY src ./src

# Compilar y empaquetar la aplicación saltando los tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo .jar generado en la etapa anterior usando los datos de tu artifactId y versión
COPY --from=build /app/target/depahub-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto configurado en tu Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]