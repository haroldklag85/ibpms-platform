# Análisis de Entendimiento: US-029 — SEGUNDO PASE (Post-Remediación + Refinamiento)

**Ejecutado por:** Analista de Producto Senior / Arquitecto Funcional  
**Fecha:** 2026-04-05  
**Historia:** US-029 — Ejecución y Envío de Formulario (iForm Maestro o Simple)  
**Pantalla:** Pantalla 2 (Vista de Detalle / Formulario Dinámico)  
**Estado de desarrollo:** NO DESARROLLADA  
**CAs vigentes:** 34 (CA-01 a CA-34)  
**SSOT:** `docs/requirements/v1_user_stories.md` (líneas 1238–1579)  
**Primer pase:** `us029_functional_analysis.md` (6 GAPs detectados → 6 remediaciones CA-19 a CA-24)  
**Refinamiento:** `us029_refinamiento_respuestas.md` (45 preguntas → 10 CAs nuevos CA-25 a CA-34)

---

## 1. Resumen del Entendimiento

La US-029 es la historia de mayor peso transaccional de todo el iBPMS. Gobierna la **Pantalla 2**: el momento exacto en que un operario abre una tarea, ve el formulario correspondiente, lo llena, adjunta documentos, y presiona [Enviar] para completar su trabajo. Es el "momento de la verdad" del producto, donde todas las demás historias convergen.

En lenguaje simple: **es la pantalla donde el operario HACE su trabajo.**

Tras el primer pase de auditoría se detectaron 6 brechas críticas (GAPs), que fueron cerradas con los CA-19 a CA-24. Posteriormente, el refinamiento funcional de 45 preguntas descubrió 10 huecos adicionales que fueron cerrados con los CA-25 a CA-34. En este segundo pase, la US-029 está **materialmente completa y lista para el desarrollo**.

La historia ahora está organizada en **6 bloques funcionales**:

| Bloque | CAs | Enfoque |
|---|---|---|
| **A. Core Original** | CA-01 a CA-09 | Envío, validación, borrador, BFF, archivos, seguridad básica |
| **B. US-029.1 — Inicialización y Borradores** | CA-10 a CA-11 | BFF mejorado, autoguardado cifrado |
| **C. US-029.1 — Ejecución e Idempotencia** | CA-12 a CA-14 | Anti-doble clic, Anti-IDOR, Micro-Tokens |
| **D. US-029.1 — Validación y Persistencia** | CA-15 a CA-18 | Zod Isomórfico, CQRS, RYOW, Implicit Locking |
| **E. Remediaciones** | CA-19 a CA-24 | Reconciliación US-017, feedback envío, Wizard, delegación, draft API |
| **F. Refinamiento** | CA-25 a CA-34 | Scroll a error, pre-aviso borrador, archivos, pestañas, lectura-solo, condicionales |

---

## 2. Objetivo Principal

**Objetivo de negocio:** Permitir al operario completar su tarea de manera confiable, segura y sin pérdida de datos, garantizando que: (1) sus datos se validen antes de llegar al motor de procesos, (2) sus archivos viajen de forma segura, (3) su progreso se guarde automáticamente, (4) tenga retroalimentación visual en todo momento, y (5) el sistema lo proteja contra errores accidentales.

**Objetivo técnico:** Implementar la Pantalla 2 como un "Smart Form Runtime" que consume esquemas Zod del BFF, ejecuta validación bidireccional (Frontend + Backend), gestiona archivos vía Upload-First, y envía datos al endpoint `POST /complete` con idempotencia nativa, protección CQRS, y honestidad operativa (sin falsos positivos).

---

## 3. Alcance Funcional Definido

### ✅ DENTRO del alcance de la US-029:

| # | Funcionalidad | CAs que la cubren |
|---|---|---|
| 1 | Renderizar formularios dinámicos desde el BFF | CA-05, CA-10 |
| 2 | Validar datos en tiempo real (Frontend Zod) | CA-02, CA-15, CA-25, CA-34 |
| 3 | Enviar datos al servidor con feedback visual | CA-01, CA-20, CA-21 |
| 4 | Proteger contra doble envío (idempotencia) | CA-12 |
| 5 | Autoguardar borradores (local + servidor) | CA-03, CA-11, CA-24, CA-26, CA-31 |
| 6 | Cifrar datos sensibles en el navegador | CA-11 |
| 7 | Subir archivos con barra de progreso | CA-09, CA-13, CA-28, CA-29 |
| 8 | Navegar formularios multi-paso (Wizard) | CA-22 |
| 9 | Proteger contra envío accidental | CA-32 |
| 10 | Distinguir campos editables vs solo-lectura | CA-33 |
| 11 | Validación inteligente de campos condicionales | CA-34 |
| 12 | Detectar pestañas duplicadas | CA-30 |
| 13 | Gestionar versiones de esquema mid-flight | CA-08, CA-27 |
| 14 | Proteger asignación (solo el dueño envía) | CA-07, CA-18 |
| 15 | Gobernanza de delegación supervisor | CA-23 |
| 16 | Validación Zero-Trust Backend | CA-06, CA-14, CA-15 |
| 17 | Consistencia RYOW post-submit | CA-17 |
| 18 | Reconciliación con US-017 | CA-19 |
| 19 | Protección de Camunda (exclusión topológica) | CA-16 |
| 20 | Honestidad operativa (sin falsos positivos) | CA-04 |

### ❌ FUERA del alcance de la US-029:

| # | Funcionalidad | Dónde vive |
|---|---|---|
| 1 | Diseño de formularios (IDE) | US-003 (Pantalla 7) |
| 2 | Persistencia CQRS/Event Sourcing en Backend | US-017 |
| 3 | Reclamar tarea antes de editarla | US-002 (Pantalla 1) |
| 4 | Navegación desde el Workdesk a la tarea | US-001 (Pantalla 1) |
| 5 | Gestión de roles/permisos | US-036 (Pantalla 14) |
| 6 | Almacenamiento de archivos en bóveda | US-035 (SharePoint/SGDEA) |
| 7 | Impresión/exportación del formulario | US-010 (Generar PDF) |
| 8 | Internacionalización multi-idioma | Diferido a V2 |
| 9 | Atajos de teclado avanzados | Diferido a V2 |
| 10 | Modo completación rápida | Diferido a V2 |

---

## 4. Lista de Funcionalidades Incluidas (34 CAs)

### Bloque A — Core Original (9 CAs)
- **CA-01:** Envío exitoso de formulario → tarea marcada COMPLETED
- **CA-02:** Rechazo por datos inválidos → HTTP 400 con detalle por campo
- **CA-03:** Autoguardado local con TTL 72h y limpieza de huérfanos
- **CA-04:** Honestidad operativa: si Camunda cae → HTTP 500, jamás falso positivo
- **CA-05:** BFF Mega-DTO: una sola llamada carga esquema + layout + datos
- **CA-06:** Micro-Tokens JWT para validaciones externas (Zero-Trust)
- **CA-07:** Implicit Locking: solo el asignado puede completar
- **CA-08:** Lazy Patching: migración silenciosa entre versiones de formulario
- **CA-09:** Upload-First: archivos viajan separados, solo UUID en el POST

### Bloque B — Inicialización y Borradores (2 CAs)
- **CA-10:** BFF mejorado con `schema_version` para control generacional
- **CA-11:** Autoguardado cifrado (AES para PII) + Merge Commit al servidor

### Bloque C — Ejecución e Idempotencia (3 CAs)
- **CA-12:** Anti-doble clic con `Idempotency-Key` UUID
- **CA-13:** Anti-IDOR: UUID de archivo validado contra user + task + Anti-basura 24h
- **CA-14:** Anti-Replay: Micro-Token con `jti` invalidado en Redis post-submit

### Bloque D — Validación y Persistencia (4 CAs)
- **CA-15:** Zod Isomórfico bidireccional + RBAC per-campo + strip silencioso
- **CA-16:** Exclusión topológica de Camunda: solo DTO mínimo al motor + Saga rollback
- **CA-17:** RYOW: purga LocalStorage + Pinia ANTES de redirigir
- **CA-18:** Implicit Locking reforzado: SecurityContext vs assignee

### Bloque E — Remediaciones Post-Auditoría (6 CAs)
- **CA-19:** Política de propiedad US-029 (FE) vs US-017 (BE) — reconciliación
- **CA-20:** Feedback visual durante envío (spinner → overlay → "Guardando...")
- **CA-21:** Confirmación post-submit (✅ 3 segundos → redirect automático)
- **CA-22:** Navegación Wizard (barra de pasos + siguiente/anterior + enviar solo al final)
- **CA-23:** Delegación: supervisor debe hacer Claim antes de completar
- **CA-24:** API de borradores: `PUT /draft` + validación parcial + TTL + seguridad

### Bloque F — Refinamiento Funcional (10 CAs)
- **CA-25:** Scroll automático al primer campo con error + foco pulsante
- **CA-26:** Pre-aviso de caducidad de borrador (aviso a las 48h de 72h TTL)
- **CA-27:** Resiliencia ante cambio de versión mid-flight (HTTP 409 + recuperación)
- **CA-28:** Aduana de archivos: 25MB límite, lista blanca de tipos, validación MIME
- **CA-29:** Barra de progreso en carga de archivos + cancelar + reintentar
- **CA-30:** Detección de sesión duplicada: segunda pestaña en solo lectura
- **CA-31:** Indicador ☁️/💾 de sincronización borrador local vs servidor
- **CA-32:** Diálogo anti-envío accidental para formularios sin campos obligatorios
- **CA-33:** Distinción visual de campos de solo lectura (fondo gris + 🔒)
- **CA-34:** Validación Zod consciente de campos condicionales + `_visibleFields`

---

## 5. Brechas Residuales (GAPs)

### Estado de los 6 GAPs originales (Primer Pase):

| GAP | Estado | Cerrado por |
|---|---|---|
| GAP-1: Duplicación US-029/US-017 | ✅ CERRADO | CA-19 (Política de propiedad) |
| GAP-2: UX de Submit sin feedback | ✅ CERRADO | CA-20 (Overlay + spinner) |
| GAP-3: Post-Submit sin confirmación | ✅ CERRADO | CA-21 (✅ + redirect 3s) |
| GAP-4: Wizard sin navegación | ✅ CERRADO | CA-22 (Barra + Siguiente/Anterior) |
| GAP-5: Delegación no gobernada | ✅ CERRADO | CA-23 (Claim obligatorio) |
| GAP-6: Merge Commit sin API | ✅ CERRADO | CA-24 (PUT /draft + validación parcial) |

### Nuevos GAPs detectados en este segundo pase:

**GAP-7 (🟢 INFORMATIVO — NO bloqueante): Duplicación parcial entre CA-05 y CA-10.**

Ambos CAs describen el patrón BFF (Mega-DTO) con lenguaje ligeramente diferente. El CA-05 pertenece al bloque original y dice "inyectando en un solo Mega-DTO la triada". El CA-10 pertenece al bloque US-029.1 y dice "inyectará el Mega-DTO: [Esquema Zod Vigoroso + Layout UI + Data Histórica]". El CA-10 agrega `schema_version`, que el CA-05 no menciona.

**Riesgo:** Bajo. No genera implementación divergente porque ambos CAs apuntan al mismo endpoint (`/form-context`). El CA-10 es la versión "enriquecida" del CA-05.

**Recomendación:** NO requiere remediación en V1. En una futura normalización del documento, el CA-05 debería marcarse como "superado por CA-10" o fusionarse en uno solo.

---

**GAP-8 (🟢 INFORMATIVO — NO bloqueante): Duplicación parcial entre CA-07 y CA-18.**

Ambos CAs describen el Implicit Locking (validación de que solo el asignado puede completar). El CA-07 menciona adicionalmente `409 Conflict` como alternativa al `403 Forbidden` y menciona `{delegatedUserId}`. El CA-18 es más directo y solo menciona `403 Forbidden`.

**Riesgo:** Bajo. Ambos CAs protegen el mismo vector de ataque. La discrepancia del código HTTP (403 vs 409) es menor y se resuelve en implementación (403 = no autorizado, 409 = conflicto de estado; ambos son válidos, se estandarizará a 403).

**Recomendación:** NO requiere remediación en V1. En una futura normalización, fusionar CA-07 y CA-18 en un único CA.

---

## 6. Exclusiones Explícitas y Diferidos a V2

### Fuera de alcance (pertenecen a otras historias):
1. **Diseño de formularios** → US-003
2. **Event Sourcing / CQRS Backend** → US-017
3. **Reclamar tarea** → US-002
4. **Navegar al detalle desde el Workdesk** → US-001
5. **Gestión de roles RBAC** → US-036
6. **Bóveda documental** → US-035
7. **Generar PDF** → US-010

### Diferidos a V2 (decididos durante el refinamiento):
1. **Etiqueta "NUEVO" en campos por cambio de versión** — Valor bajo, cosmético.
2. **Saltar pasos en Wizard** — Riesgo en procesos regulados.
3. **Internacionalización (i18n)** — V1 opera en es-CO.
4. **Atajos de teclado (Ctrl+Enter, Ctrl+S)** — Nice-to-have.
5. **Modo completación rápida (sin animación 3s)** — Caso extremo de uso.

---

## 7. Observaciones de Alineación y Riesgos para Continuar

### ✅ Fortalezas Confirmadas (Post-Remediación + Refinamiento):

| # | Fortaleza | Estado |
|---|---|---|
| 1 | **Zero-Trust bidireccional** — Zod en Frontend + Backend + RBAC per-campo | ✅ Robusto (CA-15, CA-34) |
| 2 | **Upload-First blindado** — Límites + lista blanca + MIME + Anti-IDOR | ✅ Robusto (CA-09, CA-13, CA-28, CA-29) |
| 3 | **Idempotencia nativa** — UUID + Anti-doble clic Frontend + Backend | ✅ Robusto (CA-12) |
| 4 | **Autoguardado dual** — LocalStorage cifrado + servidor con sincronización visual | ✅ Robusto (CA-11, CA-24, CA-26, CA-31) |
| 5 | **Reconciliación US-029/US-017** — Política de propiedad formalizada | ✅ Cerrado (CA-19) |
| 6 | **Honestidad operativa** — Sin falsos positivos jamás | ✅ Robusto (CA-04, CA-16) |
| 7 | **UX de completación** — Feedback visual completo (pre, durante, post) | ✅ Robusto (CA-20, CA-21, CA-25) |
| 8 | **Protección de datos** — Anti-pestañas duplicadas + anti-envío accidental | ✅ Nuevo (CA-30, CA-32) |
| 9 | **Resiliencia generacional** — Versión de esquema mid-flight | ✅ Nuevo (CA-27) |
| 10 | **Campos condicionales inteligentes** — Validación contextual | ✅ Nuevo (CA-34) |

### 🟡 Riesgo residual: Complejidad de implementación

Con 34 CAs, la US-029 es la historia más grande del backlog. Esto NO es un problema funcional (cada CA aporta valor real), pero sí es un riesgo de **estimación de esfuerzo y planificación de sprint**:

- Se recomienda dividir la implementación en **3 sub-sprints** alineados con los bloques:
  - **Sprint 1:** Bloques A+B (CA-01 a CA-11): Core + BFF + Borradores → Pantalla 2 funcional básica.
  - **Sprint 2:** Bloques C+D (CA-12 a CA-18): Seguridad + CQRS + Validación → Pantalla 2 blindada.
  - **Sprint 3:** Bloques E+F (CA-19 a CA-34): UX Polish + Edge Cases → Pantalla 2 premium.

### ✅ Dependencias ya documentadas

Las 6 dependencias externas están formalizadas en el bloque `[!IMPORTANT]` de la historia:
- US-003 🔴 Bloqueante
- US-002 🔴 Bloqueante
- US-017 ⚠️ Gemela
- US-001 🟡 Fuerte
- US-036 🟡 Fuerte
- US-035 🟡 Fuerte

### ✅ Veredicto Final

> **La US-029 está funcionalmente completa y lista para desarrollo.** Los 6 GAPs del primer pase están cerrados. Los 2 GAPs residuales (duplicación CA-05/CA-10 y CA-07/CA-18) son informativos y no afectan la implementación. La historia tiene 34 CAs que cubren el ciclo completo de vida de la Pantalla 2: desde que el operario abre la tarea hasta que el motor BPMN avanza al siguiente paso.
