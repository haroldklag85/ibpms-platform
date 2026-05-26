# Protocolo Global de Conversación (Zero-Context-Bleed)

Este documento define la regla inquebrantable de gobierno para las sesiones de trabajo entre el equipo técnico (Desarrollador Humano) y el Arquitecto/Agente Autónomo de Inteligencia Artificial (Antigravity 2.0).

El objetivo es prevenir la contaminación de memoria (Context Bleed) y garantizar que la ventana de contexto del LLM mantenga una precisión del 100% sobre la tarea actual.

## REGLA DE ORO: 1 Sesión (Conversación) = 1 Historia de Usuario (US)

**Queda estrictamente prohibido cambiar el contexto de trabajo o introducir una nueva Historia de Usuario, Hotfix o Sprint dentro de una misma conversación o chat activo.**

---

## Ciclo de Vida de una Sesión de Trabajo

Para garantizar la estabilidad del proyecto y evitar decisiones basadas en memoria residual, se debe seguir este flujo:

### 1. Inicialización (Bootstrap y Prompt Maestro)
*   **Paso 1:** El usuario debe iniciar explícitamente una **Nueva Conversación** en la interfaz de chat.
*   **Paso 2:** El primer mensaje (`Prompt Maestro`) debe seguir esta estructura obligatoria:
    ```markdown
    Asume el rol de ⚙️ BACKEND - JAVA (o el rol que aplique).
    
    Lee obligatoriamente:
    1. .cursorrules
    2. .agents/skills/[Tu-Skill]/SKILL.md
    3. .agentic-sync/handoff_US-XXX.md
    
    NUEVA MISION: Iniciar el desarrollo de la US-XXX.
    ```
*   **Objetivo:** Inyectar "aire fresco" obligando al agente a repasar las reglas globales, e inyectar el cerco (Handoff) de la US específica.

### 2. Aislamiento y Mantenimiento del Alcance (Zero-Trust Boundaries)
*   El agente operará con un modelo de seguridad "Zero-Trust" en relación al código. 
*   **Regla:** El agente **NO inspeccionará ni modificará** ningún archivo `.java`, `.vue`, `.xml` o `.sql` que no esté enlistado explícitamente en el archivo `handoff_US-XXX.md`.
*   Si el desarrollo lógico dicta que se debe modificar un archivo no mapeado, el agente detendrá su ejecución y solicitará permiso expreso al usuario para ampliar el alcance.

### 3. Ejecución Constante
*   Durante la vida de la conversación, el agente mantendrá un estado cognitivo cerrado. Si el usuario pide "Un favor rápido para revisar un bug de producción" no relacionado con la US actual, el agente recomendará rechazar la tarea y abrir una nueva ventana.

### 4. Cierre Declarativo por el Humano
*   El acto de hacer un `git commit` o `git push` **NO** finaliza la sesión de la US.
*   El agente mantendrá una postura de soporte activo para resolver *Code Reviews* o fallos reportados por el entorno de QA local.
*   **Cierre Oficial:** La conversación solo debe darse por terminada cuando el usuario (Humano) indique textualmente: *"La US ha sido aprobada. Cierro la sesión"*. Solo entonces, el usuario abandonará la ventana actual para abrir una nueva conversación de cara a la siguiente US.
