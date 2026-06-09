# 🔧 HANDOFF: Remediación de 31 Tests de Integración (ARQ-005)
> **Fecha:** 2026-05-01
> **Prioridad:** 🔴 ALTA (Post-Refactorización Bloque 2)
> **Agente Destino:** 🔴 BACKEND 

Este documento detalla la estrategia de remediación para los 31 tests del backend que fallan tras la migración a la Arquitectura Hexagonal (ARQ-005).

---

## 🔍 Análisis Forense de los Fallos

El análisis profundo del reporte de `Surefire` revela que los 31 fallos **NO son un único problema**, sino que se dividen en 3 categorías exactas que requieren abordajes distintos:

### Categoría 1: Bloqueos de Infraestructura (Falsos Negativos)
- **Error:** `java.lang.NoClassDefFoundError` (Ej. `CreateFormDesignDTO`, `WorkdeskGlobalItemDTO`).
- **Afectados:** `FormDesignControllerTest`, `WorkdeskQueryPerformanceTest`, etc.
- **Causa Real:** El servidor de desarrollo en vivo (`spring-boot:run` dentro de Docker) mantiene "bloqueada" la carpeta `/target/classes` a través del volumen de Windows. Cuando ejecutamos `mvn test`, Maven no puede sobrescribir ni leer correctamente las clases compiladas, lanzando un error de clase no encontrada aunque la clase sí exista en el código fuente.
- **Solución (Infra):** Apagar el proceso de Spring Boot dentro del contenedor antes de compilar los tests, asegurando que los archivos `.class` puedan reescribirse sin bloqueo de I/O.

### Categoría 2: Cambios en el Contrato de la API (JSON Path)
- **Error:** `java.lang.AssertionError: No value at JSON path "$[0].description"`
- **Afectados:** `SlaAdminControllerTest` (y otros controladores refactorizados).
- **Causa Real:** Durante el ARQ-005, se actualizaron los DTOs de respuesta para ser más eficientes (por ejemplo, omitiendo campos nulos o renombrando propiedades). Sin embargo, el test sigue exigiendo el formato JSON antiguo estricto.
- **Solución (Backend):** Actualizar las aserciones `jsonPath()` dentro de los tests para que coincidan con la nueva estructura de los DTOs del dominio Hexagonal.

### Categoría 3: Evolución de las Políticas de Seguridad
- **Error:** `org.opentest4j.AssertionFailedError: expected: <true> but was: <false>`
- **Afectados:** `GlobalExceptionHandlerTest`.
- **Causa Real:** Se implementaron nuevas reglas de seguridad globales (ocultar stack traces en producción, registrar de forma diferente en ELK). El test estaba diseñado para validar la política anterior.
- **Solución (Backend):** Modificar el test para validar el nuevo comportamiento esperado de la política Zero-Trust.

---

## 🛠️ Plan de Ejecución (Handoff Backend)

El Agente Backend deberá ejecutar los siguientes pasos **una vez finalizada la arquitectura base del Bloque 2**:

### Fase 1: Desbloqueo de Infraestructura
1. Dentro del contenedor, asegurarse de detener cualquier proceso Java en background que no sea el testrunner.
2. Ejecutar `mvn clean test-compile` forzando la regeneración de la carpeta `target/`.
3. Esto eliminará todos los errores "fantasma" de la **Categoría 1**.

### Fase 2: Remediación Lógica
1. Ejecutar `mvn test` (sobre el directorio ya limpio).
2. Para cada fallo de la **Categoría 2** (Contratos API):
   - Revisar el Endpoint del Controller y verificar qué DTO está devolviendo.
   - Ajustar los `jsonPath()` para que las pruebas pasen con la nueva estructura de datos.
3. Para cada fallo de la **Categoría 3** (Políticas):
   - Modificar las aserciones estáticas de JUnit para validar el nuevo manejo de excepciones.

### Fase 3: Integración Final
1. Confirmar que el comando `mvn test -pl ibpms-core` devuelve `BUILD SUCCESS`.
2. Reportar al Arquitecto Líder la certificación técnica de los tests.
