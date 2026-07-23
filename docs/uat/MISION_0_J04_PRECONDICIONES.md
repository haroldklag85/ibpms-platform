# Misión 0: Prerrequisitos de Infraestructura y Data Seed

**Parámetros:** Sprint 6 | Iteración 6.2 | Journey: J-04 (Workdesk Operario)
**Objetivo:** Preparar el entorno y generar las 4 instancias base del proceso de siniestros requeridas para que el Operario pueda probar su bandeja unificada.

> **Regla de Evidencia:** En los pasos marcados con [📸], toma una captura de pantalla. En pasos con [📋], extrae el log (consola o BD).
> **Fallo:** Si algo falla, regístralo en `docs/sprints/sprint_6_bugs.md` y continúa.

---

## 🛠 PARTE A: Infraestructura (Consola Local)

### Paso 0.1: Levantar entorno E2E
1. Abre tu terminal de sistema en la raíz del proyecto (`ibpms-platform`).
2. Ejecuta: `docker compose -f docker-compose.e2e.yml up -d`
3. Monitorea que PostgreSQL, Redis, Camunda y RabbitMQ estén "Healthy".
4. Levanta el servidor frontend: `cd frontend && npm run dev`.

### Paso 0.2: Validar usuarios base (Fixture)
1. Verifica mediante BD (o asegurate en el `.sql` local) que existan los siguientes usuarios con password de prueba:
   - `analista_n1` (Roles: Operario, Adapters)
   - `perito_a`, `perito_b`
   - `director_1` (Roles: Supervisor)

---

## 🏗 PARTE B: Data Seed (Simulación parcial J-02)
Necesitamos poblar el Workdesk con tareas "reales" generadas por el BPMN. Para ello, como usuario *Arquitecto*:

### Paso 0.3: Login Arquitecto / Super Admin
1. Abre Navegador 1 (Chrome preferiblemente) en `http://localhost:5174/login`.
2. Autentícate con el perfil de `admin` o `arquitecto`.
3. `[📸 Captura]` del dashboard inicial confirmando inicio de sesión exitoso.

### Paso 0.4: Instanciar flujo 4 veces (Caso 1) - Tarea Normal
1. Ve al Gestor de Instancias y lanza el proceso `insurance_claims_complex` (Reclamo de Siniestros).
2. Llena el formulario de inicio con: `tipoSiniestro="Incendio"`, `monto=150000`.
3. Dale "Iniciar". (Esto forzará a que el DMN enrute hacia "Task_ManualReview").

### Paso 0.5: Instanciar flujo 4 veces (Casos 2, 3 y 4)
1. Repite el **Paso 0.4** tres veces más para tener al menos **4** tareas "Auditar Información Siniestro" generadas.
2. `[📋 Log/Consola]` Si ocurre algún error HTTP 500 al instanciar alguna de ellas, captura la pestaña `Network` del navegador y regístralo.

### Paso 0.6: Tablero Kanban (Datos Tontos)
1. Ve a `/workdesk` y filtra por "Proyectos (Kanban)".
2. Crea manualmente **3 tarjetas genéricas** llamadas "Tarea Manual 1", "Tarea Manual 2", "Tarea Manual 3" en la columna TODO.
3. `[📸 Captura]` del Kanban con las tarjetas.

---

## ✅ CONFIRMACIÓN PARA EL AGENTE QA (Fin Misión 0)
Cuando el Humano termine esta misión, debe responderle al Agente QA indicando:
> *"Misión 0 completada. Generé 4 instancias de BPMN y 3 en Kanban. No encontré bugs (o encontré X bugs registrados en el documento)."*

**Siguiente paso:** Iniciar Misión 1 (Fase 1: Bandeja Unificada).
