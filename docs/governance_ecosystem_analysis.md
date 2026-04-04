# Análisis del Ecosistema de Gobernanza Multi-Agente (iBPMS)

Este documento centraliza el inventario, análisis y estado de salud de todos los artefactos (rules, workflows, skills y auxiliares) que rigen el comportamiento de la Inteligencia Artificial dentro del proyecto `ibpms-platform`.

---

## 1. Inventario y Clasificación de Artefactos

El ecosistema está compuesto por los siguientes pilares estructurales:

### 🏛️ Constitución Global (Rules)
| Archivo | Tipo | Objetivo Principal |
|---------|------|--------------------|
| `.cursorrules` | **Rule** | Ley suprema del proyecto. Define los límites cognitivos, prohíbe acciones destructivas (Zero-Trust), rige avatares y obliga al uso de RAG antes de codificar. |

### 📜 Políticas Descentralizadas (Workflows)
Ubicación: `scaffolding/workflows/`

| Archivo | Tipo | Objetivo Principal |
|---------|------|--------------------|
| `agent_git_governance_policy.md` | **Workflow** | Define la topología de ramas (sprints, hotfixes, po) y el ciclo de vida colaborativo en Git/GitHub. |
| `agent_governance_policy.md` | **Workflow** | Establece al Arquitecto Líder como única autoridad técnica. Regula escalamiento y el protocolo de Failover al Humano. |
| `agent_documentation_policy.md` | **Workflow** | Obliga al uso de un Monorepositorio ordenado y define la jerarquía estricta de carpetas (docs/, frontend/, rpa/, etc.). |
| `multi_agent_architecture_policy.md` | **Workflow** | Dictamina la separación estricta de memorias y contextos en chats separados, interconectados vía `.agentic-sync/`. |
| `agent_requirements_ssot_policy.md` | **Workflow** | Forzaba el respeto a SSOT documental. *(Nota: La mayor parte de su núcleo vital ya fue absorbida por la versión 2.0 del `.cursorrules`).* |

### 🛠️ Doctrinas Operativas Especializadas (Skills)
Ubicación: `.agents/skills/`

| Archivo | Tipo | Objetivo Principal |
|---------|------|--------------------|
| `backend_sre_compilation_audit/SKILL.md` | **Skill** | Instruye al backend a jamás asumir éxito estático. Obliga a usar comandos Docker para garantizar viabilidad (SRE). |
| `frontend_build_audit/SKILL.md` | **Skill** | Obliga al frontend a ejecutar compilación local (`npm run build`) para auditar linter/typescript antes del handoff. |

### 🛡️ Filtros Auxiliares (Archivos de Entorno)
| Archivo | Tipo | Objetivo Principal |
|---------|------|--------------------|
| `.cursorignore` | **Auxiliar** | Blindaje cognitivo. Filtra archivos pesados irrelvantes (`docs/requirements/future_roadmap/`, `node_modules`, `doc.json`) para optimizar el Semantic Search y evitar alucinaciones operativas de la IA (Token Bloat). |

---

## 2. Cobertura e Impacto por Agente

Estas reglas actúan de forma diferenciada según el "sombrero" o rol temporal de la IA:

*   **Arquitecto Líder (Orquestador):** Exento de la prohibición de Commits hacia Main (bajo orden humana para documentar) y único capaz de ejecutar Pull Requests y Validaciones Técnicas.
*   **Gestores (Product Owner):** Gobernado por la nueva topología para usar ramas `po/refinement/...` sin bloquear el desarrollo.
*   **Constructores (Backend / Frontend):** Afectados severamente por el modelo "Zero-Trust". No pueden commitear a main, no pueden mezclar chats y son gobernados por *Skills* de auto-compilación estricta.
*   **QA / DevOps:** Exento de autorizaciones burocráticas del Arquitecto, habilitado para interactuar directamente con el Humano para lotes de validación (Failover UAT).

---

## 3. 🚨 Hallazgos Críticos: Contradicciones y Reglas Rotas 🚨

Al auditar la última actualización impulsada a `agent_git_governance_policy.md` (Topología de ramas de Sprint), se ha detectado una **CONTRADICCIÓN CRÍTICA GRAVE** estructural entre este workflow y la constitución global.

### 💥 CONFLICTO 1: Mecanismo de Empaquetado vs Desarrollo en Ramas Paralelas (`git commit` vs `git stash`)

**Dónde Ocurre:**
*   `multi_agent_architecture_policy.md` y `.cursorrules` exigen **Git Stash**: "Subagentes no tienen permiso explícito para hacer commit... deben empaquetar mediante `git stash save`".
*   VS. `agent_git_governance_policy.md` (Sección 2 Paso 2): "El agente hace commit de su trabajo en su rama temporal. Ejecuta `git push origin agent/{mi-rol}/...`". 

**Impacto (Loop Cognitivo y Ruptura):**
Esta es una regla rota nivel rojo. Un subagente (ej. Frontend) que termine un ticket creará la rama aislada (`sprint-1/...`), pero cuando intente hacer un commit como se lo pide la política Git, su propio sistema Gatekeeper interno (del `.cursorrules`) lo bloqueará asumiendo que es una acción destructiva no autorizada (por estar entrenado para ser un perro guardián del Stash). Esto paralizará al agente.

**Recomendación de Mejora Activa:**
Evolucionar definitivamente las reglas maestras hacia la cultura de Feature-Branches. Ya que el proyecto implementó ramas (sprints), la metodología prehistórica del `stash local` quedó obsoleta.
*   **Solución:** Extraer la Ley Gatekeeper del `.cursorrules` y autorizar formalmente a los subagentes a efectuar `git commit` y `git push` siempre y cuando se encuentren parados sobre ramas que inicien con `sprint-...` o `agent/...`. El veto al commit aplicará únicamente hacia los pushes directos en `main`.

---

## 4. Otras Oportunidades de Mejora Menores

1.  **Limpieza Documental de SSOT:** `agent_requirements_ssot_policy.md` fue transcrito conceptualmente al `.cursorrules`. Se recomienda eliminar el archivo viejo para reducir las colisiones de contexto RAG.
2.  **Validación Continua de `.cursorignore`:** A medida que avancen las automatizaciones E2E, se deben agregar los reportes HTML y pantallazos pesados de `playwright` a la lista negra para salvaguardar la memoria y agilidad de la Semántica RAG.
