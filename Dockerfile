# ETAP 1: Budowanie
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
# Kopiujemy cały projekt (wraz z głównym pom.xml)
COPY . .
# Przekazujemy nazwę modułu, który chcemy zbudować (np. map-service)
ARG MODULE_NAME
# Kompilujemy tylko wybrany moduł i to, od czego zależy (-am)
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# ETAP 2: Uruchamianie
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG MODULE_NAME

# Instalujemy narzędzie 'nc' (potrzebne dla wait-for) oraz 'dos2unix'
RUN apt-get update && apt-get install -y netcat-openbsd dos2unix && rm -rf /var/lib/apt/lists/*

# Kopiujemy zbudowany w pierwszym etapie plik .jar oraz skrypt startowy
COPY --from=builder /build/${MODULE_NAME}/target/*.jar app.jar
COPY entrypoint.sh .

# Naprawiamy znaki końca linii (z Windows CRLF na Linux LF) i nadajemy uprawnienia
RUN dos2unix entrypoint.sh && chmod +x entrypoint.sh

# Uruchamiamy aplikację przez nasz skrypt
ENTRYPOINT ["./entrypoint.sh"]