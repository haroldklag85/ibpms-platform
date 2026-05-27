# T-24-QA: J-02 Certification 49 E2E Specs Execution Plan

This document tracks the creation of 49 E2E test files for UAT J-02 certification.

## Batch 1: F1 Formularios (4 CUs)
- [x] f1-formularios/cu-j02-01-iform-maestro-auditoria.spec.ts
- [x] f1-formularios/cu-j02-02-formularios-simples.spec.ts
- [x] f1-formularios/cu-j02-03-iform-maestro-evaluacion.spec.ts
- [x] f1-formularios/cu-j02-04-validacion-zod.spec.ts (✅ Timeout Resolved via Vite Proxy Bypass & Content-Length fix)

## Batch 2: F2 DMN + BPMN (5 CUs)
- [ ] f2-dmn-bpmn/cu-j02-05-crear-tabla-dmn.spec.ts
- [ ] f2-dmn-bpmn/cu-j02-06-importar-bpmn.spec.ts
- [ ] f2-dmn-bpmn/cu-j02-07-vincular-formkey.spec.ts
- [ ] f2-dmn-bpmn/cu-j02-08-vincular-decisionref.spec.ts
- [ ] f2-dmn-bpmn/cu-j02-09-exportar-bpmn.spec.ts

## Batch 3: F3 Deploy (2 CUs)
- [ ] f3-deploy/cu-j02-10-preflight.spec.ts
- [ ] f3-deploy/cu-j02-11-deploy-rm.spec.ts

## Batch 4: F4 Ejecución E2E (9 CUs)
All NO APLICA (8) + 1 Sin Test.
Wait, let's list them:
- [ ] f4-ejecucion/cu-j02-f1-01-iniciar-caso.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f1-02-analista-n1.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f1-03-perito-mi.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f1-04-mensaje-policial.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f1-05-subprocess-firma.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f1-06-reserva-cierre.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f2-01-rechazo-dmn.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f3-01-timeout-escalamiento.spec.ts (NO APLICA)
- [ ] f4-ejecucion/cu-j02-f4-01-error-pago-compensacion.spec.ts

## Batch 5: F5 Genérico Kanban (1 CU)
- [ ] f5-generico/cu-j02-k01-kanban-generico.spec.ts

## Batch 6: F6 Observabilidad (3 CUs)
- [ ] f6-observabilidad/cu-j02-obs-01-dashboard-bam.spec.ts
- [ ] f6-observabilidad/cu-j02-obs-02-historial-motor.spec.ts
- [ ] f6-observabilidad/cu-j02-obs-03-audit-log-modeler.spec.ts

## Batch 7: F7A Workdesk (10 CUs)
- [ ] f7a-workdesk/cu-j02-w01-paginacion.spec.ts
- [ ] f7a-workdesk/cu-j02-w02-busqueda.spec.ts
- [ ] f7a-workdesk/cu-j02-w03-filtros-facetados.spec.ts
- [ ] f7a-workdesk/cu-j02-w04-semaforo-sla.spec.ts
- [ ] f7a-workdesk/cu-j02-w05-recalculo-inactividad.spec.ts
- [ ] f7a-workdesk/cu-j02-w06-consolidacion-grilla.spec.ts
- [ ] f7a-workdesk/cu-j02-w07-keepalive.spec.ts
- [ ] f7a-workdesk/cu-j02-w08-websocket.spec.ts
- [ ] f7a-workdesk/cu-j02-w09-delegacion.spec.ts
- [ ] f7a-workdesk/cu-j02-w10-attend-next.spec.ts

## Batch 8: F7B Claim (8 CUs)
- [ ] f7b-claim/cu-j02-c01-reclamo-individual.spec.ts
- [ ] f7b-claim/cu-j02-c02-concurrencia.spec.ts
- [ ] f7b-claim/cu-j02-c03-bulk-claim.spec.ts
- [ ] f7b-claim/cu-j02-c04-exploracion-readonly.spec.ts
- [ ] f7b-claim/cu-j02-c05-liberacion-amnesia.spec.ts
- [ ] f7b-claim/cu-j02-c06-despojo-forzoso.spec.ts
- [ ] f7b-claim/cu-j02-c07-trazabilidad-popup.spec.ts
- [ ] f7b-claim/cu-j02-c08-separacion-cola-bandeja.spec.ts

## Batch 9: F7C Kanban (7 CUs)
- [ ] f7c-kanban/cu-j02-a01-crear-tablero.spec.ts
- [ ] f7c-kanban/cu-j02-a02-crud-tarjetas.spec.ts
- [ ] f7c-kanban/cu-j02-a03-drag-drop-ws.spec.ts
- [ ] f7c-kanban/cu-j02-a04-blocked-modal.spec.ts
- [ ] f7c-kanban/cu-j02-a05-time-tracking.spec.ts
- [ ] f7c-kanban/cu-j02-a06-inmutabilidad-done.spec.ts
- [ ] f7c-kanban/cu-j02-a07-formulario-generico.spec.ts

## Batch 10: Negativos (17 CUs)
- [ ] negativos/cu-j02-neg-01-form-sin-campos.spec.ts
- [ ] negativos/cu-j02-neg-02-datos-invalidos.spec.ts
- [ ] negativos/cu-j02-neg-03-deploy-sin-formkey.spec.ts
- [ ] negativos/cu-j02-neg-04-designer-sin-rol.spec.ts
- [ ] negativos/cu-j02-neg-05-decisionref-huerfano.spec.ts
- [ ] negativos/cu-j02-neg-06-obs-invalidas.spec.ts
- [ ] negativos/cu-j02-neg-07-director-rechaza.spec.ts
- [ ] negativos/cu-j02-neg-08-hard-limit-paginacion.spec.ts
- [ ] negativos/cu-j02-neg-09-idor-delegacion.spec.ts
- [ ] negativos/cu-j02-neg-10-rate-limiting.spec.ts
- [ ] negativos/cu-j02-neg-11-dto-sanitizacion.spec.ts
- [ ] negativos/cu-j02-neg-12-cross-team-despojo.spec.ts
- [ ] negativos/cu-j02-neg-13-optimistic-rollback.spec.ts
- [ ] negativos/cu-j02-neg-14-exceder-columnas.spec.ts
- [ ] negativos/cu-j02-neg-15-editar-done.spec.ts
- [ ] negativos/cu-j02-neg-16-doble-asignacion.spec.ts
- [ ] negativos/cu-j02-neg-17-borrar-timelog.spec.ts

## Actions
## Actions
- Write tests per batch.
- Run `npx playwright test --grep "CU-J02" --reporter=list` locally.
- Commit.
