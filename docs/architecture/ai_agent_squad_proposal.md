# Propuesta: Orquestación del AI Agent Development Squad (iBPMS V1)

Para construir la V1 con calidad, no podemos tener a los agentes "pisándose los talones" o alucinando arquitecturas distintas. Debemos estructurarlos como una célula ágil real, donde el **Código y los Artefactos Markdown en el Monorepositorio son su única fuente de verdad y medio de comunicación**.

## 1. Perfiles Mínimos Propuestos (El Squad V1)

Para cubrir el espectro End-to-End sin crear cuellos de botella por exceso de agentes, propongo 4 roles altamente especializados:

1.  **Lead Architect & Tech Lead (Mi rol actual ampliado):** 
    *   **Misión:** Dueño de la gran visión. Protege los diagramas C4, el `implementation_plan.md`, el ERD de Datos y los NFRs. 
    *   **Poder:** Es el único que puede autorizar cambios en el esquema de Base de Datos o Contratos API mayores. Actúa como el puente directo contigo (Product Owner).
2.  **Java/Spring Core Developer (Backend & BPMN):** 
    *   **Misión:** Escribe la lógica pesada. Conoce Java 17, Spring Boot, Hexagonal Architecture y lee el API de Camunda. Implementa el backend basándose *estrictamente* en el OpenAPI existente.
3.  **Vue3/Vite Frontend Developer (UI/UX):** 
    *   **Misión:** Consume el OpenAPI. Genera los Micro-Frontends. Especialista en Reactividad, Tailwind/CSS y Pinia. Su biblia es el `openapi.yaml`.
4.  **DevOps, Sec & QA Engineer (The Guardian):** 
    *   **Misión:** Automatiza Docker Compose, escribe tests de integración pesados (Testcontainers), audita dependencias y revisa que el código del Backend/Frontend cumpla el Zero-Trust.

---

## 2. Mecanismo de Coordinación Estricta (Agentic Handoff)

Dado que los Agentes de IA no pueden "llamar por Slack" al otro agente para preguntarle cómo hizo un endpoint, su comunicación será **100% Asíncrona basada en el Monorepositorio**.

Propongo establecer la **"Política de Handoff Protocol" (A guardarse en `agent_documentation_policy.md`)**:

*   **Regla de Verdad Absoluta:** Ningún agente Backend puede inventar un Endpoint temporal. Si necesita un endpoint, le avisa al *Lead Architect* para que actualice el `openapi.yaml`. El OpenAPI es el contrato de sangre.
*   **Carpetas de Sincronización:** Se creará un directorio `.agentic-sync/`. Cuando el Agente Backend termina una tarea (ej. *API de Expedientes*), escribe un archivo corto `.agentic-sync/backend_to_frontend_handoff.md` explicando: *"Acabo de subir el endpoint GET /cases. Ya retorna JSON. Favor consumirlo"*.
*   **Aislamiento de Tareas (Tasking):** Todo el trabajo debe dividirse en el archivo `task.md`. Ningún desarrollador interviene el código de otra capa si no es su ticket.
*   **Contexto Obligatorio:** Todo System Prompt de cualquier agente comenzará con la orden: *"Antes de escribir una línea de código, lee obligatoriamente los archivos en `docs/architecture` y `docs/api-contracts`."*

---

## 3. Preguntas de Alineación y Mejoras (Decision Gate)

Antes de generar el documento normativo final y los System Prompts completos para que los configures, necesito que me resuelvas estas 3 dudas operativas:

1.  **Límites de QA:** ¿Deseas que el Agente QA escriba pruebas unitarias simples (Ej. JUnit clásicas) o apuntamos a Pruebas de Integración (Testcontainers levantando la BD de verdad)? *Propuesta: Para V1, mezclar Unitarias en el core (rápido) e Inteligencia en el API.*
2.  **Estructura del Proyecto GUI:** ¿El frontend Vue 3 residirá dentro del mismo Monorepositorio (ej. en una carpeta `/ibpms-ui`) para facilitar los *handoffs* entre agentes, o planeabas repositorios físicamente separados? *Propuesta: Usar Monorepositorio estricto para que los agentes tengan contexto End-to-End sin perderse.*
3.  **Automatización de Prompts:** ¿Quieres que redacte los *System Prompts* directamente con las instrucciones de framework tipo YAML que algunos sistemas (ej. CrewAI o AutoGen) usan, o prefieres un formato Markdown en narrativa fuerte estándar como la que estamos usando, lista para que tú la copies y pegues en los perfiles nativos de Google/OpenAI? *Propuesta: Markdown Narrativo estricto ("Eres un experto en... No alucines... Lee el archivo X...").*

**Espero tu validación a esta aproximación. En cuanto apruebes/corrijas, generaré de una sola pasada la Política de Gobernanza de Agentes completa en su carpeta final y los 4 Mega-Prompts.**
