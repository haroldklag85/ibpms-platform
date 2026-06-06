# Handoff Técnico: Sprint 5 - Iteración 1 (Transaccionalidad Base)

## 📌 Metadatos del Handoff
- **Agente Destino:** Desarrollador Backend (Java/Spring Boot)
- **Autor:** Arquitecto Líder (Antigravity)
- **Historias de Usuario contenidas:** US-002 (CAs 01-10), US-029 (CAs 01-10), US-007 (CAs 01-08).
- **Riesgo:** Crítico (Bases de Datos, Deadlocks, Seguridad Transaccional).

## 🎯 Objetivo de la Iteración
Asegurar la capa base (cimientos) de las nuevas integraciones antes de exponer los endpoints REST al Frontend. Esto implica asegurar la atomicidad de la base de datos para la adjudicación de tareas, la verificación dura del *Owner* de un formulario (Zero-Trust), y definir la estructura JSON/XML para el DMN.

## ⚙️ Workflows de Gobernanza Obligatorios
El Arquitecto Líder **EXIGE** que el Agente Backend, antes y durante el desarrollo, acate los siguientes workflows para garantizar la auditoría y cierre perimetral:

1. **`cierreDeudaTecCriteriosAceptacion.md`**: El código escrito **debe** llevar trazabilidad al CA exacto que resuelve. Ningún cambio se debe hacer "por intuición". 
2. **`reconciliacionCoberturaCa.md`**: Verifica que todo CA transaccional de este scope esté cubierto y documentado sin huecos logísticos.
3. **`router_certificacion_qa.md`**: Todo Entity, Repository y Servicio modificado en esta iteración base REQUIERE pruebas de integración `@DataJpaTest` o `@SpringBootTest` limpias.

*Skills activadas requeridas:* `@Skill: java_spring_boot_hexagonal`, `@Skill: qa_testcontainers`.

---

## 🛠️ Acciones Tácticas Requeridas (Scope Backend)

### 1. Concurrencia Atómica en Claiming (US-002: CA-01 al CA-10)
**Contexto:** Evitar condiciones de carrera si dos operarios presionan "Reclamar" sobre la misma tarea en el mismo milisegundo.
- **Acción (Persistencia JPA):** Implementar bloqueos pesados de DB. Usa `SELECT ... FOR UPDATE SKIP LOCKED` o `@Lock(LockModeType.PESSIMISTIC_WRITE)` en los repositorios asociados a las tareas del Workdesk.
- **Validación QA:** Crear un Test de Integración que despache dos hilos transaccionales simultáneos simulando el robo concurrente de una tarea, garantizando que el hilo B lance `ObjectOptimisticLockingFailureException` (o análogo) y el hilo A retenga su adjudicación intacta.

### 2. Implicit Locking & Seguridad del Submit (US-029: CA-01 al CA-10)
**Contexto:** Seguridad Zero-Trust. El Backend no confía en la UI.
- **Acción (Domain/Application):** Establecer las verificaciones de estado ANTES del guardado del formulario. Si el JSON entrante (`POST /submit`) dice ser de `User-X`, el Backend debe contrastarlo con el `assignee` registrado en la BD. Si no coindice -> Lanzar capa de Excepción controlada (HTTP 403 Forbidden delegado por el Controller Handler).
- **Validación QA:** Escribir un caso Unitario/Integración donde un token válido de `User-Y` intenta someter un JSON para una tarea cuyo dueño actual en BD es `User-X`, confirmando el rechazo unánime.

### 3. Esqueletos Core DMN & NLP (US-007: CA-01 al CA-08)
**Contexto:** Antes de conectar Vertex o Azure OpenAI, necesitamos fijar el modelo de entrada/salida.
- **Acción (Modelo Hexagonal):** Construir los puertos de salida (Interfaces) para el servicio generador de IA. 
- Crear las clases Record/DTO que parsearán el `Prompt JSON Request` y prever la envoltura para la String devuelta (que representará el DMN XML Payload).
- Implementar un adaptador o clase Mock en memoria temporal (Ej. `MockNlpDmnAdapter.java`) para pruebas de la Interfaz.

---

## 🛑 Condición para Cierre de Iteración
No puedes dar por finalizada esta tanda de CAs si `mvn clean verify` no corre al 100% verde (incluyendo Testcontainers sobre tu DB Locking de US-002). Tras ello, notifica al Desarrollador Frontend o retorna al Arquitecto.
