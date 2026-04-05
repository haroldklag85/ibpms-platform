# Análisis Funcional y de Entendimiento: US-003

## Historia Analizada
**US-003: Instanciar y Generar un Formulario "iForm Maestro" vs "Simple"**

---

### 1. Resumen del Entendimiento

La US-003 es la **historia más extensa y densa del backlog completo** (86 Criterios de Aceptación, ~560 líneas de Gherkin). Constituye el corazón del producto iBPMS: un **IDE Web bidireccional** (Pantalla 7) donde un Arquitecto Frontend / Administrador diseña formularios empresariales arrastrando componentes visuales a un Canvas, mientras un panel Mónaco IDE genera automáticamente código Vue 3 y esquemas de validación Zod en tiempo real. El enlace es bidireccional: modificar el código altera el Canvas y viceversa.

La historia define dos arquetipos de formulario:
- **Formulario Simple:** Un formulario plano de una sola vista.
- **iForm Maestro (Expediente Multi-Etapa):** Un formulario de alta densidad que se transforma dinámicamente según la variable `Current_Stage` de Camunda, permitiendo que distintas áreas operativas (Comercial, Legal, Auditoría) trabajen sobre el mismo expediente con campos, permisos y validaciones diferenciados por etapa y rol.

La historia abarca desde la paleta de componentes HTML5, la validación reactiva Zod, el aislamiento de seguridad (Shadow DOM, AST Sandbox anti-XSS), hasta herramientas avanzadas como generación de tests QA automáticos, simulación multi-rol en caliente y generación de formularios asistida por Inteligencia Artificial.

---

### 2. Objetivo Principal

Dotar a la plataforma de un **constructor de formularios No-Code/Pro-Code híbrido** que elimine la deuda técnica de vendor lock-in, permita a los Arquitectos diseñar expedientes multi-etapa empresariales (ERP-like) y generar código Vue 3 + Zod compilable, validado y seguro, sin depender de frameworks propietarios cerrados.

---

### 3. Alcance Funcional Definido

| Dimensión | Hasta Dónde Llega | Dónde Termina |
|---|---|---|
| **Diseño Visual** | Canvas drag-and-drop con paleta HTML5 completa (CA-11), layouts multicolumna (CA-55), tabs/acordeones (CA-8) | No incluye theming por tenant ni modo oscuro (Diferidos a V2) |
| **Generación de Código** | Bidireccional en vivo Canvas ↔ Mónaco IDE con Zod (CA-2, CA-78) | No genera archivos `.js` estáticos para producción; todo es dinámico en runtime |
| **Validación** | Zod reactivo con Lazy Validation @blur/debounce (CA-22, CA-80), validaciones cruzadas (CA-32), condicionales Required-If (CA-48) | No soporta validaciones asíncronas complejas tipo "consultar BD externa al tipear" directamente en Zod |
| **Seguridad** | Shadow DOM (CA-6, CA-79), AST Sandbox anti-eval (CA-4), SSRF prevention (CA-77) | No incluye WAF externo ni CSP headers (eso es infra) |
| **Integración Camunda** | Drag-and-drop de Process Variables (CA-12), I/O Mapping (CA-13), Smart Buttons (CA-14, CA-82) | El acoplamiento es "blando" (CA-16): errores de mapeo no bloquean la compilación del IDE |
| **IA** | Prompt-to-Form y Document-to-Form multimodal (CA-73) | No incluye IA para refinamiento automático de validaciones |
| **QA** | Auto-generación de `.spec.ts` Vitest (CA-68), Simulador Multi-Rol in-browser (CA-69), Sandbox Zod (CA-83) | No sustituye pipelines E2E con Playwright/Cypress |
| **Exposición B2C** | Formularios públicos sin JWT con reCAPTCHA (CA-70) | No incluye portal ciudadano completo ni gestión de identidad anónima |

---

### 4. Lista de Funcionalidades Incluidas

#### A. Arquitectura Core del IDE
1. Selección de patrón de formulario: Simple vs iForm Maestro (CA-1)
2. Generación bidireccional Canvas ↔ Mónaco IDE en vivo (CA-2, CA-78)
3. Iconos de ayuda en pestañas del IDE (CA-3)
4. AST Sandbox anti-XSS sin `eval()` (CA-4, CA-79)
5. Factoría Zod reactiva on-the-fly sin archivos estáticos (CA-5, CA-78)
6. Aislamiento CSS Shadow DOM (CA-6, CA-79)
7. Render Functions `h()`, Teleport y Z-Index Orchestrator (CA-7)
8. Agrupadores ERP: Tabs, Acordeones, DataGrids anidados (CA-8)
9. Cohabitación de múltiples iForm Maestros en un mismo proceso (CA-9)
10. Modo inmersivo Full-Screen Focus (CA-10)
11. Maximización del lienzo con colapso del Mónaco IDE (CA-19)
12. Language Servers: TypeScript, Vue SFC, SCSS/Tailwind, JSON (CA-17)

#### B. Paleta de Componentes y Configuración
13. Paleta completa HTML5 con mapeo Zod por tipo (CA-11)
14. Componente Modal/Pop-up informativo sin I/O Binding (CA-11B)
15. Tooltips de ayuda "Para Dummies" en propiedades avanzadas (CA-18)
16. Dropdowns alimentados por CSV (CA-29)
17. Dropdown con búsqueda Typeahead (CA-40)
18. Multi-Select visual con Chips/Pastillas (CA-45)
19. Campos ocultos (Hidden Inputs) para metadata (CA-47)
20. Máscaras de entrada con formato visual (CA-36, CA-50)
21. Firma electrónica manuscrita Canvas HTML5 (CA-31)
22. Captura GPS embebida (CA-61)
23. Lector de código de barras/QR por cámara (CA-62)
24. Cronómetro de productividad con 3 modos (CA-58)
25. Componente DatePicker simple (CA-44)
26. Campo Contraseña enmascarado (CA-53)
27. Dropzone drag-and-drop para adjuntos (CA-60)

#### C. Validación y Lógica de Negocio
28. Validación reactiva Zod con Debounce/Blur (CA-22, CA-80)
29. Validaciones cruzadas entre campos (CA-32)
30. Restricciones de longitud min/max dinámicas (CA-38)
31. Validaciones condicionales Required-If (CA-48)
32. Auto-Regex para Email/URL (CA-63)
33. Visibilidad condicional de campos (CA-25)
34. Candado Solo-Lectura por fórmulas (CA-57)
35. Limpieza automática de datos fantasma en ramas condicionales (CA-54, CA-82)
36. Mensajes Hint multi-estado con progreso visual (CA-64)

#### D. Integración con Camunda y Datos
37. Drag-and-drop de Process Variables desde BPMN (CA-12)
38. Mapeo declarativo I/O Form-to-Process (CA-13)
39. Smart Buttons nativos: Completar, Reclamar, Borrador, Error BPMN (CA-14, CA-82)
40. Interceptores `try/catch` automáticos en Smart Buttons (CA-15)
41. Bajo acoplamiento Form-to-Process (CA-16)
42. Autocompletado vía Hub de Integraciones (CA-30, CA-77)
43. Data Binding / Precarga desde Camunda (CA-43)
44. Enrutamiento de adjuntos por TRD a SGDEA/SharePoint (CA-21)

#### E. Gestión, Versionamiento y Seguridad
45. Permisos de sobrescritura por Rol RBAC (CA-20)
46. Prevención de borrado de formularios activos (CA-26)
47. Control de versiones inmutables del formulario (CA-27)
48. Bitácora de auditoría a nivel de campo (CA-28)
49. Grillas editables con protección y auditoría parcial (CA-51)
50. Sello visual de aprobación con Rol (CA-46)
51. Estilos CSS corporativos estandarizados V1 (CA-23)
52. Anclaje de versión para procesos In-Flight / Lazy Patching (CA-81)
53. Catálogo y explorador de formularios con buscador (CA-86)

#### F. Resiliencia y Offline
54. Auto-guardado de borrador en LocalStorage (CA-24, CA-82, CA-85)
55. Resiliencia offline con Service Worker y Optimistic Locking (CA-72)
56. Limpieza de archivos huérfanos vía Beacon (CA-82)
57. Máquina del Tiempo JSON con snapshots restaurables (CA-71)
58. Manejo amigable de errores de sintaxis en Mónaco IDE (CA-84)

#### G. Herramientas Avanzadas y QA
59. Generación autónoma de pruebas unitarias Vitest/Jest (CA-68)
60. Simulador Multi-Rol en tiempo real (CA-69)
61. Sandbox de pruebas Zod in-browser (CA-83)

#### H. IA y Gobernanza de Datos
62. AI Prompt-to-Form y Document-to-Form multimodal (CA-73)
63. Diccionario Global y Fragmentos reutilizables "Snippets" (CA-74)
64. Peaje Analítico / Data Diet (CA-75)
65. Sello Radiactivo de Privacidad PII (CA-76)
66. Formulario público B2C con reCAPTCHA y Rate Limiting (CA-70)

---

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas

#### GAP-1: Ausencia de Estrategia de Persistencia del Diseño (¿Dónde vive el JSON del Canvas?)
La historia describe exhaustivamente cómo se diseña, valida y renderiza un formulario, pero **no especifica el mecanismo de persistencia del diseño en sí**. ¿El JSON/AST del esquema visual se guarda en una tabla relacional? ¿En un blob S3? ¿En el repositorio Git del proyecto? El CA-27 menciona "versionamiento inmutable" pero no dicta la infraestructura de almacenamiento. Esto es un vacío que el desarrollador Backend deberá resolver por interpretación.

#### GAP-2: Ambigüedad en el Motor de Renderizado Operativo vs IDE
Hay una dualidad no resuelta explícitamente: el **IDE** (Pantalla 7, donde el Arquitecto diseña) y el **Workdesk** (Pantalla 2, donde el Operario llena el formulario). La historia mezcla criterios del IDE y criterios del renderizado operativo sin separar claramente los dos contextos de ejecución. Por ejemplo, CA-80 (Lazy Validation) aplica al Workdesk, pero CA-84 (Errores de Mónaco) aplica al IDE. Esta separación de responsabilidades debería estar explícitamente delimitada para evitar que el desarrollador mezcle lógica de diseño con lógica de ejecución.

#### GAP-3: Conflicto Potencial entre CA-68 (Auto-Vitest) y CA-83 (Sandbox In-Browser)
- CA-68 genera archivos `.spec.ts` físicos para Vitest/Jest.
- CA-83 ejecuta fuzzing en la RAM del navegador sin generar archivos.
Ambos prometen "cobertura QA automatizada" pero con mecanismos opuestos. No queda claro si son complementarios (el Sandbox es el quick-check y el `.spec.ts` es para CI/CD) o redundantes. El desarrollador necesita una directriz de cuándo usar cuál.

#### GAP-4: Escala de Complejidad del iForm Maestro (Límites de Rendimiento)
Se mencionan formularios de "100+ campos" (CA-8, CA-80) con grillas anidadas, tabs, y datos de múltiples etapas. No existe un criterio que defina el **límite máximo funcional** del Canvas (¿500 campos? ¿1000?). Tampoco se especifica la estrategia de lazy-loading del DOM para formularios monstruosos que podrían asfixiar el navegador por puro peso del Virtual DOM.

#### GAP-5: Dependencia Crítica No Resuelta con US-029 (Persistencia CQRS)
Múltiples criterios (CA-13, CA-14, CA-24, CA-82) hacen referencia a la persistencia del estado del formulario y al ciclo de vida de borradores via US-029. Si la US-029 no estuviera completamente definida o implementada, **toda la funcionalidad de Auto-Guardado, Smart Buttons y I/O Mapping de la US-003 queda huérfana**.

#### GAP-6: Política de Cleanup de LocalStorage
Los CA-24, CA-72, CA-82 y CA-85 acumulan datos en `LocalStorage` del navegador (borradores del Workdesk, borradores del IDE, snapshots JSON). No se define una **política de expiración o limpieza automática** del LocalStorage. Un operario que trabaja 8 meses en el sistema acumulará megabytes de borradores fantasma que nunca se purgan, potencialmente degradando el rendimiento del navegador.

#### GAP-7: Vista de Solo-Lectura (CA-56) vs Visor Histórico (CA-37) — Duplicidad Funcional
Ambos criterios prometen un modo lectura del formulario sin capacidad de edición, pero con matices diferentes (CA-56 es para "Visualizadores activos" y CA-37 es para "Auditoría post-mortem"). No queda explícito si comparten el mismo componente Vue subyacente o si son implementaciones separadas.

---

### 6. Lista de Exclusiones (Fuera de Alcance)

1. **Theming/White-Label por Tenant** — Explícitamente diferido a V2 (Nota Post-MVP en línea 419).
2. **Modo Oscuro** — Diferido a V2 (CA-65).
3. **Soporte Multi-Idioma (i18n)** — Diferido a V2 (CA-42).
4. **Conversor de Moneda en Vivo** — Diferido a V2 (CA-66).
5. **Editor WYSIWYG de Texto Enriquecido** — Diferido a V2 (CA-67).
6. **Rango de Fechas (Date Range Picker)** — Diferido a V2 (CA-44).
7. **Ejecución de formularios en aplicaciones móviles nativas** — No mencionado.
8. **Gestión de ciclo de vida del formulario desde Camunda (deploy automático)** — No incluido; el formulario se publica desde el IDE, no desde el modelador BPMN.
9. **Tests E2E (Playwright/Cypress)** — La US genera `.spec.ts` unitarios con Vitest, pero no E2E.

---

### 7. Observaciones de Alineación o Riesgos para Continuar

> [!WARNING]
> **Riesgo de Scope Creep Monumental.** Con 86 criterios de aceptación, esta historia concentra una cantidad de funcionalidad equivalente a un producto completo por sí sola. Recomiendo firmemente que, al momento de llevarla a desarrollo, se **fragmente en sub-épicas** (Ej: "Core IDE + Bidireccional", "Paleta de Componentes", "Validación Zod", "Herramientas QA", "IA", "B2C/Público") para evitar que un solo sprint colapse bajo su peso.

> [!IMPORTANT]
> **Dependencias Externas Críticas.** La US-003 es un nodo central que irradia dependencias hacia:
> - **US-029** (Persistencia CQRS de borradores y estado)
> - **US-033** (Hub de Integraciones para autocompletado gobernado)
> - **US-035** (Bóveda SGDEA para adjuntos)
> - **US-036** (RBAC para permisos por campo)
> - **US-051** (Router Vue para rutas públicas)
> - **US-028** (Sandbox QA como extensión directa de CA-83)
> Si alguna de estas historias sufre retraso o redefinición, impacta directamente la capacidad de entregar la US-003 completa.

> [!NOTE]
> **Alineación Arquitectónica Positiva.** La historia demuestra una madurez técnica excepcional en el diseño de seguridad (Shadow DOM, AST Sandbox, SSRF prevention) y en la separación de concerns (Factoría Zod dinámica, bajo acoplamiento Form-to-Process). Estas decisiones arquitectónicas están bien fundamentadas y alineadas con la visión del producto.
