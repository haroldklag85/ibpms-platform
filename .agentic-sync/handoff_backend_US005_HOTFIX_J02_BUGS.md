# Handoff Backend: Fix BUG-J02-003 (P0 Mock en FormDirectoryService) + BUG-J02-001/002

## Metadata
- **Iteración:** Sprint01-UAT-HOTFIX
- **US:** US-005 (CA-39, CA-40), US-003 (Catálogo Formularios)
- **Rama:** DevDavid
- **Prioridad:** 🔴 P0 — BLOQUEANTE (Certificación UAT pausada)
- **Origen:** Informe del Agente QA durante Certificación UAT Manual J-02
- **Necesita QA:** no (la certificación UAT manual se reanuda post-fix)

## Alineación Arquitectónica
- **ADR-001 (Hexagonal):** El `FormDirectoryService` vive en `application/service/` (capa correcta), pero su lógica viola la arquitectura al NO usar un puerto de dominio.
- **ADR-009 (PostgreSQL):** Toda persistencia debe pasar por JPA/PostgreSQL. Datos hardcodeados en memoria son una violación directa.
- **Zero-Mock Policy:** La variable `mockDirectory` es una violación flagrante de la política Zero-Mock del proyecto.

---

## BUG-J02-003 (P0 BLOQUEANTE): Mock en Catálogo de Formularios

### Causa Raíz Verificada por el Arquitecto
El archivo `FormDirectoryService.java` (31 líneas) en la ruta:
```
backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/form/FormDirectoryService.java
```

Contiene una variable `mockDirectory` (líneas 13-17) con 3 formularios hardcodeados:
- FRM-001 "Solicitud de Crédito Express"
- FRM-002 "Alta de Empleado (Onboarding)"
- FRM-003 "Reclamación Seguro (PQR)"

El método `searchForms()` (líneas 19-29) retorna estos datos falsos en lugar de consultar PostgreSQL.

### Controller que lo expone
```
backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/FormDirectoryController.java
```
- Endpoint: `GET /api/v1/forms` → `FormDirectoryService.searchForms(search)`
- Este es el endpoint que `FormList.vue` consume para el Catálogo de Formularios

### Solución Prescrita (NO es sugerencia — es ORDEN del Arquitecto)
Ya existe una implementación funcional que consulta la BD real. El fix consiste en **reutilizar la lógica existente**, NO crear lógica nueva:

**Archivo que SÍ funciona correctamente:**
```
backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/FormDesignService.java
```
- Método: `listarCatalogo()` (línea 52)
- Usa: `formDesignPort.findAllActive()` → JPA → PostgreSQL ✅
- Ya lo consume: `FormCatalogController.java` en `GET /api/v1/forms/active`

**Instrucciones quirúrgicas de corrección:**

1. **Abrir** `FormDirectoryService.java`
2. **Eliminar** completamente la variable `mockDirectory` (líneas 12-17)
3. **Inyectar** el `FormDesignService` (que ya existe) como dependencia
4. **Refactorizar** `searchForms()` para que:
   - Llame a `formDesignService.listarCatalogo()` para obtener la lista real de formularios
   - Mapee los `FormDesignDTO` al formato `Map<String, Object>` que el frontend espera (campos: `id`, `name`, `type`, `version`, `author`, `updatedAt`)
   - Aplique el filtro de búsqueda (parámetro `query`) sobre los datos reales
5. **Eliminar** el comentario de la línea 12 que dice "Evasión de BD compleja para acelerar Boot"

**Contrato de respuesta esperado (para que el Frontend no se rompa):**
El endpoint `GET /api/v1/forms` debe retornar un array JSON con objetos que contengan al menos:
```json
[
  {
    "id": "string (technicalName del form)",
    "name": "string (nombre del formulario)",
    "type": "string (SIMPLE o MASTER)",
    "version": "string (versión)",
    "author": "string",
    "updatedAt": "string (ISO datetime)"
  }
]
```

---

## BUG-J02-001 (P3): Ruta `/admin/modeler/` retorna 404

### Causa Raíz
En `frontend/src/router/index.ts`, las 5 rutas del modeler son siblings planas sin ruta padre `/admin/modeler`.

### Fix Quirúrgico
Agregar un redirect en el router:
```typescript
{ path: '/admin/modeler', redirect: '/admin/modeler/bpmn' },
```
Ubicación: Dentro del array de `children` del layout autenticado, ANTES de las rutas individuales del modeler.

---

## BUG-J02-002 (P3): Link roto en BpmnDesigner para Call Activities

### Causa Raíz
En `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, aproximadamente en la línea 4197:
```javascript
window.open('/admin/modeler?processId=...')
```
La ruta `/admin/modeler` no existe (ver BUG-001). Debe apuntar a `/admin/modeler/bpmn`.

### Fix Quirúrgico
Cambiar a:
```javascript
window.open('/admin/modeler/bpmn?processId=...')
```

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> 🛑 **REGLA FUNDAMENTAL E IRROMPIBLE DE TEARDOWN DOCKER:**
> Queda absolutamente prohibido crear, levantar o dejar contenedores Docker adicionales fuera de los 3 servicios de soporte (postgres, redis, rabbitmq).

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

**Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

**Build obligatorio Frontend (para BUG-001 y BUG-002):** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_Backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_Backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
