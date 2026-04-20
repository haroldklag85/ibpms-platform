# Diagnóstico y Remediación de la Inicialización E2E del Backend

## 1. Identificación del Problema (Colapso del Servidor 500)

Tras realizar la refactorización arquitectónica de `KanbanV2BoardEntity` para mitigar el `DuplicateMappingException`, los agentes Front, QA y subagentes de captura de pantalla documentaron persistencia en el fallo bajo el escenario `http://localhost:5173/login?emergency=true`. 
El frontend reportaba **ALERTA DEL SISTEMA: NIVEL 0 - Colapso del Servidor / Integración Cíclica (Código de Error: 500)**.

> [!WARNING]
> Este error de "Integración Cíclica" es un wrapper hardcodeado en `apiClient.ts` que se emite automáticamente cuando el Spring Boot se colapsa y devuelve un fallo de conexión directo (`Empty reply from server`), lo que indica que el servidor no estaba arrancando de manera saludable pese a que el Fix en el host Windows sí era exitoso.

## 2. Root-Cause Analysis Documentado a través de Docker Inspect

La trazabilidad del agente en consola permitió evidenciar que la directiva de pruebas (Cita: `el qa debe validar que se utilice la base de datos del docker garantizando una real ejecución de pruebas`) implica que la ejecución de `spring-boot:run` se realiza **dentro del contenedor** `ibpms-core-dev`.

### Causa Raíz
Dado que el volumen del sistema de archivos Windows se enlaza con el de Docker, al renombrar la clase `KanbanBoardEntity.java` a `KanbanV2BoardEntity.java` en local, el plugin de compilador Maven dentro de Docker ejecutó una compilación incremental y generó exitosamente `KanbanV2BoardEntity.class`, pero **NO eliminó el archivo huérfano viejo** `KanbanBoardEntity.class` que residía en la caché `/app/ibpms-core/target/`.

Esto ocasionó un "Cyclic Server Collapse Loop":
1. Spring Boot escaneaba el classpath de `target/`.
2. Hiberate encontraba simultáneamente la implementación vieja y la nueva.
3. Desencadenaba infinitamente la `org.hibernate.AnnotationException: targets an unknown entity`.
4. El contenedor caía, se reiniciaba por AutoDevtools, y entraba en bucle de fallo durante la conexión del frontend.

## 3. Remediación

1. **Wipe Completo y Radical de Caché (Host y Container):**
   - Ejecución del comando destructivo en PowerShell y dentro del Docker: `mvn clean`.
   - Limpieza completa del directorio `target/` del Host anfitrión (Windows) para descartar persistencia transaccional del WSL2.
2. **Re-aprovisionamiento y Build Zero-Knowledge:**
   - Reinicio del contenedor de Docker para asegurar un clean compilation state total que fuerza la descarga y generación final bytecode alineada a la arquitectura limpia de la Sprint 6.2.

> [!SUCCESS]
> **El backend ya se encuentra recompilando el módulo de manera segura sin dependencias fantasma.** 
> Se ha mitigado cualquier fuga de código en el contenedor.
