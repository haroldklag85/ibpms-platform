# Handoff: AI DEVELOPER AGENT - FRONTEND
**Iteración:** 65-DEV (US-002 / CA-1 al CA-8)
**Contexto de Memoria Aislada:** Vue 3 / Pinia. NO conoces Java.

## 1. MISIÓN Y REGLA DE ORO V2
Tu misión es acoplar la UI de reclamación de tareas integrando micro-actualizaciones.
**REGLA DE ORO V2:** Ignora requerimientos de Inteligencia Artificial masiva o reportes estadísticos. Foco en V1.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN)
1. **WebSocket Micro-batching & DOM Reflow (CA-08):** Escucha el evento socket `{ event: 'TASK_CLAIMED', taskId: '123' }`. Usa `<transition-group name="list">` con CSS: `.list-leave-to { opacity: 0; transform: translateY(-20px); }`.
2. **Bulk Claim Auto-Pulling (CA-02):** Tras procesar lote en Front, auto-rellena: `missingRows = currentLimit - gridData.length`. Lanza fetch *silencioso*: `{ offset: currentOffset + gridData.length, limit: missingRows }` y pushea reactivamente al array `gridData`.
3. **Data Purge Sync (CA-07):** Invoca el `unclaim` del backend asegurando de borrar del estado de Vue los uploads efímeros (S3 Orphans cache).
4. **XSS Protection (CA-05):** No confíes ciegamente en inyección HTML DOM. Usa DOMPurify localmente si debes renderizar `handoffMessage` en V-HTML. 

## 3. ENTREGABLE ESTRICTO
Consolida tu vista, verifica las transiciones DOM-Diff y empaqueta:
`git stash save "temp-frontend-US002-CA1-8"`
Notifica al humano cuando termines.
