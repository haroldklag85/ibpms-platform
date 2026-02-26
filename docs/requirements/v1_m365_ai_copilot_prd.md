# Definición Funcional: Módulo Vertical Copiloto de Correos M365 (SaaS V1)

**Versión Objetivo:** iBPMS V2 (Expansión SaaS AI-Centric)
**Rol Análisis:** Product Owner / Strategy
**Contexto:** Este documento aborda el **Nuevo Requerimiento Funcional** para la gestión de buzones corporativos. A diferencia del Docketing Legal, este es un módulo transversal (Agnóstico de Industria) enfocado en la eficiencia de Customer Service, Mesa de Ayuda, Peticiones/Quejas (PQRs) y Operaciones Centralizadas usando Inteligencia Artificial bajo el paradigma **"Human-in-the-Loop"**.

---

## 1. El Problema que Resolvemos en el Mercado

**El Dolor Actual:** Los buzones corporativos genéricos (`contacto@empresa.com`, `pqrs@empresa.com`) son agujeros negros de productividad. Varios agentes humanos entran al mismo buzón, leen hilos de correos desordenados, pierden contexto histórico, copian y pegan respuestas de Word, y no conectan lo que leen con el CRM ni con los procesos del BPM.
**El Riesgo de la IA Autónoma:** Dejar que un LLM responda correos automáticamente a los clientes genera un riesgo legal y reputacional inaceptable para las empresas medianas y grandes (alucinaciones).

## 2. La Solución Propuesta (Copiloto AI "Human-in-the-Loop")

Un Asistente Virtual (Agente AI) conectado mediante la Graph API a Microsoft 365/Exchange Online, cuyo rol **no es reemplazar al humano, sino aumentarlo** al actuar como un preparador incansable de borradores y acciones.

### Funcionalidades Core (Especificación del Asistente):

#### A. Comprensión Semántica (Intent Detection)
*   **Lectura Activa:** El módulo monitorea constantemente los buzones configurados. Al llegar un correo, lee no solo el mensaje, sino que reconstruye el hilo ("Thread") de conversación.
*   **Clasificación de Intención y Prioridad:** Usa NLP (Procesamiento de Lenguaje Natural) para etiquetar el correo: *¿Es una queja furiosa? ¿Un seguimiento de un caso abierto? ¿Una cotización nueva?* Le asigna un peso de prioridad inicial antes de que el humano lo abra.

#### B. Motor de Generación y Memoria Transaccional
*   **Aprendizaje del Estilo Corporativo (Fine-Tuning/RAG):** El sistema analiza históricamente cómo los humanos de esa empresa han respondido correos similares en el pasado (Tono formal, informal, formatos de firma, políticas de privacidad incrustadas).
*   **Propuesta de Borradores (Bilingüe):** Detecta automáticamente si el correo entra en Inglés o Español. Crea un borrador de respuesta invisible en Outlook/iBPMS perfectamente redactado en el idioma de entrada.
*   **La Aprobación ("The Loop"):** El agente humano revisa la propuesta, la edita si hay imprecisiones, o simplemente le da a "Aprobar y Enviar", cortando el THT (Average Handling Time) de 10 minutos a 30 segundos.

#### C. Integración Omnicanal (CRM y Orquestación)
*   **Enriquecimiento Automático CRM:** Extrae el dominio o correo del remitente (Ej: `@bancoalpha.com`) e interroga por API al CRM ONS. Reúne el contexto: *"Este cliente es VIP, el gerente de cuenta es Juan"*. El LLM usa este contexto en tiempo real para adaptar el nivel de formalidad del borrador.
*   **Recomendación de Acciones (Orquestación iBPMS):** El asistente no solo sugiere texto. Analiza el correo y sugiere botones accionables en la interfaz para el humano:
    *   `[🤖 Sugerencia: Iniciar Proceso de Devolución en iBPMS]`
    *   `[🤖 Sugerencia: Escalar este correo al buzón de Soporte Técnico Nivel 2]`

#### D. Trazabilidad y Seguridad Corporativa (Microsoft Guardrails)
*   **Cumplimiento eDiscovery:** Dado que el asistente no envía nada sin confirmación, todo el tráfico, incluyendo sus sugerencias de borrador y los rechazos o ediciones del humano, queda en la capa de auditoría de Microsoft y en el log de `ibpms_audit_log` para evitar la duplicación de datos (Gobernanza corporativa).

---

## 3. Implicaciones UX/UI para V1 (Cambios en Wireframes)

Para materializar este requerimiento en la V2, deberemos inyectar este módulo directamente en la vista operativa:

**El Componente "Buzón Inteligente" (Integración en Pantalla 1 Inbox / Pantalla 2 Formulario):**
Cuando el empleado recibe una "Tarea de Lectura de Correo PQR" desde del motor Camunda, la interfaz frontal mostrará:

1.  El texto original del cliente.
2.  Un Badge flotante al lado del remitente: `[ Cliente VIP (Extraído de CRM) ]`
3.  Una ventana resaltada que dice: **"Borrador Recomendado por IA:"**
4.  Botones de acción exclusivos:
    *   `[ ✍️ Editar Borrador ]`
    *   `[ 👎 Rechazar y Aprender Motivo ]` (Este botón nutre el RAG para el futuro).
    *   `[ 🚀 Aprobar, Enviar y Terminar Tarea ]`

---
**Conclusión de Producto:**
Este módulo convierte al iBPMS en un fuerte competidor no solo de los motores BPM legados, sino de plataformas de Helpdesk modernas como Zendesk o ServiceNow, ofreciendo un ROI contundente al reducir dramáticamente la carga operativa de servicio al cliente.
