-- 1. Tworzenie baz danych dla poszczególnych mikroserwisów
CREATE DATABASE notice_db;
CREATE DATABASE map_db;
CREATE DATABASE pet_db;
CREATE DATABASE user_db;
CREATE DATABASE notification_db;

-- 2. Aktywacja rozszerzenia PostGIS dla bazy NoticeService
\c notice_db;
CREATE EXTENSION IF NOT EXISTS postgis;

-- 3. Aktywacja rozszerzenia PostGIS dla bazy MapService
\c map_db;
CREATE EXTENSION IF NOT EXISTS postgis;