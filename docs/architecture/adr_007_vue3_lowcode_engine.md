# ADR-007: Motor de Renderizado Vue 3 para Low-Code iForms (US-003)

**Status:** Aprobado
**Date:** 2026-03-07
**Context:** US-003 (iForm Builder / Generador Visual de Formularios)
**Autor:** Lead Software Architect

## 1. Contexto Estratégico
El Product Owner ha definido la visión para la Pantalla 7 (iForm Builder) como un Web IDE nativo que evite el *Vendor Lock-in* de BPMs comerciales. Este generador creará formularios complejos ("iForms Maestros") interpretados y validados completamente del lado del cliente antes de enviarse al Core.

Se han planteado cuatro grandes desafíos arquitectónicos respecto a la seguridad, reactividad y las capacidades técnicas de Vue 3 para lograr este objetivo. A continuación, las decisiones técnicas inquebrantables.

## 2. Decisiones Arquitectónicas (Resolución de Dudas)

### A. Inyección Dinámica de Lógica JS (Seguridad VS Flexibilidad)
**Problema:** El arquitecto funcional necesita escribir reglas como `if (monto > 5000) showCampo = true`. Ejecutar esto con `eval()` o `new Function()` abre vectores de ataque XSS catastróficos y viola las políticas CSP (Content Security Policy) de la empresa.
**Veredicto (AST Evaluator / Sandboxing Estricto):** Queda estrictamente prohibido el uso de `eval()`.
La plataforma iBPMS (Frontend) integrará un evaluador de expresiones seguras o un intérprete AST ligero (como `expr-eval` o `jsep`). Estas librerías parsean el string y lo ejecutan en un contexto matemático y lógico cerrado donde es matemáticamente imposible acceder al objeto `window`, `document` o ejecutar llamadas `fetch`/XHR (Robo de JWT).

### B. Validación de Datos Reactiva (Zod On-The-Fly)
**Problema:** ¿Cómo compilar la validación en tiempo de ejecución sin redesplegar la SPA de Vue?
**Veredicto (Factory Pattern con Zod):** El motor no compilará código fuente. El Mónaco IDE generará un JSON descriptivo (Ej: `{ field: 'monto', rules: ['required', {min: 5000}] }`). 
En el Frontend, una clase de factoría estática leerá este JSON y ensamblará el esquema Zod dinámicamente encadenando los métodos nativos de la librería (Ej: `z.number().min(5000)`). Este esquema puramente dinámico se conectará de inmediato a la librería `vee-validate` o se inyectará en los Watchers de un *store* de Pinia desacoplado generado en la memoria RAM del navegador.

### C. Estilizado Dinámico sin sangrado global (Style Bleeding)
**Problema:** Inyectar CSS redactado por el usuario final puede reescribir clases como `.btn-primary` y destruir o desfigurar la plataforma base del iBPMS.
**Veredicto (Shadow DOM / Web Components):** 
Para prevenir que el CSS inyectado emponzoñe el ecosistema global de TailwindCSS, el contenedor maestro que renderiza el *iForm* se instanciará utilizando el paradigma de **Shadow DOM** nativo de HTML5.
Los estilos redactados en la Pantalla 7 serán inyectados dentro de una etiqueta `<style>` encerrada dentro de ese *Shadow Root*. Es biológicamente imposible que una directriz CSS (incluso un `body { display: none !important }`) cruce la frontera del Shadow DOM hacia el exterior.

### D. Apalancamiento Excepcional de Vue 3 (Grado Empresarial)
**Problema:** ¿Qué piezas internas del Virtual DOM de Vue usaremos para alejarnos de un simple "v-if" y hacer de esto un motor de nivel corporativo?
**Veredicto:** El motor Low-Code se construirá utilizando:
1.  **Render Functions (`h()`) nativas:** No usaremos plantillas HTML (`<template>`). El motor leerá recursivamente el JSON del formulario y utilizará la función de renderizado programática `h('div', {...})` de Vue 3. Esto otorga control microscópico sobre el Virtual DOM y masivo rendimiento.
2.  **Vue Teleport:** Para que los modales cruzados, diálogos de ayuda o Popovers configurados por el creador del formulario se "teletransporten" físicamente al `<body>` del documento evitando problemas de `z-index` y `overflow: hidden` anidados.
3.  **Custom Directives (`v-mask`, `v-currency`):** El JSON inyectará directivas personalizadas al vuelo en campos de texto, garantizando enmascaramiento transaccional (formateo de moneda, números de teléfono) interceptando nativamente el evento de tipeo del usuario a bajo nivel.

## 3. Impacto Inmediato
1.  El PO tiene luz verde para diseñar los Criterios de Aceptación (Gherkin) de la US-003 vendiéndole al cliente una experiencia "Programable" 100% segura contra XSS.
2.  Los desarrolladores Frontend Vue 3 deben estudiar **Render Functions** y evitar los componentes basados puramente en SFC (Single-File Components) para el corazón del motor "iForm Renderer".
