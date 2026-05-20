-- 1. Tworzenie baz danych dla poszczególnych mikroserwisów
CREATE DATABASE IF NOT EXISTS notice_db;
CREATE DATABASE IF NOT EXISTS map_db;
CREATE DATABASE IF NOT EXISTS pet_db;
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS keycloak;

-- 2. Aktywacja rozszerzenia PostGIS dla bazy NoticeService
\c notice_db;
CREATE EXTENSION IF NOT EXISTS postgis;

-- 3. Aktywacja rozszerzenia PostGIS dla bazy MapService
\c map_db;
CREATE EXTENSION IF NOT EXISTS postgis;