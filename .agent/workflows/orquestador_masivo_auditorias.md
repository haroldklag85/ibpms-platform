---
description: Orquestador automático para ejecutar iteraciones masivas de auditorías de trazabilidad Top-Down sobre un bloque de Historias de Usuario.
---

> **[ATENCIÓN AGENTE]:** Este es un orquestador de larga duración (Long-Running Loop). No debes detenerte hasta procesar la lista completa de US. Eres resiliente a fallas y usarás `task.md` como estado de memoria.

Actúas como un **Master Orchestrator Agent**. Tu trabajo es recorrer secuencialmente una lista de Historias de Usuario, identificar sus Criterios de Aceptación (CAs) e invocar recursivamente el workflow de auditoría para cada uno de ellos.

## 📋 Lista de Objetivos (Procesar Secuencialmente)
US-000, US-001, US-002, US-003, US-004, US-005, US-007, US-017, US-025, US-028, US-029, US-030, US-034, US-036, US-038, US-039, US-043, US-048.

---

## ⚙️ MECÁNICA DEL BUCLE (LOOP)

Por cada `US-XXX` en la lista, ejecuta este flujo estricto:

### 1. Sistema de Checkpoint (Memoria)
* Revisa el archivo `task.md`.
* Si ya existe la sección `### Tareas Pendientes US-XXX`, asume que esta US ya fue auditada en una ejecución anterior que colapsó. **Sáltala y pasa a la siguiente US.**

### 2. Descubrimiento de CAs
* Lee `docs/requirements/v1_user_stories_index.md` y compáralo contra `.agentic-sync/coverage_matrix.md`.
* Determina el número exacto total de Criterios de Aceptación para esta US.

### 3. Bucle Interno (CA-1 hasta CA-Total)
Por cada `CA-YY`, haz lo siguiente:

* **Pausa Obligatoria:** Aplica siempre un tiempo de espera de **4 segundos** (sleep) antes de iniciar un nuevo CA para evitar bloqueos del disco/API y mitigar rate limits.
* **Filtro N/A:** Revisa en la `coverage_matrix.md` si el CA-YY está clasificado como "N/A" o "Fuera de alcance". 
  * Si es así, **NO ejecutes la auditoría**. Simplemente asegúrate de que esté indicado como "N/A" en la matriz y pasa al siguiente CA.
* **Invocación de Auditoría:** Si es un CA válido, carga internamente el siguiente contexto y ejecútalo de principio a fin:
  > *"Arquitecto líder. Por favor, asume el rol y las reglas del archivo `.agent/workflows/auditoria_trazabilidad_topdown.md`. Quiero que ejecutes esa auditoría paso a paso y de forma autónoma para la **US-XXX**, abarcando el **CA-YY**. Como dicta el workflow, tu única fuente de verdad debe ser el archivo de índices de requerimientos."*

### 4. Resiliencia y Fallos (Try-Catch)
* Si encuentras un error (ej. timeout, falla al leer el código, confusión de contexto), tienes permitidos **4 REINTENTOS MÁXIMOS** para ese CA específico.
* Si fallas 4 veces consecutivas, abandona el CA, anota lo siguiente en el `task.md` y salta al siguiente CA:
  `- [ ] 🚨 RE-AUDITAR MANUALMENTE US-XXX CA-YY (Agotados los 4 reintentos del agente)`

### 5. Cierre de Bucle y Entregables
Al terminar todos los CAs de una `US-XXX`, debes ejecutar dos acciones de guardado antes de saltar a la siguiente US:

1. **Generar Reporte Individual:** Crea el archivo `Reporte_Auditoria_US_XXX.md` con los hallazgos. **Nota importante:** Debes guardar este archivo en tu propio directorio interno (el `brain/` de la conversación actual), NO en la raíz del proyecto.
2. **Actualizar el `task.md` (Append Only):**
   Añade al final del archivo `task.md` una nueva sección que consolide las tareas derivadas de la auditoría. Deben estar divididas obligatoriamente en estas capas:
   
   ```markdown
   ### Tareas Pendientes US-XXX
   
   **Backend:**
   - [ ] CA-01: [Descripción de lo que falta en el back]
   
   **Frontend:**
   - [ ] CA-02: [Descripción de lo que falta en la UI o componentes Vue]

   **Infra/DB:**
   - [ ] CA-03: [Descripción de tareas de bases de datos, migraciones Liquibase o CI/CD]
   ```
*(Asegúrate de NO sobreescribir ni borrar tareas de otras US).*

---
**[INICIADOR]:** Al recibir este prompt, no me preguntes por dónde empezar. Inicia inmediatamente el Bucle de Ejecución revisando el `task.md` para la US-000.
