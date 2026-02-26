-- Privilegios y Schemas Init
CREATE DATABASE IF NOT EXISTS `ibpms_core`;
GRANT ALL PRIVILEGES ON `ibpms_core`.* TO 'ibpms_user'@'%';

-- Se asegura la codificación para soportar caracteres internacionales (UTF-8)
ALTER DATABASE `ibpms_core` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

FLUSH PRIVILEGES;

-- Nota: Las tablas físicas (DDL) de negocio (`ibpms_case`, `sys_catalog`, etc)
-- se generarán dinámicamente o por Liquibase desde el código fuente de Spring Boot
-- al arrancar. Este script solo asegura el tenant/squema maestro.
