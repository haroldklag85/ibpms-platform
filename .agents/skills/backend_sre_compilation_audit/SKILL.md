---
name: backend_sre_compilation_audit
description: Skill obligatoria para el Agente Backend. Exige auto-compilación con Maven y auditoría de arranque del puerto 8080 antes de entregar cualquier tarea o realizar un handoff.
triggers:
  - "Cuando el agente Backend termine de escribir código Java y se prepare para consolidar su trabajo vía commit en su rama o reportar completitud."
  - "Al crear o modificar clases @Entity o integraciones de red en Spring Boot."
---

# MANDATO DE AUTO-COMPILACIÓN Y DISCIPLINA SRE (BACKEND)

🛑 **REGLA DE SUPERVIVENCIA CERO-CONFIANZA (ZERO-TRUST COMPILATION)**

A partir de este momento, TIENES ESTRICTAMENTE PROHIBIDO asumir que tu código Java funciona solo por haberlo escrito. Tu flujo de trabajo (Workflow) cambia obligatoriamente a validación de compilación en caliente:

## 0. PRE-VALIDACIÓN DE DOCKER DAEMON (Requisito Previo Obligatorio)
Antes de ejecutar cualquier comando `docker compose`, DEBES verificar que el Docker Daemon esté activo:
1. Ejecuta: `docker info > /dev/null 2>&1 || echo "DOCKER_OFFLINE"`
2. Si el resultado es `DOCKER_OFFLINE`:
   a. **En Windows:** Intenta iniciar Docker Desktop con: `Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"` y espera 30 segundos.
   b. **En Linux/Mac:** Intenta levantar el servicio con: `sudo systemctl start docker` o `open -a Docker`.
   c. Vuelve a ejecutar `docker info` para confirmar que el daemon responde.
3. Si después de 2 intentos Docker sigue sin responder:
   a. **NO hagas fallback silencioso a `mvn compile`.**
   b. **DETENTE** y reporta el bloqueo al Humano con el mensaje: "BLOQUEADO: Docker Daemon no disponible. No puedo cumplir LEY GLOBAL 2. Se requiere intervención de infraestructura."
   c. Documenta el bloqueo en `.agentic-sync/infra_blocker_[fecha].md`.

## 0.5 FALLBACK DE EMERGENCIA LOCAL (Solo con Autorización)
Si Docker está inoperante **Y** el Arquitecto Líder autoriza explícitamente el desvío, el agente PUEDE usar el binario local `mvn test` o `mvn spring-boot:run` (ej. `.\maven_bin\apache-maven-3.9.6\bin\mvn.cmd test`) SOLO para ejecución de pruebas unitarias o de integración. La compilación final de producción SIGUE requiriendo Docker.

## 1. PROHIBIDO EL HANDOFF CIEGO
Antes de enviar cualquier estado a QA, al Arquitecto, o notificar que has terminado, **DEBES** ejecutar la compilación y arranque mediante la topología de contenedores agnóstica de la plataforma. En la raíz del proyecto, ejecuta:
```bash
docker compose up -d --build ibpms-core
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
Una vez que valides el éxito del arranque en consola, cancela el proceso interactivo de Spring Boot (`Ctrl+C` o enviando input de terminación), efectúa `git commit` asegurándote de estar posicionado en tu rama respectiva (`sprint-X/...`) y notifica al Humano Enrutador.
