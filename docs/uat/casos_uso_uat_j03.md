# Journey J-03: Intake Externo — Webhook O365 → Idempotencia → ClamAV → RabbitMQ → Pre-Triaje → Tarea

> **Journey:** J-03 — Ingesta Automatizada de Correos Externos y Aprobación Humana
> **Actor principal:** Sistema (MS Graph Webhook) / Operario de Pre-Triaje
> **Criticidad:** 🔴 ALTA (Puerta de entrada principal sin UAT previo)
> **US Cruzadas:** US-004, US-034, US-001, US-002
> **Épica:** Motor Core BPMN & Workdesk (Épica A) + Dashboards & Integraciones (Épica F)
> **Fecha:** 2026-04-18
> **Autor:** Agente PO (Antigravity)
> **Formato:** Híbrido (Manual paso-a-paso + links a `.spec.ts`)
> **Decisión PO:** Se EXCLUYE el `EmailWebhookController` (legacy) — se asume deprecación antes del MVP.

---

## Narrativa del Journey

Este Journey certifica el flujo completo de ingesta externa automatizada: un correo electrónico llega vía Microsoft Graph Webhook, pasa por filtros de seguridad (idempotencia, whitelist, HMAC, ClamAV), se encola en RabbitMQ para resiliencia, y finalmente genera una tarea de Pre-Triaje visible en el Workdesk donde un operario humano decide si aprobar o rechazar la solicitud.

```
┌────────────────────────────────────────────────────────────────────────────┐
│  1. Correo llega a buzón O365 (MS Graph detecta)                          │
│  2. Webhook POST al endpoint iBPMS (Idempotencia + HMAC)       (US-004)  │
│  3. Validación: Whitelist dominio + Anti Auto-responder         (US-004)  │
│  4. Escaneo Anti-Malware ClamAV sobre adjuntos                  (US-004)  │
│  5. Encolamiento RabbitMQ (Priority Queue P2)                   (US-034)  │
│  6. Worker desencola → crea Tarea Pre-Triaje en Camunda         (US-004)  │
│  7. Tarea visible en Workdesk del Operario                      (US-001)  │
│  8. Operario reclama tarea → evalúa → Aprueba/Rechaza           (US-002)  │
│  9. Si aprobado → instancia proceso BPMN oficial                (US-004)  │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## Precondiciones

| # | Precondición | Verificación | US Origen |
|---|-------------|-------------|-----------|
| PRE-1 | Endpoint Webhook público expuesto: `POST /api/v1/webhook/intake` | cURL retorna 401/403 sin headers | US-004 |
| PRE-2 | Secreto HMAC compartido configurado entre MS Graph y Backend | Variable de entorno `WEBHOOK_HMAC_SECRET` presente | US-004 CA-10 |
| PRE-3 | RabbitMQ operativo con topología desplegada | Exchange `ibpms.exchange.topic`, Queue `ibpms.integrations.webhook` existen | US-034 CA-4 |
| PRE-4 | ClamAV sidecar operativo | `GET /health/clamav` retorna 200 | US-004 CA-11 |
| PRE-5 | Al menos 1 dominio en Whitelist: `@cliente-autorizado.com` | `GET /api/v1/admin/webhook/allowed-domains` retorna ≥1 | US-004 CA-12 |
| PRE-6 | Operario con rol `PRE_TRIAJE` o `ANALISTA` autenticado | JWT con permisos apropiados | US-001 |
| PRE-7 | Al menos 1 Process Definition publicado en Camunda para vincular | Dropdown de procesos devuelve ≥1 | US-004 CA-15 |

---

## Escenarios UAT — Bloque 1: Recepción y Seguridad Perimetral (US-004)

### CU-J03-01: Recepción Exitosa de Webhook con Firma HMAC
**CA Mapeado:** US-004 CA-10, US-004 CA-17
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | MS Graph | Envía POST `/api/v1/webhook/intake` con Header `X-Hub-Signature` HMAC-SHA256 y payload de correo de `usuario@cliente-autorizado.com` | — |
| 2 | Backend | Valida firma HMAC contra secreto compartido | Firma válida → continúa procesamiento |
| 3 | Backend | Responde sub-segundo | HTTP 202 Accepted: "Mensaje Recibido" (acuse inmediato CA-17) |
| 4 | Backend | Inicia procesamiento asíncrono "en el patio trasero" | Escaneo, validaciones y encolamiento ocurren sin bloquear la respuesta |
**Automatización:** `e2e/specs/j03/webhook-hmac-reception.spec.ts`

### CU-J03-02: Idempotencia ante Duplicados Nerviosos
**CA Mapeado:** US-004 CA-1
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | MS Graph | Envía POST con `id_mensaje: MSG-ABC-123` | HTTP 202 Accepted. Mensaje procesado |
| 2 | MS Graph | Envía segundo POST idéntico con `id_mensaje: MSG-ABC-123` (retry nervioso) | HTTP 200 OK silencioso. NO crea tarea duplicada |
| 3 | MS Graph | Envía tercer POST con el mismo ID | HTTP 200 OK silencioso. BD muestra solo 1 registro para MSG-ABC-123 |
| 4 | Verificación | Consulta BD `ibpms_processed_messages` | Solo 1 entrada con `idempotency_key: MSG-ABC-123` (US-034 CA-5) |
**Automatización:** `e2e/specs/j03/webhook-idempotency.spec.ts`

### CU-J03-03: Bloqueo de Auto-Responders
**CA Mapeado:** US-004 CA-2
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema Externo | Envía correo desde `no-reply@empresa.com` | — |
| 2 | Backend | Detecta patrón de auto-responder en el remitente | Match con regex: `no-reply@`, `mailer-daemon@`, `postmaster@` |
| 3 | Backend | Rechaza en el perímetro | HTTP 400 Bad Request: "Remitente de sistema bloqueado" |
| 4 | Verificación | No existe tarea de Pre-Triaje ni registro en Camunda | Ciclo infinito cortado de raíz |
**Automatización:** `e2e/specs/j03/webhook-autoresponder-block.spec.ts`

### CU-J03-04: Verificación de Dominio en Whitelist
**CA Mapeado:** US-004 CA-4, US-004 CA-12
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Correo Externo | Webhook recibe correo de `atacante@dominio-no-registrado.xyz` | — |
| 2 | Backend | Extrae dominio `@dominio-no-registrado.xyz` y consulta caché Redis de Whitelist | No existe coincidencia |
| 3 | Backend | Rechaza antes de despertar a Camunda | HTTP 403 Forbidden: "Dominio no autorizado" |
| 4 | Verificación | Consulta tabla `ibpms_webhook_allowed_domains` | Dominio no está registrado. Performance <5ms (caché Redis TTL 5min CA-12 §6) |
**Automatización:** `e2e/specs/j03/webhook-domain-whitelist.spec.ts`

### CU-J03-05: Escaneo Anti-Malware ClamAV sobre Adjuntos
**CA Mapeado:** US-004 CA-11, US-004 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Webhook | Recibe correo autorizado con adjunto PDF de 5MB (dentro del límite CA-7) | — |
| 2 | Backend | Envía binario al sidecar ClamAV vía REST | — |
| 3 | ClamAV | Escanea y reporta `CLEAN` | — |
| 4 | Backend | Almacena archivo con flag `scan_status: CLEAN` y hash SHA-256 | Archivo persistido en storage |
| 5 | Verificación | Campo `scan_status` en BD | `CLEAN` + hash SHA-256 verificable |
**Automatización:** `e2e/specs/j03/webhook-clamav-clean.spec.ts`

---

## Escenarios UAT — Bloque 2: Encolamiento y Resiliencia (US-034)

### CU-J03-06: Encolamiento en RabbitMQ con Priority P2
**CA Mapeado:** US-034 CA-3, US-034 CA-4, US-034 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Backend | Tras validar correo, publica mensaje en exchange `ibpms.exchange.topic` | Routing Key: `integrations.p2.webhook` |
| 2 | RabbitMQ | Enruta a cola `ibpms.integrations.webhook` | Mensaje con header `x-priority: P2` (SLA <30min CA-6) |
| 3 | RabbitMQ | Aplica header `x-idempotency-key: {UUID}` | Idempotencia del productor (US-034 CA-5) |
| 4 | Worker | Desencola y procesa en orden de prioridad | P1 (críticos) se procesan antes que P2 (webhooks) |
**Automatización:** `e2e/specs/j03/rabbitmq-priority-enqueue.spec.ts`

### CU-J03-07: Retry Automático con Backoff Exponencial ante Fallo de Worker
**CA Mapeado:** US-034 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Worker | Intenta procesar mensaje pero falla con IOException (error transitorio) | — |
| 2 | RabbitMQ | Reintento 1: inmediato (0ms) | Falla de nuevo |
| 3 | RabbitMQ | Reintento 2: delay 5 segundos | Falla |
| 4 | RabbitMQ | Reintento 3: delay 30 segundos | Falla |
| 5 | RabbitMQ | Reintento 4 (final): delay 2 minutos | Falla → envía a DLX |
| 6 | Sistema | Mensaje enrutado a `ibpms.exchange.dlx` → cola `ibpms.dlq.global` | Headers: `x-original-queue`, `x-delivery-count: 4`, `x-last-error-message` |
**Automatización:** `e2e/specs/j03/rabbitmq-retry-backoff.spec.ts`

### CU-J03-08: Dashboard DLQ para Administrador IT
**CA Mapeado:** US-034 CA-2, US-034 CA-8
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Admin IT | Navega a Pantalla DLQ (componente Vue nativo del iBPMS) | — |
| 2 | Sistema | Consume `GET /api/v1/admin/queues/dlq/summary` | Tabla: total mensajes, agrupación por cola origen, timestamp más antiguo |
| 3 | Admin IT | Pulsa `[Reintentar Mensajes]` | Modal: "Se reintentarán N mensajes. Los Workers deben ser idempotentes" |
| 4 | Admin IT | Confirma reintento | `POST /api/v1/admin/queues/dlq/retry`. Mensajes reencolados |
| 5 | Admin IT | Pulsa `[Purgar Cola]` | Requiere Sudo-Mode (US-038). Justificación obligatoria 20+ caracteres |
| 6 | Admin IT | Confirma con Sudo-Mode + justificación | `DELETE /api/v1/admin/queues/dlq/purge`. Auditoría completa en `ibpms_audit_log` |
**Automatización:** `e2e/specs/j03/rabbitmq-dlq-dashboard.spec.ts`

---

## Escenarios UAT — Bloque 3: Pre-Triaje Humano y Workdesk (US-001 + US-002 + US-004)

### CU-J03-09: Tarea de Pre-Triaje Visible en Workdesk
**CA Mapeado:** US-004 CA-8, US-004 CA-9, US-001 CA-01
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Worker | Desencola mensaje de RabbitMQ exitosamente | — |
| 2 | Worker | Instancia "Tarea de Pre-Triaje" en Camunda (NO el proceso oficial) | UserTask con candidateGroup: `PRE_TRIAJE` |
| 3 | Operario | Abre Workdesk (Pantalla 1) | Ve tarea "Pre-Triaje: Correo de usuario@cliente.com" en su Cola de Equipo |
| 4 | Operario | Tarea muestra SLA de Entrada asignado (reloj corriendo) | Semáforo 🟢 Verde (dentro de tiempo CA-16) |
**Automatización:** `e2e/specs/j03/pretriage-workdesk-visibility.spec.ts`

### CU-J03-10: Operario Reclama y Evalúa la Tarea de Pre-Triaje
**CA Mapeado:** US-002 CA-1, US-004 CA-14
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Reclama la tarea de Pre-Triaje desde la Cola del Equipo | `PATCH /api/v1/tasks/{id}/claim`. Tarea asignada al operario (US-002) |
| 2 | Operario | Abre la tarea | Pre-visualización del cuerpo del correo original + lista de adjuntos |
| 3 | Operario | Revisa el contenido y anexos | Vista clara del mensaje, metadatos del remitente, archivos adjuntos con flag `CLEAN` |
| 4 | Sistema | Muestra botones de acción | `[Aprobar Ingesta]` y `[Rechazar Petición]` visibles |
**Automatización:** `e2e/specs/j03/pretriage-claim-evaluate.spec.ts`

### CU-J03-11: Aprobación de Ingesta → Instanciación de Proceso BPMN Oficial
**CA Mapeado:** US-004 CA-15, US-004 CA-8
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Pulsa `[Aprobar Ingesta]` | — |
| 2 | Sistema | Obliga a seleccionar "Tipo de Proceso" | Dropdown con procesos BPMN publicados disponibles |
| 3 | Operario | Selecciona "Proceso_Onboarding_Cliente" | — |
| 4 | Sistema | Instancia el proceso BPMN oficial en Camunda | `POST start-process`. Variables del correo inyectadas como variables de proceso |
| 5 | Sistema | Tarea de Pre-Triaje marcada como completada | Estado: COMPLETED |
| 6 | Workdesk | Nueva tarea real del proceso aparece en Cola del Equipo | Operarios ven la primera UserTask del flujo Onboarding |
**Automatización:** `e2e/specs/j03/pretriage-approve-instantiate.spec.ts`

### CU-J03-12: Rechazo de Ingesta con Motivo Obligatorio
**CA Mapeado:** US-004 CA-14
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Pulsa `[Rechazar Petición]` | — |
| 2 | Sistema | Despliega campo obligatorio "Motivo de Rechazo" | Textarea con validación `min(10 caracteres)` |
| 3 | Operario | Escribe: "Correo publicitario no relacionado con trámites" | Validación pasa |
| 4 | Operario | Confirma rechazo | Tarea de Pre-Triaje marcada como CANCELLED con motivo registrado |
| 5 | Sistema | NO instancia ningún proceso BPMN | Camunda sin nueva instancia |
| 6 | Verificación | `ibpms_audit_log` contiene el motivo de rechazo | Auditoría completa: operario, timestamp, razón |
**Automatización:** `e2e/specs/j03/pretriage-reject-with-reason.spec.ts`

---

## Escenarios Negativos

### CU-J03-NEG-01: Webhook con Adjunto Infectado (ClamAV Quarantine)
**CA Mapeado:** US-004 CA-11
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Webhook recibe correo autorizado con adjunto `malware.exe` renombrado como `documento.pdf` |
| 2 | Backend envía binario a ClamAV sidecar |
| 3 | ClamAV detecta firma maliciosa → reporta `INFECTED` |
| 4 | Backend retorna HTTP 422: `{ "error": "MALWARE_DETECTED", "file": "documento.pdf" }` |
| 5 | Archivo NO persistido en storage productivo. Hash SHA-256 + metadatos registrados en "Payloads Huérfanos" con motivo `MALWARE_QUARANTINE` |

### CU-J03-NEG-02: Webhook con Adjunto que Excede Límite de Peso
**CA Mapeado:** US-004 CA-7
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Webhook recibe correo con adjuntos que suman 15MB (límite: 10MB) |
| 2 | Backend detecta exceso de peso antes de procesar |
| 3 | HTTP 413 Payload Too Large: "El tamaño de los adjuntos excede el límite permitido" |
| 4 | Correo no procesado. Registro en tabla de Payloads Fallidos (CA-3) |

### CU-J03-NEG-03: RabbitMQ Offline — Buffer y Fallback SQL
**CA Mapeado:** US-034 CA-10
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | RabbitMQ sufre caída total (simulada: `docker stop rabbitmq`) |
| 2 | Health check falla 3 veces consecutivas (45 segundos) → Circuit Breaker OPEN |
| 3 | Webhook recibe correo válido → Backend almacena mensaje en buffer local en memoria (máx 1000 FIFO) |
| 4 | Si RabbitMQ regresa en <5 min: Circuit Breaker HALF-OPEN → buffer se drena automáticamente |
| 5 | Si RabbitMQ NO regresa en 5 min: mensajes del buffer se persisten en tabla `ibpms_queue_fallback`. Alerta crítica al SysAdmin |

### CU-J03-NEG-04: ClamAV Offline — Fail-Secure
**CA Mapeado:** US-004 CA-11 §4
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Servicio ClamAV sidecar no disponible (caída del contenedor) |
| 2 | Webhook recibe correo con adjuntos → Backend intenta escanear |
| 3 | Fail-Secure: Backend rechaza archivo con HTTP 503 Service Unavailable |
| 4 | Payload completo encolado en DLQ de RabbitMQ (CA-6) para reintento cuando escáner se recupere |
| 5 | Alerta técnica emitida al SysAdmin: "Servicio Anti-Malware no disponible" |

### CU-J03-NEG-05: Payload Malformado (JSON Basura)
**CA Mapeado:** US-004 CA-3
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Atacante envía POST con cuerpo: `{ "basura": true, "nada": "util" }` (sin estructura requerida) |
| 2 | Backend valida estructura del payload → falla validación |
| 3 | HTTP 400 Bad Request con respuesta estructurada US-000 |
| 4 | Rastro persistido en tabla "Payloads Huérfanos/Fallidos" con hash, timestamp e IP origen para auditoría forense |

---

## Matriz de Trazabilidad

| Escenario | US Principal | CAs Cubiertos | Prioridad |
|-----------|:-----------:|:------------:|:---------:|
| CU-J03-01 | US-004 | CA-10, CA-17 | MUST |
| CU-J03-02 | US-004 | CA-1 | MUST |
| CU-J03-03 | US-004 | CA-2 | MUST |
| CU-J03-04 | US-004 | CA-4, CA-12 | MUST |
| CU-J03-05 | US-004 | CA-11, CA-7 | MUST |
| CU-J03-06 | US-034 | CA-3, CA-4, CA-6 | MUST |
| CU-J03-07 | US-034 | CA-7 | MUST |
| CU-J03-08 | US-034 | CA-2, CA-8 | MUST |
| CU-J03-09 | US-004 | CA-8, CA-9 | MUST |
| CU-J03-10 | US-002/004 | CA-1 (US-002), CA-14 | MUST |
| CU-J03-11 | US-004 | CA-15, CA-8 | MUST |
| CU-J03-12 | US-004 | CA-14 | MUST |
| CU-J03-NEG-01 | US-004 | CA-11 | MUST |
| CU-J03-NEG-02 | US-004 | CA-7 | MUST |
| CU-J03-NEG-03 | US-034 | CA-10 | MUST |
| CU-J03-NEG-04 | US-004 | CA-11 §4 | MUST |
| CU-J03-NEG-05 | US-004 | CA-3 | MUST |

---

## Resumen de Cobertura

| US | CAs Totales Epic | CAs Cubiertos J-03 | Cobertura |
|----|:-----------------:|:------------------:|:---------:|
| US-004 | 17 | 12 (CA-1,2,3,4,7,8,9,10,11,12,14,15,17) | 71% |
| US-034 | 10 | 7 (CA-2,3,4,5,6,7,8,10) | 70% |
| US-001 | 31 | 1 (CA-01 visibilidad en Workdesk) | 3% |
| US-002 | 8 | 1 (CA-1 reclamación) | 13% |

> **Nota:** US-001 y US-002 tienen cobertura UAT exhaustiva en sus archivos individuales (`casos_uso_uat_us001_sprint1.md`, `casos_uso_uat_us002.md`) y en los Journeys J-02 y J-04. Aquí se valida únicamente su integración con el flujo de Webhook/Intake. Los CAs restantes de US-004 (CA-5, CA-6, CA-13, CA-16) serán cubiertos por J-08 (Resiliencia) y J-SEC (Seguridad).
