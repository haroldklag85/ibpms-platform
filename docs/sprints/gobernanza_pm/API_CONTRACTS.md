# 📜 Registro Centralizado de Contratos de API — IBPMS Platform

> **Versión**: 1.0.0
> **Fecha de vigencia**: 2026-06-02
> **Clasificación**: SSOT (Single Source of Truth) — Este documento es la ÚNICA fuente de verdad para endpoints de API.
> **Ubicación canónica**: `docs/sprints/gobernanza_pm/API_CONTRACTS.md`
> **Responsable**: Arquitecto Líder (creación y actualización)
> **Autoridad**: PM-IA (aprobación de cambios estructurales)

---

## 1. Propósito

Este documento es la **Fuente Única de Verdad (SSOT)** para TODOS los endpoints de API consumidos por el Frontend y producidos por el Backend de la plataforma IBPMS.

**Objetivos:**
- Eliminar la alucinación de endpoints por parte de agentes de IA
- Garantizar que Backend y Frontend trabajen sobre contratos idénticos
- Proveer un punto de referencia auditable para QA
- Prevenir la creación de endpoints "fantasma" no documentados
- Alinear los handoffs de Backend y Frontend sobre la misma especificación

---

## 2. Regla Cardinal

> ### 🚫 Si un endpoint NO está en este documento, ese endpoint NO EXISTE.
>
> Ningún agente (Arquitecto Líder, Backend, Frontend, QA) puede:
> - **Referenciar** un endpoint no listado aquí en un handoff
> - **Consumir** un endpoint no listado aquí en código Frontend
> - **Producir** un endpoint no listado aquí en código Backend
> - **Testear** un endpoint no listado aquí en pruebas QA
>
> **Violación de esta regla = Handoff inválido = Iteración rechazada por PM-IA.**

---

## 3. Leyenda de Estados

| Icono | Estado | Significado |
|---|---|---|
| ✅ | **Verified** | Endpoint existe en el código, compilado y testeado. Incluye commit hash. |
| ⚠️ | **Assumed** | Endpoint documentado pero NO verificado en código. Necesita auditoría. |
| ❌ | **Missing** | Endpoint necesario pero NO implementado. Requiere creación. |
| 🔄 | **Modified** | Contrato modificado recientemente. Verificar alineación Backend/Frontend. |

---

## 4. Formato de Contrato (Plantilla)

Cada endpoint DEBE documentarse con la siguiente estructura:

```markdown
### [METHOD] [PATH]
- **Estado**: ✅ Verified / ⚠️ Assumed / ❌ Missing
- **US**: US-XXX
- **CA**: CA-XX
- **Descripción**: Qué hace este endpoint en lenguaje claro
- **Auth**: Bearer JWT / Public / API Key
- **Headers**:
  - `Content-Type`: application/json
  - `Authorization`: Bearer {token}
- **Path Params**: (si aplica)
  - `{id}`: UUID — Identificador del recurso
- **Query Params**: (si aplica)
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
- **Request Body**:
  ```json
  {
    "campo": "tipo — descripción"
  }
  ```
- **Response 200**:
  ```json
  {
    "campo": "tipo — descripción"
  }
  ```
- **Response 4xx**:
  ```json
  {
    "error": "string — código de error",
    "message": "string — descripción legible",
    "timestamp": "ISO-8601"
  }
  ```
- **Notas**: Comportamiento especial (paginación, CQRS, caché, etc.)
- **Commit de verificación**: (solo si ✅) `abc1234`
- **Última actualización**: YYYY-MM-DD
```

---

## 5. Contratos Conocidos — Inventario Inicial

> **⚠️ AVISO IMPORTANTE**: Todos los contratos en este inventario inicial están marcados como **⚠️ Assumed**. El Arquitecto Líder DEBE verificar cada uno contra el código fuente real y actualizar su estado. Ningún handoff debe asumir que estos contratos están implementados hasta su verificación.

---

### 5.1 Auth & Identity (Autenticación e Identidad)

#### POST /api/auth/login
- **Estado**: ⚠️ Assumed
- **US**: US-001 (estimada)
- **CA**: Por determinar
- **Descripción**: Autenticar usuario con credenciales y obtener token JWT
- **Auth**: Public (no requiere autenticación previa)
- **Headers**:
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "username": "string — nombre de usuario o email",
    "password": "string — contraseña del usuario"
  }
  ```
- **Response 200**:
  ```json
  {
    "accessToken": "string — JWT access token",
    "refreshToken": "string — JWT refresh token",
    "expiresIn": "integer — segundos hasta expiración",
    "tokenType": "string — 'Bearer'"
  }
  ```
- **Response 401**:
  ```json
  {
    "error": "INVALID_CREDENTIALS",
    "message": "Usuario o contraseña incorrectos",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Rate limiting recomendado (máx. 5 intentos/minuto por IP)
- **Última actualización**: 2026-06-02

---

#### POST /api/auth/refresh
- **Estado**: ⚠️ Assumed
- **US**: US-001 (estimada)
- **CA**: Por determinar
- **Descripción**: Renovar el access token usando un refresh token válido
- **Auth**: Public (el refresh token va en el body)
- **Headers**:
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "refreshToken": "string — JWT refresh token vigente"
  }
  ```
- **Response 200**:
  ```json
  {
    "accessToken": "string — nuevo JWT access token",
    "refreshToken": "string — nuevo JWT refresh token (rotación)",
    "expiresIn": "integer — segundos hasta expiración"
  }
  ```
- **Response 401**:
  ```json
  {
    "error": "INVALID_REFRESH_TOKEN",
    "message": "El refresh token es inválido o ha expirado",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Implementar rotación de refresh tokens para seguridad
- **Última actualización**: 2026-06-02

---

#### GET /api/auth/me
- **Estado**: ⚠️ Assumed
- **US**: US-001 (estimada)
- **CA**: Por determinar
- **Descripción**: Obtener perfil del usuario autenticado actualmente
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Response 200**:
  ```json
  {
    "id": "UUID — identificador del usuario",
    "username": "string — nombre de usuario",
    "email": "string — correo electrónico",
    "roles": ["string — lista de roles asignados"],
    "fullName": "string — nombre completo",
    "tenantId": "UUID — identificador del tenant (si aplica)"
  }
  ```
- **Response 401**:
  ```json
  {
    "error": "UNAUTHORIZED",
    "message": "Token inválido o expirado",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Este endpoint es el punto de entrada para cargar el perfil en el store de Pinia
- **Última actualización**: 2026-06-02

---

### 5.2 Workdesk (Bandeja de Tareas)

#### GET /api/tasks/pending
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Obtener la lista de tareas pendientes asignadas al usuario autenticado
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
  - `sort`: string — Campo de ordenamiento (default: "createdAt,desc")
- **Response 200**:
  ```json
  {
    "content": [
      {
        "taskId": "string — ID de tarea Camunda",
        "processInstanceId": "string — ID de instancia de proceso",
        "taskName": "string — nombre de la tarea",
        "taskDefinitionKey": "string — key de definición BPMN",
        "assignee": "string — usuario asignado",
        "candidateGroups": ["string — grupos candidatos"],
        "createdAt": "ISO-8601 — fecha de creación",
        "dueDate": "ISO-8601 — fecha límite (nullable)",
        "priority": "integer — prioridad (0-100)",
        "processDefinitionName": "string — nombre del proceso"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer",
    "pageNumber": "integer",
    "pageSize": "integer"
  }
  ```
- **Response 401**: Token inválido (formato estándar de error)
- **Notas**: Las tareas provienen de Camunda 7 Task Service. Paginación Spring Data estándar.
- **Última actualización**: 2026-06-02

---

#### POST /api/tasks/{id}/claim
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Reclamar (asignarse) una tarea de la bandeja grupal
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{id}`: string — ID de la tarea Camunda
- **Request Body**: Vacío (el usuario se determina del JWT)
- **Response 200**:
  ```json
  {
    "taskId": "string — ID de la tarea",
    "assignee": "string — usuario que reclamó la tarea",
    "claimedAt": "ISO-8601 — timestamp del reclamo"
  }
  ```
- **Response 409**:
  ```json
  {
    "error": "TASK_ALREADY_CLAIMED",
    "message": "La tarea ya fue reclamada por otro usuario",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Operación idempotente si el mismo usuario reclama la misma tarea
- **Última actualización**: 2026-06-02

---

#### POST /api/tasks/{id}/release
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Liberar una tarea previamente reclamada, devolviéndola a la bandeja grupal
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{id}`: string — ID de la tarea Camunda
- **Request Body**: Vacío
- **Response 200**:
  ```json
  {
    "taskId": "string — ID de la tarea",
    "releasedAt": "ISO-8601 — timestamp de liberación",
    "status": "string — 'UNASSIGNED'"
  }
  ```
- **Response 403**:
  ```json
  {
    "error": "NOT_TASK_OWNER",
    "message": "Solo el asignado actual puede liberar la tarea",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Solo el assignee actual puede liberar la tarea
- **Última actualización**: 2026-06-02

---

#### POST /api/tasks/{id}/delegate
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Delegar una tarea a otro usuario específico
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Path Params**:
  - `{id}`: string — ID de la tarea Camunda
- **Request Body**:
  ```json
  {
    "targetUserId": "string — ID del usuario destinatario",
    "reason": "string — motivo de la delegación (opcional)"
  }
  ```
- **Response 200**:
  ```json
  {
    "taskId": "string — ID de la tarea",
    "delegatedTo": "string — usuario destinatario",
    "delegatedBy": "string — usuario que delegó",
    "delegatedAt": "ISO-8601 — timestamp"
  }
  ```
- **Response 404**:
  ```json
  {
    "error": "USER_NOT_FOUND",
    "message": "El usuario destinatario no existe",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Requiere permiso de delegación. Genera notificación al destinatario.
- **Última actualización**: 2026-06-02

---

### 5.3 Forms (Formularios Dinámicos)

#### GET /api/forms/{id}
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Obtener la estructura y datos de un formulario asociado a una tarea
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{id}`: string — ID del formulario o task form key
- **Response 200**:
  ```json
  {
    "formId": "string — identificador del formulario",
    "formKey": "string — clave de formulario BPMN",
    "title": "string — título del formulario",
    "fields": [
      {
        "fieldId": "string — identificador del campo",
        "type": "string — text|number|date|select|checkbox|textarea|file",
        "label": "string — etiqueta visible",
        "required": "boolean — campo obligatorio",
        "defaultValue": "any — valor por defecto (nullable)",
        "validations": {
          "minLength": "integer (nullable)",
          "maxLength": "integer (nullable)",
          "pattern": "string — regex (nullable)",
          "options": ["string — opciones para select (nullable)"]
        }
      }
    ],
    "data": {
      "fieldId": "valor actual del campo (para formularios pre-rellenados)"
    }
  }
  ```
- **Response 404**:
  ```json
  {
    "error": "FORM_NOT_FOUND",
    "message": "El formulario solicitado no existe",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Los formularios pueden estar vinculados a tareas Camunda via `formKey`
- **Última actualización**: 2026-06-02

---

#### POST /api/forms/{id}/submit
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Enviar los datos de un formulario completado, completando la tarea asociada
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Path Params**:
  - `{id}`: string — ID del formulario
- **Request Body**:
  ```json
  {
    "taskId": "string — ID de la tarea Camunda asociada",
    "data": {
      "fieldId1": "valor del campo 1",
      "fieldId2": "valor del campo 2"
    }
  }
  ```
- **Response 200**:
  ```json
  {
    "formId": "string — ID del formulario",
    "taskId": "string — ID de la tarea completada",
    "submittedAt": "ISO-8601 — timestamp de envío",
    "processInstanceId": "string — ID de la instancia de proceso"
  }
  ```
- **Response 422**:
  ```json
  {
    "error": "VALIDATION_ERROR",
    "message": "Errores de validación en los campos",
    "fieldErrors": [
      {
        "fieldId": "string — campo con error",
        "message": "string — descripción del error"
      }
    ],
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Al completar el formulario, se completa la tarea Camunda y el proceso avanza
- **Última actualización**: 2026-06-02

---

#### GET /api/form-definitions
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Listar todas las definiciones de formularios disponibles en el sistema
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
- **Response 200**:
  ```json
  {
    "content": [
      {
        "formId": "string — identificador del formulario",
        "formKey": "string — clave BPMN",
        "title": "string — título",
        "version": "integer — versión del formulario",
        "createdAt": "ISO-8601",
        "updatedAt": "ISO-8601",
        "fieldCount": "integer — número de campos"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer",
    "pageNumber": "integer",
    "pageSize": "integer"
  }
  ```
- **Notas**: Paginación Spring Data estándar. Usado por el admin para gestionar formularios.
- **Última actualización**: 2026-06-02

---

### 5.4 BPMN (Motor de Procesos)

#### POST /api/bpmn/deploy
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Desplegar un archivo BPMN al motor Camunda 7
- **Auth**: Bearer JWT (requiere rol ADMIN o PROCESS_MANAGER)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: multipart/form-data
- **Request Body** (multipart):
  - `file`: archivo .bpmn — Definición del proceso BPMN 2.0
  - `deploymentName`: string — Nombre del despliegue
  - `tenantId`: string — ID del tenant (opcional)
- **Response 200**:
  ```json
  {
    "deploymentId": "string — ID del despliegue Camunda",
    "deploymentName": "string — nombre del despliegue",
    "deployedAt": "ISO-8601 — timestamp",
    "processDefinitions": [
      {
        "id": "string — ID de definición del proceso",
        "key": "string — clave del proceso",
        "name": "string — nombre del proceso",
        "version": "integer — versión"
      }
    ]
  }
  ```
- **Response 400**:
  ```json
  {
    "error": "INVALID_BPMN",
    "message": "El archivo BPMN contiene errores de validación",
    "validationErrors": ["string — lista de errores"],
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Camunda 7 REST API wrapper. Validar BPMN antes de desplegar.
- **Última actualización**: 2026-06-02

---

#### GET /api/bpmn/definitions
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Listar todas las definiciones de procesos BPMN desplegadas
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `latestVersion`: boolean — Solo última versión (default: true)
  - `key`: string — Filtrar por clave de proceso (opcional)
- **Response 200**:
  ```json
  {
    "definitions": [
      {
        "id": "string — ID de definición",
        "key": "string — clave del proceso",
        "name": "string — nombre del proceso",
        "version": "integer — versión",
        "deploymentId": "string — ID del despliegue",
        "suspended": "boolean — si está suspendido",
        "description": "string — descripción (nullable)"
      }
    ]
  }
  ```
- **Notas**: Wrapper sobre Camunda 7 Process Definition API
- **Última actualización**: 2026-06-02

---

#### POST /api/bpmn/instances
- **Estado**: ✅ Verified
- **US**: US-007
- **CA**: Todos
- **Descripción**: Iniciar una nueva instancia de un proceso BPMN
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "processDefinitionKey": "string — clave del proceso a iniciar",
    "businessKey": "string — clave de negocio (opcional)",
    "variables": {
      "variableName": {
        "value": "any — valor de la variable",
        "type": "string — String|Integer|Boolean|Json"
      }
    }
  }
  ```
- **Response 201**:
  ```json
  {
    "processInstanceId": "string — ID de la instancia creada",
    "processDefinitionKey": "string — clave del proceso",
    "businessKey": "string — clave de negocio",
    "startedAt": "ISO-8601 — timestamp de inicio",
    "startedBy": "string — usuario que inició el proceso"
  }
  ```
- **Response 404**:
  ```json
  {
    "error": "PROCESS_NOT_FOUND",
    "message": "No se encontró la definición de proceso con la clave indicada",
    "timestamp": "ISO-8601"
  }
  ```
- **Notas**: Las variables de proceso se pasan al motor Camunda y están disponibles en tareas subsecuentes. Integrado en el Panel de Catálogo de Procesos del Workdesk.
- **Commit de verificación**: 76c6ffc0
- **Última actualización**: 2026-06-07

---

#### POST /api/bpmn/tasks/{taskId}/complete
- **Estado**: ✅ Verified
- **US**: US-007
- **CA**: Todos
- **Descripción**: Completar directamente una tarea en el motor BPMN (vía API nativa de ejecución, saltando la abstracción de Workdesk si es necesario)
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Path Params**:
  - `{taskId}`: string — ID de la tarea BPMN
- **Request Body**:
  ```json
  {
    "variables": {
      "varName": {
        "value": "any — valor de la variable",
        "type": "string"
      }
    }
  }
  ```
- **Response 204**: No Content
- **Response 404**:
  ```json
  {
    "error": "TASK_NOT_FOUND",
    "message": "La tarea indicada no existe o ya fue completada",
    "timestamp": "ISO-8601"
  }
  ```
- **Notas**: Creado para US-007 y utilizado en bifurcación de completitud en `useWorkdeskStore`.
- **Commit de verificación**: 76c6ffc0
- **Última actualización**: 2026-06-07

---

### 5.5 DMN (Tablas de Decisión)

#### POST /api/dmn/generate
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Generar una tabla de decisión DMN a partir de reglas de negocio
- **Auth**: Bearer JWT (requiere rol ADMIN o RULES_MANAGER)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "tableName": "string — nombre de la tabla de decisión",
    "hitPolicy": "string — UNIQUE|FIRST|PRIORITY|ANY|COLLECT|RULE_ORDER|OUTPUT_ORDER",
    "inputs": [
      {
        "label": "string — etiqueta de entrada",
        "expression": "string — expresión FEEL",
        "type": "string — string|integer|boolean|double|date"
      }
    ],
    "outputs": [
      {
        "label": "string — etiqueta de salida",
        "name": "string — nombre de la variable de salida",
        "type": "string — tipo de dato"
      }
    ],
    "rules": [
      {
        "inputEntries": ["string — condiciones de entrada"],
        "outputEntries": ["string — valores de salida"]
      }
    ]
  }
  ```
- **Response 200**:
  ```json
  {
    "dmnId": "UUID — ID del DMN generado",
    "tableName": "string — nombre de la tabla",
    "status": "string — 'DRAFT'",
    "createdAt": "ISO-8601",
    "previewXml": "string — XML DMN generado (truncado)"
  }
  ```
- **Notas**: Genera un borrador DMN. Requiere publicación explícita con POST /api/dmn/publish
- **Última actualización**: 2026-06-02

---

#### GET /api/dmn/drafts
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Listar borradores de tablas de decisión DMN pendientes de publicación
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
- **Response 200**:
  ```json
  {
    "content": [
      {
        "dmnId": "UUID — ID del DMN",
        "tableName": "string — nombre de la tabla",
        "status": "string — DRAFT|PUBLISHED|ARCHIVED",
        "createdAt": "ISO-8601",
        "updatedAt": "ISO-8601",
        "createdBy": "string — usuario creador"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer",
    "pageNumber": "integer",
    "pageSize": "integer"
  }
  ```
- **Notas**: Paginación Spring Data estándar
- **Última actualización**: 2026-06-02

---

#### POST /api/dmn/publish
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Publicar (desplegar) un borrador DMN al motor Camunda
- **Auth**: Bearer JWT (requiere rol ADMIN o RULES_MANAGER)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "dmnId": "UUID — ID del borrador DMN a publicar"
  }
  ```
- **Response 200**:
  ```json
  {
    "dmnId": "UUID — ID del DMN",
    "deploymentId": "string — ID del despliegue Camunda",
    "status": "string — 'PUBLISHED'",
    "publishedAt": "ISO-8601",
    "publishedBy": "string — usuario que publicó",
    "decisionDefinitionId": "string — ID de definición de decisión Camunda"
  }
  ```
- **Response 400**:
  ```json
  {
    "error": "INVALID_DMN",
    "message": "El DMN contiene errores de validación",
    "validationErrors": ["string — lista de errores"],
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Una vez publicado, el DMN está disponible para evaluación en procesos BPMN
- **Última actualización**: 2026-06-02

---

### 5.6 Admin (Administración de Usuarios y Roles)

#### GET /api/admin/users
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Listar todos los usuarios del sistema con sus roles
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
  - `search`: string — Búsqueda por nombre o email (opcional)
  - `role`: string — Filtrar por rol (opcional)
- **Response 200**:
  ```json
  {
    "content": [
      {
        "id": "UUID — identificador del usuario",
        "username": "string — nombre de usuario",
        "email": "string — correo electrónico",
        "fullName": "string — nombre completo",
        "roles": ["string — roles asignados"],
        "enabled": "boolean — cuenta activa",
        "createdAt": "ISO-8601",
        "lastLogin": "ISO-8601 (nullable)"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer",
    "pageNumber": "integer",
    "pageSize": "integer"
  }
  ```
- **Notas**: Solo accesible para administradores. Paginación estándar.
- **Última actualización**: 2026-06-02

---

#### POST /api/admin/users
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Crear un nuevo usuario en el sistema
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "username": "string — nombre de usuario (único)",
    "email": "string — correo electrónico (único)",
    "password": "string — contraseña inicial",
    "fullName": "string — nombre completo",
    "roles": ["string — roles a asignar"]
  }
  ```
- **Response 201**:
  ```json
  {
    "id": "UUID — identificador del usuario creado",
    "username": "string — nombre de usuario",
    "email": "string — correo electrónico",
    "roles": ["string — roles asignados"],
    "createdAt": "ISO-8601"
  }
  ```
- **Response 409**:
  ```json
  {
    "error": "USER_ALREADY_EXISTS",
    "message": "Ya existe un usuario con ese username o email",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: La contraseña se almacena hasheada con BCrypt. Se recomienda forzar cambio en primer login.
- **Última actualización**: 2026-06-02

---

#### GET /api/admin/roles
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Listar todos los roles disponibles en el sistema
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Response 200**:
  ```json
  {
    "roles": [
      {
        "id": "UUID — identificador del rol",
        "name": "string — nombre del rol",
        "description": "string — descripción del rol",
        "permissions": ["string — permisos asociados"]
      }
    ]
  }
  ```
- **Notas**: Roles predefinidos: ADMIN, PROCESS_MANAGER, RULES_MANAGER, TASK_USER, VIEWER
- **Última actualización**: 2026-06-02

---

### 5.7 SLA (Configuración de Acuerdos de Nivel de Servicio)

#### GET /api/config/sla
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Obtener la configuración actual de SLA del sistema
- **Auth**: Bearer JWT (requiere rol ADMIN o PROCESS_MANAGER)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Response 200**:
  ```json
  {
    "slaConfigurations": [
      {
        "id": "UUID — identificador de la configuración",
        "processDefinitionKey": "string — clave del proceso",
        "taskDefinitionKey": "string — clave de la tarea (nullable, aplica a todo el proceso si es null)",
        "warningThresholdHours": "integer — horas para alerta amarilla",
        "criticalThresholdHours": "integer — horas para alerta roja",
        "escalationEnabled": "boolean — si se activa escalamiento automático",
        "escalationTargetRole": "string — rol al que se escala (nullable)"
      }
    ]
  }
  ```
- **Notas**: Las configuraciones de SLA se aplican a nivel de proceso o de tarea específica
- **Última actualización**: 2026-06-02

---

#### PUT /api/config/sla
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Actualizar la configuración de SLA del sistema
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Request Body**:
  ```json
  {
    "slaConfigurations": [
      {
        "processDefinitionKey": "string — clave del proceso",
        "taskDefinitionKey": "string — clave de la tarea (nullable)",
        "warningThresholdHours": "integer — horas para alerta amarilla",
        "criticalThresholdHours": "integer — horas para alerta roja",
        "escalationEnabled": "boolean — activar escalamiento",
        "escalationTargetRole": "string — rol de escalamiento (nullable)"
      }
    ]
  }
  ```
- **Response 200**:
  ```json
  {
    "updatedAt": "ISO-8601 — timestamp de actualización",
    "updatedBy": "string — usuario que actualizó",
    "configurationsCount": "integer — número de configuraciones actualizadas"
  }
  ```
- **Response 422**:
  ```json
  {
    "error": "INVALID_SLA_CONFIG",
    "message": "warningThresholdHours debe ser menor que criticalThresholdHours",
    "timestamp": "2026-06-02T00:00:00Z"
  }
  ```
- **Notas**: Validar que warning < critical. Los cambios aplican a nuevas tareas, no retroactivamente.
- **Última actualización**: 2026-06-02

---

### 5.8 RabbitMQ / Webhooks (Integraciones Externas)

#### POST /api/webhooks/o365
- **Estado**: ⚠️ Assumed
- **US**: Por determinar
- **CA**: Por determinar
- **Descripción**: Recibir notificaciones webhook de Office 365 (emails, calendar, etc.)
- **Auth**: API Key o verificación de firma Microsoft
- **Headers**:
  - `Content-Type`: application/json
  - `X-Webhook-Secret`: string — clave de verificación (si aplica)
- **Request Body**:
  ```json
  {
    "value": [
      {
        "subscriptionId": "string — ID de la suscripción O365",
        "changeType": "string — created|updated|deleted",
        "resource": "string — recurso afectado (ej: messages/{id})",
        "resourceData": {
          "@odata.type": "string — tipo OData",
          "id": "string — ID del recurso"
        },
        "clientState": "string — estado de verificación",
        "tenantId": "string — tenant de O365"
      }
    ]
  }
  ```
- **Response 200**:
  ```json
  {
    "received": true,
    "processedCount": "integer — número de eventos procesados",
    "timestamp": "ISO-8601"
  }
  ```
- **Response 202** (validación de suscripción):
  - Microsoft envía un `validationToken` como query param al crear la suscripción
  - Responder con el token en texto plano para validar
- **Notas**: Este endpoint publica mensajes a RabbitMQ para procesamiento asíncrono. Verificar la firma de Microsoft para seguridad. Puede requerir URL pública (ngrok en desarrollo).
- **Última actualización**: 2026-06-02

---

### 5.7 Telemetría y Monitoreo (BAM)

#### GET /api/v1/bpm/telemetry/instances
- **Estado**: ✅ Verified
- **US**: US-030
- **CA**: Todos
- **Descripción**: Obtener métricas y listado de instancias de procesos BPMN (activas, completadas o suspendidas)
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `state`: string — Estado de la instancia (ACTIVE, COMPLETED, SUSPENDED)
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
- **Response 200**:
  ```json
  {
    "content": [
      {
        "id": "string — ID de la instancia",
        "processDefinitionKey": "string — clave del proceso",
        "state": "string — estado actual",
        "startTime": "ISO-8601",
        "endTime": "ISO-8601 (nullable)"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer"
  }
  ```
- **Response 403**: Forbidden (si no es admin)
- **Notas**: Utilizado por el dashboard de monitoreo para rastrear la salud del sistema.
- **Commit de verificación**: 5b243230
- **Última actualización**: 2026-06-07

---

#### GET /api/v1/bpm/telemetry/incidents
- **Estado**: ✅ Verified
- **US**: US-030
- **CA**: Todos
- **Descripción**: Obtener listado de incidentes técnicos (errores de ejecución) del motor BPMN
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `page`: integer — Número de página (default: 0)
  - `size`: integer — Tamaño de página (default: 20)
- **Response 200**:
  ```json
  {
    "content": [
      {
        "id": "string — ID del incidente",
        "processInstanceId": "string — ID de la instancia afectada",
        "incidentType": "string — tipo de incidente",
        "incidentMessage": "string — mensaje de error",
        "createTime": "ISO-8601"
      }
    ],
    "totalElements": "integer",
    "totalPages": "integer"
  }
  ```
- **Response 403**: Forbidden (si no es admin)
- **Notas**: Expone la tabla histórica y en tiempo de ejecución de incidentes Camunda.
- **Commit de verificación**: 5b243230
- **Última actualización**: 2026-06-07

---

## 6. Proceso de Actualización de Contratos

### 6.1 ¿Quién Puede Modificar Este Documento?

| Actor | Permiso |
|---|---|
| **Arquitecto Líder** | Crear, modificar y verificar contratos |
| **PM-IA** | Aprobar cambios estructurales (nuevos dominios, eliminación de endpoints) |
| **Backend** | Proponer modificaciones (requiere aprobación del Arquitecto Líder) |
| **Frontend** | Proponer modificaciones (requiere aprobación del Arquitecto Líder) |
| **QA** | Solo lectura. Reportar discrepancias. |

### 6.2 Procedimiento para Agregar un Nuevo Contrato

```
1. IDENTIFICAR la necesidad
   └─ Durante el análisis de una US, el Arquitecto Líder detecta que se
      necesita un nuevo endpoint

2. DEFINIR el contrato
   └─ Usando el formato de la Sección 4 (Plantilla)
   └─ Estado inicial: ⚠️ Assumed

3. UBICAR en el dominio correcto
   └─ Auth & Identity, Workdesk, Forms, BPMN, DMN, Admin, SLA, Webhooks
   └─ Si es un nuevo dominio, crear sección nueva con aprobación PM-IA

4. REGISTRAR en este documento
   └─ Commit con mensaje: "API-CONTRACT: Add [METHOD] [PATH] for US-XXX"

5. REFERENCIAR en los handoffs
   └─ El handoff de Backend referencia el contrato para IMPLEMENTAR
   └─ El handoff de Frontend referencia el MISMO contrato para CONSUMIR

6. VERIFICAR post-implementación
   └─ Actualizar estado a ✅ Verified con commit hash
   └─ Registrar fecha de verificación
```

### 6.3 Procedimiento para Modificar un Contrato Existente

```
1. JUSTIFICAR el cambio
   └─ ¿Por qué el contrato actual no funciona?
   └─ ¿Qué US/CA se ven afectadas?

2. EVALUAR impacto
   └─ ¿Qué agentes han implementado sobre este contrato?
   └─ ¿Hay código en producción que depende de este contrato?

3. APLICAR el cambio
   └─ Marcar el contrato como 🔄 Modified
   └─ Documentar la versión anterior (historial de cambios al final del contrato)

4. NOTIFICAR
   └─ Informar a Backend, Frontend y QA del cambio
   └─ Actualizar handoffs si están activos

5. VERIFICAR
   └─ Backend y Frontend confirman alineación
   └─ QA actualiza tests
```

### 6.4 Firma del Arquitecto Líder

Toda modificación a este documento DEBE incluir:

```markdown
**Modificado por**: Arquitecto Líder
**Fecha**: YYYY-MM-DD
**US relacionada**: US-XXX
**Motivo**: [breve justificación]
**Commit**: [hash]
```

---

## 7. Auditoría de Contratos

### 7.1 Auditoría Mensual Obligatoria

El primer día hábil de cada mes, el Arquitecto Líder DEBE ejecutar una **reconciliación** entre este documento y el código real.

**Pasos de la Auditoría:**

1. **Escanear controladores Backend**
   - Buscar todas las clases `@RestController` en el código Java
   - Extraer todos los `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
   - Comparar con este documento

2. **Escanear servicios Frontend**
   - Buscar todas las llamadas `axios.get`, `axios.post`, etc.
   - Extraer todas las URLs referenciadas
   - Comparar con este documento

3. **Clasificar discrepancias**

   | Tipo | Descripción | Acción |
   |---|---|---|
   | **Endpoint fantasma** | Existe en código pero NO en este documento | Agregar contrato o eliminar endpoint |
   | **Contrato huérfano** | Existe en este documento pero NO en código | Marcar como ❌ Missing o eliminar |
   | **Contrato desalineado** | Existe en ambos pero con schemas diferentes | Reconciliar y actualizar |

4. **Generar Reporte de Auditoría**
   ```markdown
   # Reporte de Auditoría de Contratos — [Mes] [Año]

   ## Resumen
   - Total endpoints en documento: [N]
   - Total endpoints en código: [N]
   - Endpoints fantasma: [N]
   - Contratos huérfanos: [N]
   - Contratos desalineados: [N]

   ## Discrepancias Encontradas
   [tabla detallada]

   ## Acciones Correctivas
   [plan de acción]
   ```

### 7.2 Auditoría por Demanda

El PM-IA puede solicitar una auditoría de contratos en cualquier momento, especialmente:
- Al cierre de un sprint
- Antes de iniciar una nueva Cadena de Capacidad
- Cuando se detectan errores de integración Frontend ↔ Backend

---

### 5.10 CQRS / Task Completion (US-017 — Persistencia Inmutable)

#### POST /api/v1/workbox/tasks/{taskId}/complete
- **Estado**: ⚠️ Assumed
- **US**: US-017
- **CA**: CA-01, CA-02, CA-03, CA-04, CA-10, CA-15, CA-16, CA-17
- **Descripción**: Completar una tarea enviando el formulario con persistencia CQRS inmutable. Graba evento `FORM_SUBMITTED` en Event Store, notifica a Camunda con DTO minificado, y retorna referencia visible del evento.
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
  - `X-Idempotency-Key`: UUID — Llave de idempotencia para prevenir doble envío
- **Path Params**:
  - `{taskId}`: string — ID de la tarea Camunda o Kanban
- **Request Body**:
  ```json
  {
    "formData": "object — Payload completo del formulario validado por Zod",
    "schemaVersion": "string — Versión del esquema JSON (Ej: 'V3')"
  }
  ```
- **Response 200**:
  ```json
  {
    "taskId": "string — ID de la tarea completada",
    "eventReference": "string — Código legible del evento (Ej: 'EVT-A3F8K9')",
    "eventId": "UUID — ID del evento en el Event Store",
    "processInstanceId": "string — ID de la instancia BPMN",
    "completedAt": "ISO-8601 — timestamp de completitud"
  }
  ```
- **Response 403**:
  ```json
  {
    "error": "NOT_TASK_ASSIGNEE",
    "message": "Solo el asignado actual puede completar la tarea",
    "timestamp": "ISO-8601"
  }
  ```
- **Response 409**:
  ```json
  {
    "error": "TASK_ALREADY_COMPLETED",
    "message": "Esta tarea ya fue completada por otro operario",
    "timestamp": "ISO-8601"
  }
  ```
- **Response 500**:
  ```json
  {
    "error": "ENGINE_UNAVAILABLE",
    "message": "Motor BPMN no disponible tras 3 reintentos",
    "timestamp": "ISO-8601"
  }
  ```
- **Notas**: SLA máximo 5s normal, 17s con reintentos. Auto-Claim para tareas de grupo sin assignee (CA-04). Rollback compensatorio si Camunda falla (CA-03/CA-10).
- **Última actualización**: 2026-06-09

---

#### GET /api/v1/workbox/tasks/{taskId}/draft
- **Estado**: ⚠️ Assumed
- **US**: US-017
- **CA**: CA-07
- **Descripción**: Recuperar el borrador más reciente del servidor para una tarea
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{taskId}`: string — ID de la tarea
- **Response 200**:
  ```json
  {
    "currentStep": "integer — paso actual del wizard (nullable)",
    "partialData": "object — datos parciales del formulario",
    "schemaVersion": "string — versión del esquema",
    "updatedAt": "ISO-8601 — última actualización del borrador"
  }
  ```
- **Response 404**:
  ```json
  {
    "error": "DRAFT_NOT_FOUND",
    "message": "No existe borrador para esta tarea",
    "timestamp": "ISO-8601"
  }
  ```
- **Response 403**: Si el usuario no es el assignee
- **Notas**: Implicit Locking — solo el assignee puede leer borradores. TTL 72h.
- **Última actualización**: 2026-06-09

---

#### PUT /api/v1/workbox/tasks/{taskId}/draft
- **Estado**: ⚠️ Assumed
- **US**: US-017
- **CA**: CA-07, CA-14
- **Descripción**: Guardar o actualizar borrador del servidor (autoguardado)
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Path Params**:
  - `{taskId}`: string — ID de la tarea
- **Request Body**:
  ```json
  {
    "currentStep": "integer — paso actual del wizard (nullable)",
    "partialData": "object — datos parciales del formulario",
    "schemaVersion": "string — versión del esquema"
  }
  ```
- **Response 204**: No Content (guardado exitoso)
- **Response 403**: Si el usuario no es el assignee
- **Response 429**: Rate limit excedido (máx 6/min por tarea, CA-14)
- **Notas**: Rate-Limit 6 peticiones/minuto por tarea. Debounce 10s en Frontend.
- **Última actualización**: 2026-06-09

---

#### DELETE /api/v1/workbox/tasks/{taskId}/draft
- **Estado**: ⚠️ Assumed
- **US**: US-017
- **CA**: CA-07, CA-16
- **Descripción**: Eliminar borrador tras submit exitoso
- **Auth**: Bearer JWT
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{taskId}`: string — ID de la tarea
- **Response 204**: No Content (eliminado exitoso)
- **Response 403**: Si el usuario no es el assignee
- **Notas**: Se invoca automáticamente como parte del flujo POST /complete (CA-16). También puede invocarse manualmente.
- **Última actualización**: 2026-06-09

---

### 5.9 Lane Management (Gestión de Lanes BPMN + Integración RBAC)

#### GET /api/v1/admin/lanes
- **Estado**: ❌ Missing
- **US**: US-005 / US-036 (Extensión: Lane Actor Assignment + RBAC Lane Integration)
- **CA**: Extensión PO — Lane Actor Assignment
- **Descripción**: Listar los lanes de un proceso BPMN desplegado, registrados como entidades de primer nivel en `ibpms_bpmn_lane`
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Query Params**:
  - `processKey`: string (requerido) — Clave del proceso BPMN
- **Response 200**:
  ```json
  [
    {
      "id": "UUID — ID del lane en ibpms_bpmn_lane",
      "processKey": "string — clave del proceso BPMN",
      "laneXmlId": "string — ID del lane en el XML BPMN",
      "laneName": "string — nombre legible del lane",
      "actorDescription": "string | null — descripción del actor asignado",
      "linkedRoleName": "string | null — nombre del rol RBAC vinculado"
    }
  ]
  ```
- **Response 400**: Si falta el queryParam `processKey`
- **Notas**: Los lanes se registran automáticamente al desplegar un BPMN con Pool+Lanes. La tabla `ibpms_bpmn_lane` se puebla en `generarRolesDesdeLanes()`. Array vacío si el proceso no tiene lanes.
- **Última actualización**: 2026-07-14

---

#### GET /api/v1/admin/roles/{roleId}/lane-assignments
- **Estado**: ❌ Missing
- **US**: US-005 / US-036 (Extensión: RBAC Lane Integration)
- **CA**: Extensión PO — RBAC Lane Integration con granularidad I/E
- **Descripción**: Obtener las asignaciones lane↔rol para un rol específico, con granularidad Initiate/Execute por lane
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
- **Path Params**:
  - `{roleId}`: UUID — ID del rol en `ibpms_security_role`
- **Response 200**:
  ```json
  [
    {
      "laneId": "UUID — ID del lane en ibpms_bpmn_lane",
      "laneName": "string — nombre legible del lane",
      "processKey": "string — clave del proceso BPMN",
      "canInitiate": "boolean — puede iniciar tareas en este lane",
      "canExecute": "boolean — puede ejecutar tareas en este lane"
    }
  ]
  ```
- **Response 404**: Si el `roleId` no existe
- **Notas**: Consume `ibpms_lane_role_assignment` JOIN `ibpms_bpmn_lane`. Usado por el modal de edición de rol en IdentityGovernance.vue para mostrar la vista jerárquica Proceso→Lanes.
- **Última actualización**: 2026-07-14

---

#### PUT /api/v1/admin/roles/{roleId}/lane-assignments
- **Estado**: ❌ Missing
- **US**: US-005 / US-036 (Extensión: RBAC Lane Integration)
- **CA**: Extensión PO — Guardar asignaciones Lane↔Rol con I/E
- **Descripción**: Guardar o actualizar las asignaciones lane↔rol para un rol específico (reemplaza todas las asignaciones existentes del rol)
- **Auth**: Bearer JWT (requiere rol ADMIN)
- **Headers**:
  - `Authorization`: Bearer {accessToken}
  - `Content-Type`: application/json
- **Path Params**:
  - `{roleId}`: UUID — ID del rol en `ibpms_security_role`
- **Request Body**:
  ```json
  [
    {
      "laneId": "UUID — ID del lane en ibpms_bpmn_lane",
      "canInitiate": "boolean — puede iniciar tareas en este lane",
      "canExecute": "boolean — puede ejecutar tareas en este lane"
    }
  ]
  ```
- **Response 200**: Asignaciones guardadas exitosamente (sin body)
- **Response 404**: Si el `roleId` no existe
- **Response 400**: Si algún `laneId` no existe en `ibpms_bpmn_lane`
- **Notas**: Estrategia DELETE+INSERT: elimina asignaciones previas del rol y crea las nuevas. Usa constraint `UNIQUE(lane_id, role_id)` para idempotencia. El campo `assigned_by` se obtiene del JWT.
- **Última actualización**: 2026-07-14

---


## 8. Historial de Cambios del Documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0.0 | 2026-06-02 | PM-IA / Arquitecto Líder | Creación inicial con inventario assumed |
| 1.1.0 | 2026-06-09 | Arquitecto Líder | Agregar contratos US-017 CQRS (complete, draft GET/PUT/DELETE) — PM-01 Slot 5 |
| 1.2.0 | 2026-07-14 | Arquitecto Líder | Agregar 3 endpoints Lane-Role Assignment (GET lanes, GET/PUT lane-assignments) — Iteración 84-DEV-LANE-ROLE |

---

> **RECORDATORIO FINAL**: Este documento es un organismo vivo. Cada endpoint que se implementa debe reflejarse aquí. Cada endpoint que se consume debe estar aquí. Si no está aquí, no existe. La disciplina en los contratos es la base de la integración exitosa.
