# Solicitud de Aprobación QA - US-007 (Modo Manual DMN)

**Remitente:** Agente Especialista E2E (QA)
**Destinatario:** Arquitecto Líder
**US a certificar:** US-007 (CA-26 a CA-32)
**Estrategia Arquitectónica:** Zero-Mock (Playwright)

## Resumen del Plan de Implementación
He analizado los Criterios de Aceptación y he diseñado la estrategia para crear el archivo `frontend/e2e/dmn-manual.spec.ts` bajo la estricta regla de Zero-Mock (sin usar `page.route` para simular respuestas de red).

1. **Seeding Dinámico:** Ampliaré `helpers/task-seeder.ts` para proveer un DMN real sobre el cual el editor pueda trabajar, garantizando que el test no colapse por falta de datos reales.
2. **CA-26 (Coexistencia UI):** Aserción de ambos paneles `.chat-nlp-panel` y `.dmn-grid-panel`.
3. **CA-28 (Validación FEEL):** Inyección de errores sintácticos intencionales, verificación de alertas rojas en celdas y el bloqueo consecuente del botón Guardar.
4. **CA-29 (Fila Catch-All):** Verificación de inmutabilidad (sin botón eliminar) en la fila "Revisión Humana".
5. **CA-31 (Límite SRE 100):** Uso de seeding vía API para inyectar 99 filas al DMN test, y aserción de que la fila 100 lanza el error "Límite SRE alcanzado" deshabilitando el botón.
6. **CA-32 (Trazabilidad):** Guardado exitoso y verificación de aparición del badge "Modificada Manualmente" en el Catálogo DMN.

**Petición:**
Arquitecto, solicito tu revisión y **LUZ VERDE (Aprobación)** para proceder a la fase de EXECUTION, donde materializaré este plan en código TypeScript y realizaré el Push a la rama `sprint-6`.
