# Análisis Funcional Definitivo: US-004 (Iniciar un Proceso mediante Webhook / O365 Listener)

## 1. Resumen del Entendimiento
La US-004 es la puerta de entrada (Gateway Inbound) de flujos no controlados por operadores físicos en el Portal iBPMS. Define la arquitectura con la que sistemas de terceros (Outlook, MS Graph, Zendesk) disparan nuevos procesos de Camunda (Start Event) mediante REST POST de forma desatendida. Incorpora capas fuertes de defensa perimetral para proteger al Motor BPM.

## 2. Objetivo Principal
Automatizar en un 100% la fase de inicio de los procesos transaccionales masivos (radicación "Zero-Touch"), mientras se resguarda violentamente a Camunda de Mail-Loops, payloads basura malformados (Catastrophic JSONs) y denegaciones de servicio causadas por buzones colapsados en internet.

## 3. Alcance Funcional Definido
**Inicia:** Desde que la petición POST atraviesa el perímetro externo hacia la ruta pública `/intake`.
**Termina:** Con la instancia creada exitosamente, el encolamiento en RabbitMQ del payload (si Camunda está Offline) o el rechazo tajante del Payload en Perímetro (HTTP 4xx). Termina también en la asignación de una "Tarea de Pre-Triaje" humano.

## 4. Lista de Funcionalidades Incluidas
- **Idempotencia Asíncrona:** Descartar payloads duplicados síncronos basándose en `id_mensaje`.
- **Filtro Anti-Robots:** Rechazo tajante a cuentas `no-reply` o automáticas.
- **Trazabilidad de Payloads Huérfanos:** Persistencia cruda (Data Lake temporal) de JSONs malformados para depuración IT (HTTP 400).
- **Lista Blanca DNS:** Si el dominio emisor no está matriculado en BD de la compañía, aborta en capa 0 (HTTP 403).
- **Safety Net RabbitMQ:** Buffer SRE elástico si el Engine BPMS de Camunda muere por completo.
- **Topes de Payload:** Límites estrictos configurables (Ej: 10 MB) para descarga rápida.
- **Intake Triage Humano:** No enruta y despacha el trámite pesado al área inmediatamente; fuerza una validación humana puente.
- **Auth Híbrida:** HMAC Secrets para Graph y Bearer normal para Legacy.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Fuga Lógica en Pre-Triaje (⚠️ CA-8 & CA-9):** Obliga a crear una "Tarea de Pre-Triaje" en la Pantalla 16 para que un humano verifique qué es el correo y oprima *[Aprobar y Crear Caso]*. Al hacerlo no nombra **qué rol asume esta tarea** ni a qué Swimlane/Pool se envía; ¿Mesa de Ayuda? Si este rol falla o no está asignado, los Webhooks válidos se irán a un limbo oscuro administrativo perpetuo.
- **Idempotencia Infinita (⚠️ CA-1):** Manda a verificar en la tabla de transacciones de entrada el `id_mensaje`. No indica un TTL temporal para esta memoria caché/idempotente (Redis). Si el sistema recuerda `id_mensajes` toda la vida, la tabla transaccional crecerá indiscriminadamente en producción hasta ahogar las métricas. Se requiere el establecimiento de un índice limitante temporal (Ej: Guardar hash del id_mensaje por máximo 7 días).

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógicas semánticas mediante IA (AI Triaging) como OCR de adjuntos y tipificación automática textual en el Webhook. Solo validación estructural e inicio manual en Triaje.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Crítico de Negocio:** La falta de definición respecto a *quién toma el Intake Triage* generará cuellos de botella el Día 1 en Producción si miles de transacciones se van a una bandeja que nadie tiene permiso "Default" para ver.
