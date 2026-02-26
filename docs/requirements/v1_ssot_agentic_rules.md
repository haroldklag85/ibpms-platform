# Reglas de Alineación Arquitectónica y Alcance (Agentic & Human Team)

**PARA:** Arquitecto de Software Líder, Agentes IA de Desarrollo (Frontend & Backend), y Equipo de QA.
**DE:** Product Owner / Product Manager
**ASUNTO:** Única Fuente de Verdad (Single Source of Truth - SSOT) para el MVP V1 (AI-First) de iBPMS.

---

## 🛑 REGLA DE ORO (MANDATORY INSTRUCTION FOR ALL AGENTS)
**Antes de escribir o modificar cualquier línea de código, arquitectura, base de datos o componente UI, el Agente y/o Desarrollador DEBE haber leído y comprendido los documentos listados abajo.** 

Cualquier desviación del alcance definido en estos documentos es considerada una **violación del MVP**. No se aceptan alucinaciones de código, no se deben crear módulos V2 (Scraping, OCR) y todo el desarrollo debe regirse estrictamente por los Criterios de Aceptación (BDD/Gherkin) de la V1.

---

## 📚 ÍNDICE OBLIGATORIO DE LECTURA (SSOT)

El líder de arquitectura debe garantizar que los Agentes lean estos archivos exactos en este orden:

### 1. ALINEACIÓN ESTRATÉGICA Y LÍMITES (Contexto General)
*Define QUÉ se construye y POR QUÉ.*
*   `docs/requirements/v1_moscow_scope_validation.md`: Contiene la matriz MoSCoW. Refleja el Pivot Estratégico donde "Copiloto M365 y Docketing" son **MUST HAVE** para la V1. (RRHH y OCR están fuera).
*   `docs/guides/core_concepts.md`: Glosario de dominio (Tenant, Definition, Instance).

### 2. CONTRATO FUNCIONAL Y PRUEBAS (QA & Desarrollo)
*Define EL COMPORTAMIENTO EXACTO esperado (Contratos).*
*   `docs/requirements/v1_user_stories.md`: **El documento más crítico.** Contiene los escenarios *Given/When/Then*. Define explícitamente parámetros de paginación (`?page=1&size=50`), estructuras precisas de error (`ValidationFailed`), validaciones de base de datos inter-tablas, y el flujo exacto de botones para la Épica 9 (IA).
*   `docs/requirements/v2_m365_ai_copilot_prd.md`: PRD técnico para la intención de IA y borradores bilingües.

### 3. ARQUITECTURA TÉCNICA Y DATOS (Líder Backend)
*Define CÓMO se estructura el motor internamente.*
*   `docs/architecture/c4-model.md`: Diagramas C4. Exige Arquitectura Hexagonal estricta.
*   `docs/architecture/ibpms_core_erd.md`: Entidad-Relación. Base estricta para las migraciones Flyway/Liquibase y los Repositorios JPA.
*   `docs/architecture/ui_components_schema.md`: Reglas del JSON Schema para asegurar que el Backend controle la renderización de los formularios dinámicos.

### 4. DISEÑO UI/UX Y LAYOUTS (Líder Frontend Vue 3)
*Define la PRESENTACIÓN Y EXPERIENCIA DE USUARIO.*
*   `docs/architecture/v1_wireframes.md`: Mapas conceptuales de baja fidelidad. Presta especial atención a la "Pantalla 2C" que rige la vista dividida (Human-in-the-Loop) del Copiloto.
*   Prototipos de Referencia HTML: (`UI1.html`, `UI2.html`, `UI3.html`, `UI4.html`). El diseño de Tailwind CSS debe extraerse y estandarizarse a partir del layout horizontal colapsable de UI4.

---

## ⚙️ INSTRUCCIONES DE EJECUCIÓN DEL HANDOFF
1.  **Backend Agents:** Centrarse 100% en Arquitectura Hexagonal y cumplir los contratos de la API y los payloads de las *User Stories*.
2.  **Frontend Agents (Vue 3/Vite):** Utilizar Pinia, enrutamiento estándar, Tailwind CSS y basarse en los *Wireframes* conceptuales renderizando de forma agnóstica basándose en los `ui_components_schema.md`.
3.  **QA:** Todos los tests de integración (`Testcontainers`, `Playwright`) deben transcribirse 1:1 desde los escenarios Gherkin de las *User Stories*.

**ESTE DOCUMENTO ES LEY PARA LA ITERACIÓN V1.**
