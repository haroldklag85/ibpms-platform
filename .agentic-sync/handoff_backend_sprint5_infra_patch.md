# Handoff: Iteración 1 - Parche Jackson y Desafío Camunda (Sprint 5)

## 📌 Metadatos del Handoff
- **Agente Destino:** Desarrollador Backend (Agente Java/Spring)
- **Autor:** Arquitecto Líder (Antigravity)
- **Épica/CAs:** Infra-01
- **Riesgo:** Alto (Afecta el Contexto de Spring y el Worker E2E)

## 🎯 Objetivo de la Tarea
Reactivar el Cliente de Tareas Externas de Camunda sin que vuelva a tumbar las suites de integración E2E o genere un goteo de memoria (Memory Leak) / ruido de log (Poll Timeout) por no poder mapear respuestas vacías de la REST API (Exception Null Handling). Se ha optado por la **Opción 1: Parche Interno (Jackson)** para no desestabilizar la arquitectura base.

## 🛠️ Acciones Tácticas Requeridas

### 1. Parche de Serialización Jackson
Camunda Client en su capa HTTP a veces recibe un 204 No Content o un JSON vacío cuando el long-polling no encuentra tareas, y Jackson falla con `MismatchedInputException`.

**Acción:** 
Crear o modificar la configuración de Jackson en `ibpms-core` (`src/main/java/com/ibpms/poc/infrastructure/config/`) para que el `ObjectMapper` tolere la ausencia de cuerpo y evite colapsar el hilo:
- Desactivar la validación de objetos vacíos (`SerializationFeature.FAIL_ON_EMPTY_BEANS`).
- Tolerancia a primitivas vacías.
*Nota Arquitectónica:* Alternativamente, puedes registrar un interceptor HTTP en el RestTemplate del Camunda Client para sobreescribir la respuesta vacía con un `{}` (JSON vacío válido) si el `ObjectMapper` general es intocable por las políticas Zero-Trust.

### 2. Reactivación del Cliente en `application.yml`
**Archivo:** `backend/ibpms-core/src/main/resources/application.yml`
**Acción:** Existen líneas que dictan `camunda.bpm.client.disable: true`. Deberás cambiarlo a `false`. 
*Mismo comportamiento para `application-test.yml` si está afectando.*

### 3. Ajuste de Cadencia de Long-Polling (Backoff)
Para evitar que el contenedor de Spring Boot asfixie la red (Windows/Docker Network Limits), asegúrate de que exista un Backoff tolerante o un polling extenso en las properties:
```yaml
camunda:
  bpm:
    client:
      max-tasks: 1
      async-response-timeout: 10000 # 10 segundos de long-polling (No agresivo)
      disable: false
```

## 🧪 Criterios de Validación (Salida Verde)
1. Levantar el proyecto `mvn clean package -DskipTests` exitosamente.
2. Levantar los contenedores DOCKER (PostgreSQL, RabbitMQ, Camunda).
3. Iniciar `Application.java`.
4. El log de la aplicación **NO DEBE** listar ningún `MismatchedInputException` derivado de los sub-hilos de Camunda de la clase `org.camunda.bpm.client`. El Worker debe registrar "fetchAndLock" en el motor y quedar en suspensión silente y verde.

## 🚦 Bloqueos Pendientes
No procedas con el CA-11 de la *US-002 (DB Locking)* hasta que este issue basal de conectividad quede 100% certificado. Reporta al equipo cuando termines.
