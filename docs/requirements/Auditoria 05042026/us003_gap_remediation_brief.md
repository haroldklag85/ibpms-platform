# 📋 Sprint Remediation Brief: Cierre de GAPs — US-003

**Emisor:** Product Owner  
**Fecha:** 2026-04-05  
**Clasificación:** Directiva de Remediación Incremental  
**Origen:** [us003_functional_analysis.md](file:///c:/Users/HaroltAndrésGómezAgu/ProyectoAntigravity/ibpms-platform/docs/requirements/us003_functional_analysis.md) — Sección 5  
**Estado de la US-003:** ✅ Desarrollo base completado (estrategia iterativa por bloques de 5 CAs)

---

## 1. Contexto Ejecutivo

La US-003 ("Instanciar y Generar un Formulario iForm Maestro vs Simple") ya fue construida mediante una estrategia iterativa controlada. El código existente implementa las 86 funcionalidades base definidas en los Criterios de Aceptación.

Posterior al desarrollo, se ejecutó el workflow de análisis de entendimiento (`/analisisEntendimientoUs.md`) que identificó **7 GAPs de implementación** — vacíos de definición funcional que no representan errores de código, sino **decisiones técnicas pendientes de formalizar** y **blindajes de resiliencia faltantes**.

> [!IMPORTANT]
> **Principio rector:** Este brief NO solicita reescribir funcionalidades. Solicita **inyectar precisiones quirúrgicas** al código existente para cerrar vacíos de definición que, de no resolverse, generarán deuda técnica o incidentes en producción.

---

## 2. Protocolo de Comunicación Multi-Agente

### 2.1 Distribución de Responsabilidades

| Ticket | GAP | Agente Responsable | Prioridad | Complejidad |
|--------|-----|---------------------|-----------|-------------|
| REM-003-01 | Estrategia de Persistencia del Diseño | **Arquitecto de Software + Backend** | 🔴 Crítica | Media |
| REM-003-02 | Separación IDE vs Workdesk en código | **Frontend** | 🟡 Alta | Baja |
| REM-003-03 | Directriz QA: Vitest vs Sandbox | **Product Owner + QA** | 🟢 Media | Baja |
| REM-003-04 | Límites de Rendimiento del Canvas | **Frontend + QA** | 🟡 Alta | Media |
| REM-003-05 | Validación de Contrato con US-029 | **Arquitecto de Software** | 🔴 Crítica | Baja |
| REM-003-06 | Política de Cleanup LocalStorage | **Frontend** | 🟢 Media | Baja |
| REM-003-07 | Unificación Vista Solo-Lectura | **Frontend** | 🟢 Media | Baja |

### 2.2 Regla de Oro para Agentes

> [!CAUTION]  
> **Ninguna remediación puede alterar la firma pública** de los componentes Vue, los endpoints REST ya definidos, ni los esquemas Zod ya compilados. Toda inyección debe ser **aditiva o configurativa**, jamás destructiva sobre interfaces existentes.

---

## 3. Tickets de Remediación — Detalle Técnico

---

### REM-003-01: Estrategia de Persistencia del Diseño del Formulario
**Agente:** Arquitecto de Software + Backend  
**Prioridad:** 🔴 Crítica  
**CAs afectados:** CA-27 (Versionamiento Inmutable)

**Problema:**  
La US-003 describe con precisión cómo se diseña, valida y renderiza un formulario, pero nunca dictamina **dónde se almacena el JSON/AST del esquema visual**. El CA-27 asume versionamiento inmutable sin definir la infraestructura.

**Decisión Requerida del Arquitecto:**

| Opción | Pros | Contras |
|--------|------|---------|
| A. Tabla relacional `ibpms_form_definitions` (JSONB en PostgreSQL) | Transaccional, queries SQL simples, versionamiento por `version_id` | Blobs JSONB grandes pueden degradar performance en queries masivos |
| B. Object Storage (S3/MinIO) + tabla de metadatos | Escala ilimitada para diseños pesados, separación storage/metadata | Requiere doble-write (metadata + blob), mayor latencia en lectura |
| C. Tabla relacional para metadata + JSONB inline | Balance: metadatos indexables + contenido embebido | Complejidad intermedia |

**Entregable esperado:**  
1. Decisión documentada en el SSOT (nuevo CA o nota técnica en US-003).
2. Script Liquibase con la tabla y sus índices.
3. Endpoint REST `GET/POST /api/v1/forms/{formId}/versions` con contrato OpenAPI.

**Restricción de no-regresión:**  
No tocar los componentes Vue del IDE que ya consumen la estructura JSON en memoria. Solo se agrega la capa de persistencia por debajo.

---

### REM-003-02: Separación Explícita de Contextos IDE vs Workdesk
**Agente:** Frontend  
**Prioridad:** 🟡 Alta  
**CAs afectados:** CA-80 (Lazy Validation → Workdesk), CA-84 (Errores Mónaco → IDE)

**Problema:**  
El código agrupa lógica del IDE (diseño) y del Workdesk (operación) sin una separación arquitectónica explícita. Esto genera riesgo de que un fix para el IDE rompa la experiencia del operario o viceversa.

**Acción Requerida:**
1. Verificar que los composables/hooks de validación Zod operativa (Workdesk) estén en un módulo distinto a los composables del IDE (Mónaco).
2. Si ya están separados: documentar la convención en un comentario TSDoc en el barrel export de cada módulo.
3. Si NO están separados: refactorizar extrayendo a dos carpetas (`composables/ide/` y `composables/workdesk/`) sin cambiar interfaces públicas.

**Restricción de no-regresión:**  
Solo mover archivos y actualizar imports. Cero cambios en lógica de negocio.

---

### REM-003-03: Directriz de Uso — Auto-Vitest (CA-68) vs Sandbox In-Browser (CA-83)
**Agente:** Product Owner + QA  
**Prioridad:** 🟢 Media  
**CAs afectados:** CA-68, CA-83

**Problema:**  
Dos herramientas QA conviven dentro de la US-003 con propósitos aparentemente solapados. Los desarrolladores y testers necesitan saber cuándo usar cuál.

**Resolución (Directriz de Product Owner):**

| Herramienta | Cuándo Usarla | Quién la Usa |
|-------------|---------------|--------------|
| **Sandbox In-Browser (CA-83)** | Durante el diseño, en tiempo real, para validación rápida "quick-check" del esquema Zod antes de publicar. No genera archivos. | Arquitecto de Formularios (Pantalla 7) |
| **Auto-Vitest (CA-68)** | Al finalizar el diseño, para generar un `.spec.ts` persistente que se integra al pipeline CI/CD y asegura regresión a largo plazo. | Ingeniero QA / Pipeline CI |

**Entregable esperado:**  
Insertar una nota aclaratoria en el SSOT (`v1_user_stories.md`) entre ambos CAs que establezca esta complementariedad. No requiere cambios de código.

---

### REM-003-04: Límites de Rendimiento para Formularios de Alta Densidad
**Agente:** Frontend + QA  
**Prioridad:** 🟡 Alta  
**CAs afectados:** CA-8, CA-80

**Problema:**  
No existe un techo definido para la cantidad de campos en un iForm Maestro. Un formulario con 500+ campos y grillas anidadas en 3 niveles puede congelar el navegador del operario por peso del Virtual DOM.

**Acción Requerida:**
1. **Frontend:** Implementar una constante configurable `MAX_FORM_FIELDS = 200` en el IDE que emita una advertencia visual al superarse (no un bloqueo duro V1).
2. **Frontend:** Para formularios que superen el umbral, activar renderizado por secciones (Lazy Mount de tabs/acordeones) donde solo la pestaña activa monta su DOM.
3. **QA:** Crear un test de carga con un formulario de 250 campos + 3 grillas anidadas y medir Time-to-Interactive (TTI).

**Restricción de no-regresión:**  
La constante es un warning, no un bloqueo. Ningún formulario existente se rompe.

---

### REM-003-05: Validación de Contrato de Integración con US-029
**Agente:** Arquitecto de Software  
**Prioridad:** 🔴 Crítica  
**CAs afectados:** CA-13, CA-14, CA-24, CA-82

**Problema:**  
La US-003 delega la persistencia de borradores, el I/O Mapping y los Smart Buttons a la US-029 (Persistencia CQRS). Si esa historia tiene contratos incompletos, la US-003 queda funcionalmente huérfana en runtime.

**Acción Requerida:**
1. Verificar que la US-029 expone los siguientes endpoints o interfaces:
   - `POST /api/v1/drafts/{taskId}` (Auto-Guardado)
   - `GET /api/v1/drafts/{taskId}` (Recuperación de borrador)
   - `POST /api/v1/tasks/{taskId}/complete` (Smart Button Completar)
   - `DELETE /api/v1/drafts/{taskId}` (Limpieza post-submit)
2. Si alguno no existe: generar el ticket de desarrollo como dependencia bloqueante.
3. Si todos existen: documentar el contrato de integración en un ADR (Architecture Decision Record).

**Restricción de no-regresión:**  
Solo verificación y documentación. Cero modificaciones a código existente.

---

### REM-003-06: Política de Expiración y Limpieza de LocalStorage
**Agente:** Frontend  
**Prioridad:** 🟢 Media  
**CAs afectados:** CA-24, CA-72, CA-82, CA-85

**Problema:**  
Múltiples CAs inyectan datos al LocalStorage (borradores Workdesk, borradores IDE, snapshots JSON) sin definir cuándo se purgan.

**Acción Requerida:**
1. Implementar un servicio `LocalStorageGarbageCollector` que se ejecute al iniciar la SPA (`App.vue` → `onMounted`).
2. Regla de expiración: eliminar entradas con `timestamp` > 7 días.
3. Regla de cuota: si el total de entradas supera 50MB (estimado por `JSON.stringify().length`), purgar las más antiguas primero (FIFO).
4. Log discreto en consola: `[GC] Purged N stale drafts (X KB freed)`.

**Restricción de no-regresión:**  
Operar solo sobre keys con prefijo `ibpms_draft_` o `ibpms_snapshot_`. No tocar otros datos de LocalStorage.

---

### REM-003-07: Unificación de Componentes de Solo-Lectura
**Agente:** Frontend  
**Prioridad:** 🟢 Media  
**CAs afectados:** CA-37 (Visor Histórico), CA-56 (Vista Imprimible)

**Problema:**  
Dos CAs prometen un formulario en modo lectura para audiencias distintas (Auditor vs Visualizador), generando riesgo de duplicidad de componentes Vue.

**Acción Requerida:**
1. Verificar si existen dos componentes o uno solo con props.
2. **Ideal:** Un único componente `<FormReadOnlyView :mode="audit|print" />` con:
   - `mode="audit"`: Muestra metadatos de auditoría (quién, cuándo, qué cambió).
   - `mode="print"`: Renderiza formato "documento físico" limpio sin bordes de input.
3. Si ya hay dos componentes separados: evaluar si compartir un base composable reduce código. No forzar merge si la complejidad no lo justifica.

**Restricción de no-regresión:**  
Si se unifica, exponer la misma interfaz de props que los componentes originales para no romper rutas de navegación existentes.

---

## 4. Cronograma Propuesto

| Fase | Tickets | Duración Estimada |
|------|---------|-------------------|
| **Sprint N (Inmediato)** | REM-003-01, REM-003-05 | 3-5 días |
| **Sprint N+1** | REM-003-02, REM-003-04, REM-003-06 | 3-4 días |
| **Sprint N+2 (Estabilización)** | REM-003-03, REM-003-07 | 1-2 días |

---

## 5. Protocolo de Cierre

Cada ticket REM se considera **cerrado** cuando:

1. ✅ La acción técnica está implementada o documentada.
2. ✅ No se introdujeron regresiones (build verde, tests existentes pasan).
3. ✅ El Product Owner valida que el GAP original ya no aplica.
4. ✅ Se actualiza el `us003_functional_analysis.md` marcando el GAP como `[CERRADO]`.

---

> [!TIP]
> **Para los Agentes:** Este documento es su fuente de verdad para las remediaciones de la US-003. No inventen alcance adicional. Si durante la implementación descubren un vacío nuevo no contemplado aquí, **repórtenlo al Product Owner** antes de actuar.
