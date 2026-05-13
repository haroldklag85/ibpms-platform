# Handoff Arquitectónico: QA
**Iteración:** 01-DEV-034-DAVID
**Épica:** 12 — Integraciones (US-034)
**Criterios de Aceptación:** CA-01 al CA-05
**Rama de Trabajo:** DevDavid

## 1. Contexto de Negocio
La US-034 implementa el andamiaje principal de orquestación asíncrona mediante RabbitMQ. En esta iteración de QA, se requiere validar empíricamente que la configuración del Broker opera adecuadamente, que el Dashboard DLQ (Dead Letter Queue) está visible y funcional (CA-2) para perfiles autorizados, y que la tolerancia y resiliencia básica asíncrona están dispuestas.

## 2. Alineación Arquitectónica y Criterios a Validar

**Criterios de Aceptación y Scenarios Gherkin de referencia:**
*   **CA-1:** Broker Exclusivo de Alta Demanda.
    *   *Gherkin:* Dado que existe procesamiento asíncrono, se verifica que la cola y el cluster están activos (validación de health o puertos internos si es posible desde e2e, o a través del status del dashboard).
*   **CA-2:** Dashboard Técnico de DLQ (Monitor Visual).
    *   *Gherkin:* Verificar como administrador el acceso al dashboard de DLQ. Verificar la presencia de métricas y los botones de `[Purgar Cola]` y `[Reintentar Mensajes]`.
*   **CA-3:** Jerarquización de Supervivencia (Priority Queues).
    *   *Gherkin:* (Validación enfocada en test de integración/back, pero comprobable a nivel estructural).
*   **CA-4:** Catálogo Oficial de Exchanges.
    *   *Gherkin:* (Verificación en código de la configuración de RabbitMQ en la documentación/repositorio).
*   **CA-5:** Idempotencia Obligatoria en Workers Consumidores.
    *   *Gherkin:* Producir un mensaje por segunda vez con el mismo `idempotency_key` debe arrojar un ACK silencioso (validación de integración/e2e a convenir).

## 3. Estrategia NFR/QA
> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en la ruta "docs/architecture/arquitecturar.md".
- Validar las vistas de administración creadas por Frontend.
- Confirmar que los Endpoints REST de la DLQ en Backend funcionan correctamente.

## 4. Instrucciones Operativas
> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
