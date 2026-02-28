# Análisis y Validación de Usabilidad UX/UI (iBPMS V1)

**Objetivo:** Auditar los "Wireframes Conceptuales V1" (`v1_wireframes.md`) bajo el prisma riguroso de las leyes psicológicas de Experiencia de Usuario (UX) y las Heurísticas de Jakob Nielsen. Este documento dicta el estándar para la transición de Baja Fidelidad (Lo-Fi conceptual) a Alta Fidelidad (Hi-Fi en Figma) y finalmente al desarrollo en Vue 3.

---

## 1. Validación Estructural: Leyes Universales de UX

La arquitectura de información y jerarquía visual de nuestros wireframes V1 se sostiene y justifica mediante las siguientes leyes universales:

### A. Ley de Fitts
> *El tiempo para adquirir un objetivo es función de la distancia y el tamaño de este.*
- **Aplicación en iBPMS:** 
  - **Botones de Acción Críticos:** En la **Pantalla 0**, los botones `[ 🚀 INICIAR ]` dentro de los *Tiles* son grandes bloques interactivos («Call-to-Action»).
  - En el **Copiloto M365 (Pantalla 2C)**, los botones `[ 🚀 Aceptar y Enviar ]` o `[ ❌ Rechazar ]` están anclados al final del componente visual en zonas de amplia clicabilidad (hit-boxes), minimizando el riesgo de un error del usuario ("Human-in-the-Loop").

### B. Ley de Hick (o Hick-Hyman)
> *El tiempo requerido para tomar una decisión disminuye al reducir la cantidad y complejidad de las opciones.*
- **Aplicación en iBPMS:** 
  - Al reemplazar un arcaico menú lateral anidado (clásico de los BPMS antiguos) por la **Pantalla 0 (App Launcher / Service Catalog)**, obligamos al sistema a no mostrar opciones que el usuario no necesita. El sistema rutea por Roles. Si soy Analista, no veo los "Tiles" de Administrar Seguridad. El cerebro tiene menos carga cognitiva para elegir qué "Proceso" iniciar.

### C. Ley de Jakob
> *Los usuarios pasan la mayor parte de su tiempo en otros sitios. Prefieren que tu sitio funcione como los sitios que ya conocen.*
- **Aplicación en iBPMS:** 
  - La **Pantalla 1 (Bandeja Unificada / Inbox)** es un clon cognitivo de Microsoft Outlook o Gmail. En lugar de forzar a los agentes a aprender un nuevo "Panel Pestañado de Tareas BPMN", les entregamos un Inbox. Saben que a la izquierda navegan carpetas (Borradores, Urgentes) y al centro leen la lista vertical. Reducción drástica en la curva de *Onboarding*.

### D. Ley de Miller
> *La persona promedio solo puede retener unos 7 (± 2) elementos en su memoria de trabajo a la vez.*
- **Aplicación en iBPMS:** 
  - Se valida en la **Pantalla 2 (Formulario)** y la **Pantalla 3 (Kanban)**.
  - En lugar de presentar formularios monolíticos de 50 campos (que abruman al humano), fraccionamos la data estructurada en "pasos" o le inyectamos *Minitareas (Kanban Checklists visuales de no más de 3 a 5 items)*, descargando la memoria a corto plazo del operario.

### E. Principio de Pareto (80/20) en UX
> *Aproximadamente el 80% de los efectos provienen del 20% de las causas. (El 20% de las funciones se usan el 80% del tiempo).*
- **Aplicación en iBPMS:**
  - El diseño consolidado de la **Pantalla 15 (Unified Project Dashboard)** concentra el estado BPMN nativo y las Tareas Ágiles en una sola vista. Un *Project Manager* no deambulará por 10 pantallas distintas. El 80% de su toma de decisiones gerencial (cuellos de botella) se resuelve consultando esa única tabla de *Actividades Pendientes* y los *Hit status* de salud.

---

## 2. Auditoría Rápida: Heurísticas de Nielsen (Revisión de Wireframes)

1. **Visibilidad del Estado del Sistema (🟢 Validado):** En la Pantalla 2 e Inbox se exhiben los marcadores `[EN PROGRESO]`, `[SLA: 2 Horas]`, `[URGENTE]`, manteniendo al usuario plenamente consciente de qué ocurre.
2. **Conexión entre el Sistema y el Mundo Real (🟢 Validado):** Reemplazo de jerga de motor ("Token Instance", "UserTask Callback") por lenguaje de negocio ("Factura", "Borrador Guardado", "Aprobar Contrato").
3. **Control y Libertad (🟢 Validado):** Soporte total para las "Rutas de Escape". Botones como `[ 🗑️ Descartar Borrador ]` (Pantalla 1) o la capacidad explícita de `[ ❌ Rechazar ]` cada sugerencia atómica individual de la IA (Pantalla 2C).
4. **Prevención de Errores (🟢 Validado):** La IA propone borradores bilingües, pero el botón de envío automático está deliberadamente *bloqueado* en Pantalla 2C hasta que el usuario ejerza intervención, evitando que el sistema excrete correos defectuosos en nombre de la empresa.
5. **Estética y Diseño Minimalista (🟢 Validado):** El concepto `Zero-UI` parcial y diseño enfocado en "Tiles" asegura limpieza, ocultando paneles no incidentes.

---

## 3. Transición de Arquitectura: De Lo-Fi (Markdown) a Hi-Fi (Vue 3 / Ui Library)

Para garantizar la viabilidad del desarrollo Frontend basado en los Wireframes, el equipo UI/UX debe respetar los siguientes paradigmas de ingeniería para el Sistema de Diseño visual (Design System):

### A. Auto Layout (Flexbox & CSS Grid)
Todo *Frame* en Figma (o componente web) debe comportarse elásticamente.
- La **Pantalla 1B (Bandeja Docketing)** debe fluir dinámicamente: la tabla de registros y el panel de filtros izquierdo deben usar "Auto Layout" para colapsar en tabletas/móviles sin romper los datos tabulares.
- La **Pantalla 2C (Panel IA)** demanda flexbox asimétrico: El panel superior (correo) ocupará `flex: 1`, y el panel IA anclado al fondo ocupará un alto rígido pero con capacidad de *scroll interno*.

### B. Componentización Férrea (Reusable Components)
El Front-end debe ser atómico para agilizar el TTM (Time to Market):
- **Molecule - 'Task Card' (Pantalla 1):** Usado en Inbox, en Dashboards y Project Hub. Contiene: Ícono, Título, Labels `[SLA]`, Metadata y un Action Row oculto al *on-hover*.
- **Molecule - 'AI Suggestion Card' (Pantalla 2C):** La tarjeta que presenta la extracción semántica y ofrece el split binario `[Aprobar | Rechazar]`. Se invoca `x` cantidad de veces según las intenciones descubiertas por la IA.
- **Molecule - 'Data Input' (Para Angular/Vue):** El Motor que renderiza dinámicamente JSONs (Pantalla 7) debe apilar componentes visuales reutilizando el input text base y selects del *Design System Component Library*.

### C. Prototipado Interactivo (Figma)
Antes de quemar horas de desarrollo, la suite de Diseño debe simular interactividad para evaluar el "TTV" (Time-to-Value) humano:
- **Flujo 1:** El viaje visual de recibir un correo en *Inbox (Pantalla 1)* -> hacer clic y abrir el modal Lateral flotante (Offcanvas) de revisión con IA.
- **Micro-interacciones:** Prototipar qué pasa (Animación visual: Desvanecimiento rápido) cuando el usuario selecciona `[❌ Rechazar]` una tarjeta IA. Esto alimenta el MLOps y saca físicamente la tarjeta de la vista sin recargar el navegador.

---
**Conclusión Normativa:**  
Nuestros wireframes V1 cumplen o exceden los estándares de un SaaS Enterprise enfocado en la usabilidad B2B y previenen el *"Síndrome del Panel Lleno"* característico de los BPMs antiguos. El camino a Alta Fidelidad está 100% justificado por psicología del diseño.
