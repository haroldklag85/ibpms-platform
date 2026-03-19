# Contrato de Arquitectura Frontend (Refactor UX/UI y Vacíos ZOD - iBPMS)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Resolver la Deuda Técnica del `FormDesigner.vue`.

## 📋 Contexto y Órdenes Directas (Implementación Estricta):

El Líder de Arquitectura requiere intervenir el FormDesigner debido a 1) Saturación de 9 botones primarios en el Header y 2) Corrupción referencial e inestabilidad en las opciones de validación del motor AST Zod.

### Tarea 1: Rediseño Jerárquico del Header (4 Zonas)
Debes condensar la cabecera actual, purgando redundancias y organizando estrictamente de izquierda a derecha así:
1. **Zona Visores (Izquierda):** Convierte `[Pantalla Completa]` y `[Print Mode]` en simples Icon-Buttons minimalistas (`🖵` y `👁️`) ubicados junto al título principal, aliviando ruido visual. Use `title=""` (Tooltip).
2. **Zona Dropdown DevTools (Centro-Derecha):** Crea un `<select>` o menú dropdown `🛠️ Herramientas Avanzadas`. Mueve allí dentro: `Historial`, `PDF Export`, `Zod Global` y el `Generador de Tests` maestro.
3. **Zona de Purga:** ELIMINA radicalmente del subárbol de Vue el botón duplicado "Generar Tests Zod (CA-115)". Consérvese únicamente la funcionalidad BDD de Vite (`generateVitestSpec`).
4. **Zona Acciones Críticas (Extrema Derecha):** Conserva aislados e intactos los botones primarios: `[🗑️ Reset]` (Outline Danger) y `[🚀 Probar / Submit Mock]` (Solid Primary).

### Tarea 2: Sellado del Vacío "Zod Global" (Root Schema Reactivity)
* **Requerimiento:** Si el modal "Zod Global" inyecta cadenas literales como `data.password === data.confirm_pwd` y luego el operador re-nombra el campo en el Canvas (`password` a `pass123`), el compilador Zod se romperá en el render.
* **Solución Técnica:** Implementa en `FormDesigner.vue` un Watcher profundo o interceptor al evento de "Actualización de ID" de los campos del Canvas. Cuando el ID mude (de `oldId` a `newId`), itera la propiedad de validaciones globales del Root Schema y aplica un Replace Regex Seguro `/\bdata\.oldId\b/g` por `data.newId`, actualizando el String sin destruir el IDE ni congelar `eval()`.

### Tarea 3: Sellado de Tab de Visualización "Z zod" (Bi-Directional AST Parsing)
* **Requerimiento:** Convertir el panel estático de Zod (code tab `activeCodeTab === 'ZOD'`) en semi/bi-direccional limitando XSS.
* **Solución Técnica:** Captura un debounce en el input o `@change` cuando el usuario edite ese textarea ZOD. Tente cuidado. No utilices `eval`. Solo detecta un simple Regex por nombres de campos. Si la sintaxis se rompe, atrapa un bloque `try-catch`, envuelve el cuadro en rojo y setea un Toast indicando que "El parseo manual ha fallado, las propiedades visuales prevalecen". 

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente. 
Escribe y reorganiza el DOM Vue3, verifica la integridad iterativa y guarda:
`git stash save "temp-frontend-US003-ZodRefactor"`

Informa tu resultado apenas termines.
