# Validación de Alcance V1 Extendido: Matriz MoSCoW (5-6 Meses TTM)

**Objetivo:** Congelar el alcance del MVP Táctico (V1) para garantizar la salida a producción. *Nota Estratégica:* El alcance de V1 fue modificado para incluir "Agentic Workflows" (M365 Copilot) desde el día 1, extendiendo el TTM pero garantizando un impacto de mercado masivo.

---

## 🟢 MUST HAVE (Obligatorio - Sin esto no hay V1)
*Elemento innegociable. Si falta, el producto es inviable o no demuestra el "Time-to-Value".*

1.  **Core Backend & Arquitectura Hexagonal:** Implementación estricta de puertos y adaptadores. Camunda 7 debe quedar aislado sin código de negocio (Java) empotrado en sus diagramas.
2.  **Gestión de Procesos (CRUD Básico):** Orquestación de procesos secuenciales y paralelos desde diagramas BPMN pre-creados.
3.  **Bandeja de Entrada Avanzada (Docketing Inbox):** Bandeja de tareas SPA responsiva con filtros cruzados complejos (Cliente, Proyecto, Etiquetas de Actividad) para gestionar altos volúmenes operativos (Pantalla 1B).
4.  **Formularios Dinámicos Nativos (Data to JSON):** Formularios generados dinámicamente que convierten el input humano en un Payload JSON estructurado.
5.  **Motor DMN & Traductor IA (NLP a DMN):** Ejecución de tablas de decisión generadas mediante IA (Lenguaje Natural a DMN).
6.  **Copiloto AI de Correos M365 ("Human-in-the-Loop"):** Agente Virtual integrado vía Graph API que lee buzones entrantes, detecta intención, cruza con CRM, propone borradores de respuesta y sugiere creación de tareas atómicas antes de la intervención humana (Pantalla 2C).
7.  **Seguridad Base (ABAC/RBAC):** Login con Azure AD (OIDC/SAML) y segregación de tareas por grupos/jerarquías en el motor.
8.  **Gestión Kanban (Operativa y Proyectos):** Soporte para gestión de proyectos ágiles (sin dependencia BPMN) y manejo de minitareas/checklists tipo Kanban dentro de actividades normales del proceso.
9.  **Dashboards Estratégicos (Process Health):** Visualización de cuellos de botella y salud de proyectos acoplando herramientas analíticas consolidadas de mercado (Grafana / PowerBI) directamente a las vistas SQL del backend.

---

## 🟡 SHOULD HAVE (Debería tener - Alto Impacto, pero evitable en mes 1)
*Crítico, pero si se retrasa 30 días post-lanzamiento, el MVP sigue operando.*

1.  **Generador Documental Jurídico Básico (PDF):** Inyectar el JSON a un template `.docx`/HTML simple para generar un documento autimático.
2.  **Notificaciones Proactivas (SLA):** Alertas in-app y correos automáticos antes de que venza una tarea. *(En el MVP puro, el usuario debe revisar su bandeja proactivamente).*

---

## 🟠 COULD HAVE (Podría tener - Funciones "Nice to Have")
*Se evaluará en el Mes 3 solo si el backend y frontend core están 100% estables.*

1.  **Tableros Históricos de Proyecto Complejos:** Audit Log visual detallado tipo *Timeline* amigable para el usuario.
2.  **Integración Síncrona Compleja con ERP (Sagas):** APIs bidireccionales complejas con sistemas legados. *(En el MVP se puede iniciar con APIs REST asíncronas simples o Webhooks de un solo sentido).*
3.  **Formularios Dinámicos Complejos:** Componentes UI muy avanzados (cálculos en vivo, sub-formularios anidados multinivel).

---

## 🔴 WON'T HAVE (Excluido de la V1 - Pertenece a la V2)
*Funcionalidades prohibidas en el scope de 3 meses. Intentar incluirlas pondrá en riesgo el lanzamiento.*

1.  **Consultor Digital AI (Machine Learning):** Nada de análisis predictivo ni rediseño autónomo de procesos en V1. Todo se abordará en V2.
2.  **Auditor Digital AI (ISO 9001):** La auditoría activa en V1 se limitará a Bitácoras Inmutables pasivas.
3.  **IA Natural Flow (Core Agentic):** El motor no será agentic en la V1; la IA se usará mediante modelo conversacional API-first solo para facilitar configuraciones (como el DMN).
4.  **Zero-UI / Headless Completo:** El usuario deberá entrar a la interfaz (Workbench) al menos en el 50% de las operativas; no operaremos *solo* por WhatsApp/Chatbots aún.
5.  **Módulos Verticales Restantes (Roadmap SaaS V2):** Componentes hiper-especializados como el *RAG Documental (Legal)*, *Web Scraping Silencioso (RPA Legal)*, y el *Escaneo Integrado OCR/ICR (Hotelero)*. Estos justifican la venta de la V2. El módulo de Copiloto M365 fue promovido a V1.

---
**Validación de Criterios (Pivot Estratégico):**
- Cumple con la restricción de "5-6 Meses TTM (Ajustado por IA)".
- Protege la deuda técnica ("Hexagonal" está en Must Have).
- El producto sale al mercado inmediatamente como una plataforma "Agentic" (AI-First) gracias a la inclusión del Copiloto M365 en V1, superando a competidores legacy.
