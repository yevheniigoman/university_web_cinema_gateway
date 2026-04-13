FROM eclipse-temurin:25-jdk

WORKDIR /app

# Копіюємо jar (зірочка допоможе, якщо назва довга)
COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]