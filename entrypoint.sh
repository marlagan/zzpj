#!/bin/bash
set -e

echo "Uruchamianie serwisu..."

# 1. Czekanie na bazę PostgreSQL (jeśli wymagana przez dany serwis)
if [ -n "$WAIT_FOR_DB" ]; then
  echo "Oczekiwanie na PostgreSQL ($WAIT_FOR_DB:5432)..."
  while ! nc -z $WAIT_FOR_DB 5432; do
    sleep 2
  done
  echo "PostgreSQL działa!"
fi

# 2. Czekanie na Config Server
if [ -n "$WAIT_FOR_CONFIG" ]; then
  echo "Oczekiwanie na Config Server ($WAIT_FOR_CONFIG:8888)..."
  while ! nc -z $WAIT_FOR_CONFIG 8888; do
    sleep 2
  done
  echo "Config Server jest gotowy!"
fi

# 3. Czekanie na Eurekę
if [ -n "$WAIT_FOR_EUREKA" ]; then
  echo "Oczekiwanie na Eurekę ($WAIT_FOR_EUREKA:8761)..."
  while ! nc -z $WAIT_FOR_EUREKA 8761; do
    sleep 2
  done
  echo "Eureka Server jest gotowa!"
fi

echo "Wszystkie zależności gotowe. Start aplikacji Spring Boot!"
exec java -jar app.jar