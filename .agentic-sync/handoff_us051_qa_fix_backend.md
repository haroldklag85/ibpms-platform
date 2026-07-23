# Handoff Resolutivo: Bugs QA Lote 1 (Backend) - US-051

**Destinatario:** Agente Backend
**Emisor:** Arquitecto Líder (Aprobado tras revisión de QA)
**Prioridad:** 🚨 CRÍTICA (Bloqueante UAT)

## 🐛 Bug Reportado
El contenedor `ibpms-core-dev` colapsa con la siguiente excepción, saturando el thread-pool y tirando abajo el endpoint `/api/v1/auth/emergency-login` (HTTP 500):
```
TASK/CLIENT-03001 Exception while fetching and locking task.
Caused by: com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
```

## 🔍 Análisis Forense
El `ExternalTaskClient` (configurado para el `GenerativeTaskWorker`) está apuntando a un endpoint incorrecto del motor Camunda, recibiendo un 404 o una respuesta vacía que Jackson no puede mapear a JSON.

## 🛠 Plan de Acción (Zero-Mock)

### 1. Corrección de `application.yml`
En `backend/ibpms-core/src/main/resources/application.yml`:
- Localiza `camunda.bpm.client.base-url: http://localhost:8080/api/v1/engine-rest`.
- Modifícalo a la ruta por defecto real de Camunda REST: `http://localhost:8080/engine-rest`.

### 2. Estabilización de Deserialización Jackson
Para prevenir futuros `MismatchedInputException` si Camunda devuelve strings vacías en long-polling, añade (si no existen) estas propiedades en la sección `spring.jackson` de tu `application.yml`:
```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
```

---
**DoD (Definition of Done):**
- Modificar YAMLs.
- Reiniciar el contexto (compilar).
- Notificar al humano: "Handoff Backend QA Fix completado."
