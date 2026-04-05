---
description: Generar la siguiente fase/iteración para el Mapa de Ruta de Auditoría Técnica de IBPMS V1
---

## 🎯 Objetivo de este Workflow
Este flujo de trabajo garantiza que el Agente Arquitecto Líder (Antigravity) extienda iterativamente el documento `technical_audit_roadmap.md` sin "alucinaciones" (amnesia de contexto) ni pérdida de instrucciones arquitectónicas originarias. Siempre analizará estrictamente las Historias de Usuario faltantes y aplicará la política de Control de Calidad Gatekeeper.

## 🧠 Instrucciones Obligatorias para el Agente (Prompt Inmortalizado)

Cuando el Arquitecto Humano te ordene ejecutar este workflow (Ej: "Genera la siguiente iteración de auditoría"), DEBES ejecutar rigurosamente estos pasos:

1. **Leer Documentos Core:**
   - Lee el documento de Historias de Usuario completo: `ibpms-platform/docs/requirements/v1_user_stories.md`.
   - Lee el progreso actual en `technical_audit_roadmap.md` (o archivo centralizado que estés usando) para saber en qué Iteración numérica nos quedamos.

2. **Identificar el Sub-Dominio Desbloqueado (Fail-Fast Rule):**
   - Determina el siguiente bloque de dependencias lógicas a atacar entre las ~40 US restantes, recordando que nunca debes agrupar al azar.
   - *Dominio Sugerido 3 (Intake):* Buzones, O365 Listener, Formularios Externos genéricos, Docketing.
   - *Dominio Sugerido 4 (Gobernanza):* SLA globales, Dashboards, BAM, RBAC, Control de límites.
   - *Dominio Sugerido 5 (Inteligencia Satelital):* Copiloto DMN, Sugerencias NLP de tareas, Copiloto ISO 9001.

3. **Estructurar la Nueva Iteración (N+1):**
   Para cada bloque seleccionado, construye una nueva *ITERACIÓN PENDIENTE* para inyectarla en la hoja de ruta usando esta estructura estricta:
   - Título de Iteración y Justificación de Negocio.
   - Lista detallada de Historias de Usuario (Ej. US-016, US-011).
   - Componentes previstos (Store, Controllers, Integraciones externas).
   - Mayor riesgo técnico predecible o de deuda.

4. **Aplicar el Método de Auditoría (Gatekeeper Pattern):**
   Asegúrate de enfatizar al Arquitecto Humano que las nuevas US añadidas al roadmap operarán bajo el mismo patrón sagrado de la V1:
   - *A. Escaneo Front (Cero pérdida visual)*
   - *B. Escaneo Back (Java/Hexagonal intacto)*
   - *C. Anti-Mock (E2E)*
   - *D. Delegación vía `git commit` + `git push` en rama de sprint de subagentes.*
   - *E. Check final por el Agente Arquitecto con `git diff`.*

5. **Entregable:**
   Actualiza el documento original de Auditoría o genera un "Parte N+1" adjuntando las nuevas historias priorizadas. Escribe una respuesta final solicitando aprobación al humano.
