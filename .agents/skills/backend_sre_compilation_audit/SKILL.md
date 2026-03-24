---
name: backend_sre_compilation_audit
description: Skill obligatoria para el Agente Backend. Exige auto-compilación con Maven y auditoría de arranque del puerto 8080 antes de entregar cualquier tarea o realizar un handoff.
triggers:
  - "Cuando el agente Backend termine de escribir código Java y se prepare para hacer el stash o reportar completitud."
  - "Al crear o modificar clases @Entity o integraciones de red en Spring Boot."
---

# MANDATO DE AUTO-COMPILACIÓN Y DISCIPLINA SRE (BACKEND)

🛑 **REGLA DE SUPERVIVENCIA CERO-CONFIANZA (ZERO-TRUST COMPILATION)**

A partir de este momento, TIENES ESTRICTAMENTE PROHIBIDO asumir que tu código Java funciona solo por haberlo escrito. Tu flujo de trabajo (Workflow) cambia obligatoriamente a validación de compilación en caliente:

## 1. PROHIBIDO EL HANDOFF CIEGO
Antes de enviar cualquier estado a QA, al Arquitecto, o notificar que has terminado, **DEBES** ejecutar la compilación y arranque mediante la topología de contenedores agnóstica de la plataforma. En la raíz del proyecto, ejecuta:
```bash
docker compose up -d ibpms-core
```
Esto reconstruirá y ejecutará la aplicación (Hot-Reload) usando el contenedor Maven dedicado sin depender de binarios en tu máquina Host.

## 2. AUDITORÍA DE ARRANQUE (GATEKEEPER DE CONSOLA)
Inmediatamente después de lanzar el contenedor, debes leer activamente la consola de logs ejecutando:
```bash
docker compose logs -f ibpms-core
```
Si observas un `BeanCreationException`, `UnsatisfiedDependencyException`, o un lapidario `Connection Refused` en el puerto 8080, **SE TE PROHÍBE ENTREGAR LA TAREA**. 
Debes auto-corregir la inyección de dependencias (`@Autowired`, `@Lazy`) y verificar que el contenedor no muera silenciosamente hasta que Tomcat reporte:
> `Tomcat started on port(s): 8080 (http)`

## 3. LEY DE CORRESPONDENCIA DDL (JPA vs DB)
El entorno estricto de Spring Boot (ej. con validación estricta de Hibernate `validate`) aniquilará el servidor si tus Entidades no coinciden métricamente con la Base de Datos. Por lo tanto:
*   Si creas o modificas una entidad (`@Entity`, `@Column`), **ES TU OBLIGACIÓN ARQUITECTÓNICA** generar el archivo de migración estructural (XML/YAML/SQL) correspondiente para Liquibase/Flyway dentro de `src/main/resources/db`.
*   Si olvidas este script de la base de datos, el contenedor morirá y tu evaluación técnica será calificada como "Negligencia Grave".

**Tu código NO es válido hasta que la JVM viva lo demuestre en el puerto 8080.**
Una vez que valides el éxito del arranque en consola, cancela el proceso interactivo de Spring Boot (`Ctrl+C` o enviando input de terminación), efectúa tu empaquetado seguro y notifica al Humano Enrutador.
