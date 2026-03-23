# Políticas de UX/UI y Reglas del Master Layout (V1)

## 1. Estructura Core del Proyecto (Master Layout)

El iBPMS Platform se rige bajo una estructura *Zero-UI* de tres bloques o zonas fundamentales, cuya disposición garantiza que la métrica cognitiva del usuario se mantenga balanceada. La estructura acordada es la siguiente:

1. **Barra Lateral Izquierda (Sidebar / Navigation):** Eje de enrutamiento principal.
2. **Barra Superior (Top Bar / Header):** Eje de contexto, identidad y acciones globales.
3. **Lienzo Principal (Main Panel / Right Canvas):** Área activa de trabajo o *Workspace*.

---

## 2. Proporciones y Comportamiento Geométrico (Percentages & CSS Grid)

Para mantener la soberanía geométrica y prevenir el "Spaghetti Visual", se establecen las siguientes reglas de partición de pantalla:

### 2.1 Monitores de Escritorio (Desktop \> 1024px)
*   **Sidebar (Barra Lateral):** Debe ocupar estrictamente **250px a 280px** de ancho fijo (aprox. **15% - 20%** en resoluciones 1080p). Nunca debe utilizar porcentajes relativos dinámicos (`%`) en su ancho principal para evitar reflows de texto.
*   **Header (Barra Superior):** Altura rígida entre **60px y 64px**. Ocupa el **100%** del remanente horizontal (ancho total menos el ancho del Sidebar).
*   **Lienzo Principal (Canvas):** Ocupa el **100%** del espacio restante (relación `flex-1` o `calc(100vh - 64px)`). Dependiendo de la pantalla activa (ej. Diseñador BPMN o Bandeja de Entrada), el lienzo interno puede subdividirse temporalmente (Ej: 25% Filtros / 75% Grilla).

### 2.2 Dispositivos Móviles y Tablets (Mobile \< 1024px)
*   **Sidebar:** Regla "Off-canvas". Debe colapsar al **0%** de la pantalla y aparecer únicamente bajo demanda flotando sobre el lienzo (`z-index` elevado con overlay oscuro).
*   **Header:** Mantiene altura rígida (ej. 60px) con botón de hamburguesa visible.
*   **Lienzo Principal:** Absorbe el **100%** del ancho de la pantalla.

---

## 3. Criterios y Políticas de UX/UI para cada Área

### 3.1 Barra Lateral Izquierda (Navigation Sidebar)
*   **Política de Agrupación:** Limitar la jerarquía visual a máximo 2 niveles (Categoría -\> Ítem). Agrupaciones de más de 3 niveles están prohibidas para prevenir "fatiga de clic".
*   **Zero-Trust Rendering:** La visibilidad de los menús está subordinada al estado del RBAC (`useAuthStore`). Todo enlace no autorizado simplemente no se renderiza en el V-DOM.
*   **Semántica Visual:** Todos los ítems deben estar acompañados de iconografía nativa SVG (Phosphor/Lucide).

### 3.2 Barra Superior (Global Header)
*   **Identidad y Contexto:** Debe reflejar permanentemente en dónde está el usuario (Breadcrumbs automáticos) y su rol activo (Ej: `ADMIN GLOBAL`).
*   **Acciones Utilitarias:** Ubicar asimétricamente a la derecha elementos de bajo impacto sistémico e informativo: Notificaciones de campana, Selector de densidades/temas y Menú de Perfil/Logout.
*   **Inmutabilidad:** Jamás debe ser cubierta por modales paralelos u objetos flotantes no críticos, excepto por alertas Toast (Nivel 0 Fatales).

### 3.3 Lienzo Principal (Workspace Canvas)
*   **Ley delScroll Independiente:** Bajo ninguna circunstancia el scroll del lienzo principal puede arrastrar la Barra Superior ni la Barra Lateral. Debe contener su propio flujo `overflow-y-auto`.
*   **Densidad de la Información (Padding \& Whitespace):** Se somete a la directiva global `p-card-p` para mantener una estructura respirable, a menos que el usuario modifique expresamente su métrica de densidad (Comfortable, Compact).
*   **Composición Dinámica:** Está diseñado para montar vistas mediante `<router-view>` sin recarga completa de PWA, protegiendo eficientemente la memoria local a través de barreras `<keep-alive>` condicionadas a sesiones válidas.

---

## 4. Gobernanza Avanzada y Casos Extremos

Aprobadas formalmente por Arquitectura, se establecen las siguientes directrices irrompibles para 15 aristas críticas de diseño:

### Bloque A: Gobernanza de Capas y Colisiones
1. **Jerarquía Escalar (Z-Index):** Adopción estricta de la escala nativa de Tailwind CSS: Lienzo (`z-0`), Sidebar (`z-30`), Header (`z-40`), Modales/Popups (`z-50`), Alertas/Toasts (`z-[9999]`).
2. **Interrupciones Globales (System Banners):** Uso de **"Inline Pushing"**. Los banners críticos (ej. Mantenimiento) se posicionan bajo el Header empujando el Lienzo hacia abajo. Ocupan el ancho y no tapan el contenido (no flotan). Los Modales bloqueantes solo se usan en Faltas Fatales Absolutas (Sesión Muerta).

### Bloque B: Geometría y Sub-Divisiones del Lienzo
3. **Límites Master-Detail (Pantallas Divididas):** La barra lateral izquierda de listados o contextos nunca debe medir menos de **300px**. En pantallas menores a `lg: 1024px`, la división colapsa y muestra solo un entorno a la vez.
4. **Anclaje de Metadatos (Sticky Headers):** Es de Uso Obligatorio `position: sticky` en los encabezados de las tablas (DataGrids) y en las barras superior/inferior de los diseñadores visuales, protegiendo el scroll infinito.
5. **Paddings Nativos (Respiración del Diseño):** Los Formularios y Grillas estáticas usarán un margen unificado corporativo (`p-6` = 24px). Los componentes inmersivos de espectro completo (Ej. Modeler BPMN, Form Builder) usarán `p-0` acaparando borde a borde.
6. **Manejo de Pestañas (Canvas Overflow):** Limitación estricta al estándar de UX de **Scroll-X horizontal visible** para pestañas dinámicas desbordantes. Las listas colapsadas (Dropdowns de pestañas escondidas) están prohibidas por pérdida de mapa mental cognitivo.

### Bloque C: Comportamiento Responsivo (Móviles y Tablets)
7. **Breakpoints Oficiales (Puntos de Quiebre):** Únicos permitidos por Tailwind: `sm: 640px`, `md: 768px`, `lg: 1024px` y `xl: 1280px`. Breakpoints quemados arbitrariamente en CSS crudo resultarán en rechazo inmediato en QA.
8. **Retención de Estado (Sidebar Memory):** El estado del árbol de navegación (carpetas expandidas) debe guardarse incondicionalmente en `localStorage` (Amnesia Zero). 
9. **Inyección de Carga (Skeleton Loaders):** Cero "Spinners" bloqueantes crudos en renderizados estructurales. Se imponen Skeletons reactivos con animaciones `pulse`.

### Bloque D: Accesibilidad y Rendimiento
10. **A11y y Focos de Teclado:** Mandatorios `focus-visible:ring`. La tabulación viaja estrictamente: Header -> Sidebar -> Canvas de Izquierda a Derecha. Jamás se permite ocultar el foco (`outline-none` ciego).
11. **Rendimiento Visual (Reduced Motion):** Ejecución silenciosa del query `@media (prefers-reduced-motion)`. El motor BPMN/Vue apagará blurs asíncronos y animaciones pesadas a favor del renderizado de cuadros crudos.
12. **Barras de Desplazamiento (Scrollbars):** Uso mandatorio de directivas `::-webkit-scrollbar` coloreadas corporativamente y finas, evadiendo el Scroll Grueso predeterminado del S.O pero garantizando el contraste AAA.
13. **Responsive Tipography (Clamping):** Uso estricto de la función nativa `clamp(min, ideal, max)` en Títulos "Hero" o "Headers" para atenuación elástica, protegiendo desbordamientos (`text-overflow`).

### Bloque E: Acciones Críticas y Vacíos
14. **Floating Action Buttons (FABs):** Quedan **estrictamente prohibidos** en Desktop/B2B. Todo Botón de Acción Principal (Call To Action - CTA) debe ubicarse en la esquina superior derecha del Panel o Header correspondiente.
15. **Posicionamiento del "Empty State" (Vacíos):** Todo "Empty State" debe emplazarse en el *Centro Matemático Absoluto* (Horizontal y Vertical) del área disponible, conteniendo estrictamente: [Ilustración] + [Texto] + [Botón de Acción].
