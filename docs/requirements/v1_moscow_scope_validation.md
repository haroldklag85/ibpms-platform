# Validación de Alcance V1 Extendido: Matriz MoSCoW (5-6 Meses TTM)

**Objetivo:** Congelar el alcance del MVP Táctico (V1) para garantizar la salida a producción. *Nota Estratégica:* El alcance de V1 fue modificado para incluir "Agentic Workflows" (M365 Copilot) desde el día 1, extendiendo el TTM pero garantizando un impacto de mercado masivo.

---

## 🟢 MUST HAVE (Obligatorio - Sin esto no hay V1)
*Elemento innegociable. Si falta, el producto es inviable o no demuestra el "Time-to-Value".*

1.  **Core Backend & Arquitectura Hexagonal:** Implementación estricta de puertos y adaptadores. Camunda 7 debe quedar aislado sin código de negocio (Java) empotrado en sus diagramas.
2.  **Gestión de Procesos (CRUD Básico):** Orquestación de procesos secuenciales y paralelos desde diagramas BPMN pre-creados.
3.  **Bandeja de Entrada Avanzada (Docketing Inbox):** Bandeja de tareas SPA responsiva con filtros cruzados complejos (Cliente, Proyecto, Etiquetas de Actividad) para gestionar altos volúmenes operativos (Pantalla 1B).
4.  **IDE Pro-Code de Formularios (Vue 3/Zod - MUST):** Constructor Web (Low-Code/Pro-Code) basado en Mónaco Editor. Permite al usuario elegir entre crear un "iForm Maestro" (Expediente multi-etapa/rol) o un "Formulario Simple" (un solo paso). El sistema genera automáticamente archivos `.vue` nativos, validaciones `.zod`, e integra tipado TypeScript.
5.  **Motor DMN & Traductor IA (NLP a DMN):** Ejecución de tablas de decisión generadas mediante IA (Lenguaje Natural a DMN).
6.  **Copiloto AI de Correos M365 ("Human-in-the-Loop"):** Agente Virtual integrado vía Graph API que lee buzones entrantes, detecta intención, cruza con CRM, propone borradores de respuesta e incorpora *Feedback Loops (MLOps)*, *Políticas Multi-Buzón* y trazabilidad *eDiscovery* innegociable antes de la intervención humana (Pantalla 2C).
7.  **Seguridad Base (ABAC/RBAC):** Login con Azure AD (OIDC/SAML) y segregación de tareas por grupos/jerarquías en el motor.
8.  **Gestión Kanban (Operativa y Proyectos):** Soporte para gestión de proyectos ágiles (sin dependencia BPMN) y manejo de minitareas/checklists tipo Kanban dentro de actividades normales del proceso.
9.  **Dashboards Estratégicos (Process Health):** Visualización de cuellos de botella y salud de proyectos acoplando herramientas analíticas consolidadas de mercado (Grafana / PowerBI) directamente a las vistas SQL del backend.
10. **Catálogo Federado de Servicios (CRM Sync - MUST):** Conector API resistente hacia CRM externo para consultar catálogo, implementar modo degradado (Caché), soporte de sincronización (Scheduled/On-Demand) y Mapeo Variable JSON con auditoría obligatoria de versiones usadas en cada caso.
11. **Service Delivery Intelligent Intake (Plan A/B - MUST):** Envío de confirmaciones por correo que generan *"Tareas de Creación de Servicio"* sin detonar procesos huérfanos. Creación manual global protegida por rol de Administrador. Vistas segregadas: Admin (Totales), Op (Mis Tareas), y Cliente (Citas/SD).
12. **Portal de Cliente Externo (Customer Portal - MUST):** Autorreporte y visibilidad B2B/B2C. Acceso web autenticado para que los clientes externos observen el estado en tiempo real (Paso a paso) de sus Service Deliveries (Vista Táctica) y métricas acumuladas históricas con descarga de documentos finales (Vista Estratégica).
13. **Copiloto AI Diseñador (ISO 9001 & BPMN - MUST):** Agente experto conversacional empotrado exclusivamente en el Canvas de Diseño (Pantalla 6). Actúa como profesor y auditor de arquitectura, sugiriendo mejores prácticas, reportando antipatrones de diseño y evaluando cumplimiento normativo (ISO 9001) en tiempo real mediante integraciones con el LLM.

---

## 🟡 SHOULD HAVE (Debería tener - Alto Impacto, pero evitable en mes 1)
*Crítico, pero si se retrasa 30 días post-lanzamiento, el MVP sigue operando.*

1.  **Generador Documental Jurídico Básico (PDF):** Inyectar el JSON a un template `.docx`/HTML simple para generar un documento autimático.
2.  **Notificaciones Proactivas (SLA):** Alertas in-app y correos automáticos antes de que venza una tarea. *(En el MVP puro, el usuario debe revisar su bandeja proactivamente).*
3.  **Catálogo Federado (Reglas de Negocio - SHOULD):** Soporte multi-idioma nativo para los catálogos traídos del CRM, *Overrides Operativos* locales en iBPMS por sobre los campos del CRM, y exposición API Multi-tenant para portales de clientes.

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
2.  **Auditor Digital AI (Organizacional Autónomo):** La auditoría activa y transversal de toda la operación queda para V2. (*Excepción: Se promovió a V1 el "Copiloto AI Diseñador" limitado estrictamente a enseñar y auditar diagramas dentro de la Pantalla 6*).
3.  **IA Natural Flow (Core Agentic):** El motor no será agentic en la V1; la IA se usará mediante modelo conversacional API-first solo para facilitar configuraciones (como el DMN).
4.  **Zero-UI / Headless Completo:** El usuario deberá entrar a la interfaz (Workbench) al menos en el 50% de las operativas; no operaremos *solo* por WhatsApp/Chatbots aún.
5.  **Módulos Verticales Restantes (Roadmap SaaS V2):** Componentes hiper-especializados como el *RAG Documental (Legal)*, *Web Scraping Silencioso (RPA Legal)*, y el *Escaneo Integrado OCR/ICR (Hotelero)*. Estos justifican la venta de la V2. El módulo de Copiloto M365 fue promovido a V1.

---
**Validación de Criterios (Pivot Estratégico):**
- Cumple con la restricción de "5-6 Meses TTM (Ajustado por IA)".
- Protege la deuda técnica ("Hexagonal" está en Must Have).
- El producto sale al mercado inmediatamente como una plataforma "Agentic" (AI-First) gracias a la inclusión del Copiloto M365 en V1, superando a competidores legacy.
