# Handoff: Lead Architect → DevOps / QA

**Objetivo:** Preparar infraestructura automatizada base y entorno de pruebas para la Prueba de Concepto (PoC).

1. **Qué se completó:**
   - He encargado al Backend (Claude Sonnet) implementar el API `POST /expedientes` en Spring Boot 3 con JPA.
   - He encargado al Frontend (Claude Sonnet) implementar el consumo en Vue 3.
   - Existe un archivo base `docker-compose.yml` en la raíz del proyecto para la BD MySQL 8.
2. **Contrato a cumplir:**
   - Crear un script Bash robusto (`setup_local.sh`) en la raíz del proyecto que arranque el `docker-compose`, espere inteligentemente hasta que MySQL responda en el puerto 3306 (usando ping o nc), y prepare el escenario para correr las pruebas.
   - Proveer un cascarón inicial de configuración de una suite de Testcontainers en Java (ej. `AbstractIntegrationTest.java` o similar) o una guía de integración limpia para el agente Backend.
3. **Cómo probarlo:**
   - Ejecutar `./setup_local.sh`, debe levantar los servicios y la terminal debe mostrar confirmación en texto verde ("Infraestructura Lista").
4. **Bloqueantes detectados:**
   - Backend está apenas inicializando el esqueleto de Spring Boot, por tanto, las pruebas de integración deben aislar la conectividad a la BD inicialmente.
   - **NUEVO (Simulación de APIs Externas):** Debido al alcance de V1, el Backend intentará consultar un CRM y MS Graph. Debes aprovisionar un contenedor simulador (Ej. `Wiremock`) en el `docker-compose.yml` local para que el Backend tenga a quién hacer HTTP GET/POST en las pruebas, previniendo fallos por falta de conectividad real corporativa.

_Lead Architect (Gemini 3.1 Pro) – 2026-02-25_
