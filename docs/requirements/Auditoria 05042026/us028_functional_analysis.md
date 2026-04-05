# Análisis Funcional Definitivo: US-028 (Simulador de Contratos Zod en Memoria - In-Browser QA Sandbox)

## 1. Resumen del Entendimiento
Esta historia plantea un módulo de Simulación y QA integrado en el propio IDE de Formularios (Pantalla 7). Su propósito es emular en caliente cómo el usuario final llenaría el formulario, generando payloads mediante *fuzzing* superficial estructurado (Path Feliz y Path Triste) contra el esquema Zod, sin invocar peticiones de red ni generar código físico (`.spec.ts`) local.

## 2. Objetivo Principal
Aplicar *Shift-Left QA* directamente en la fase de diseño. Permite al Arquitecto certificar de forma autónoma y síncrona que las complejas validaciones matemáticas y Regex sean solventes, protegiendo al motor transaccional y minimizando la dependencia de ingenieros de QA automatizado tradicionales.

## 3. Alcance Funcional Definido
**Inicia:** Cuando el diseño del formulario (JSON/Zod) está completo y se oprime el botón `[Simular Contrato Zod]`.
**Termina:** Cuando el esquema es sometido a estrés, se resuleven los paths, el QA verifica el DTO procesado, y opcionalmente firma formalmente el contrato con el botón `[Certificar Contrato]`, inyectando el sello `is_qa_certified` en base de datos. 

## 4. Lista de Funcionalidades Incluidas
- **Panel QA (Split-View):** Vista con Input editable (Payload Crudo) a la izquierda y Output inmutable a la derecha.
- **Zero-Network Mocking:** Aisla el ciclo vital del componente; desactiva `onMounted` y Red para evaluar unicamente `.safeParse()`.
- **Fuzzing Superficial (Shallow):** Autogenera datos dummy por inferencia de tipo, iterando arreglos superficialmente para evitar desbordes de memoria.
- **Traductor HTML de Errores Zod:** Transforma el JSON críptico de error en viñetas amigables.
- **Sello Criptográfico ISO:** Confirmación y guardado del estado de certificación (Guardrail final pre-despliegue de Camunda).
- **Amnesia Prevencion:** Cachea localStorage con los manual payloads por sesión.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Bloqueos Zod Sincrónicos:** El CA-8 permite bloqueos asíncronos (`.safeParse` síncrono nativo). Sin uso de Web Workers, si un QA carga manualmente un Payload malicioso de 5MB con Regex excesivos (Catastrophic Backtracking), la pestaña de Chrome colapsará (Out of Memory). Se catalogó aceptable para V1, pero a nivel SRE es un gap que debe monitorearse.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Exportar código `.spec.ts` o generar tests automatizados de Playwright a partir del diseño de QA (La US-003 CA-68 auto-escribe Vitest, pero el Sandbox de la US-028 evalúa *In-Browser*).
- Simulación compleja de peticiones API reales y Webhooks en el modo Sandbox.
- Optimización asíncrona vía Web Workers (explícitamente prohibido en V1).

## 7. Observaciones de Alineación o Riesgos
**100% de Alineación SRE.** El módulo es una pieza de arte técnica de Cero Gaps lógicos. La arquitectura `Shallow Fuzzing` y `Zero Load` previene caídas masivas del servidor al delegar la carga transaccional de QA al cliente (RAM del navegador web local).
