# Análisis Funcional Definitivo: US-029 (Ejecución y Envío de Formulario)

## 1. Resumen del Entendimiento
Gobernanza absoluta del acto de "Responder y Finalizar" una Tarea en el Workdesk (POST `/complete`). Trata del mecanismo de envío transaccional, cómo se blindan los adjuntos, cómo se aseguran los tokens asimétricos, los locks concurrentes (Idempotencia) y cómo se asegura el Backend antes de decirle a Camunda que avance el token en su BPMN.

## 2. Objetivo Principal
Cumplir el contrato vital del negocio (Enviar el trámite formuario) de manera **ACID y Zero-Trust**. Pretende asegurar que la data sea hiper-saneada (Zod Isomórfico), que dos personas no hagan doble sumisión, y que no se ahogue a Camunda inundándolo de JSONs enormes o binarios; aislando Camunda y guardando el grueso de los datos en CQRS.

## 3. Alcance Funcional Definido
**Inicia:** Desde que el usuario ingresa a la vista operativa del formulario (Pantalla 2) - recibiendo el DTO Megalítico vía patrón BFF.
**Termina:** Cuando la API orquestadora aprueba la persistencia local, destruye los borradores de LocalStorage vinculados a la tarea (RYOW), y notifica a Camunda del movimiento exitoso o, ante fallas, ejecuta el Rollback síncrono.

## 4. Lista de Funcionalidades Incluidas
- **BFF Mega-DTO:** Consolidación de un solo request inicial (Esquema + UI Layout + Data Histórica).
- **Zod Isomórfico en Backend:** Validación redundante anti-inyección con limpieza silenciosa `.strip()`.
- **Cifrado PII Draft:** Enmascaramiento local de borradores en el Storage.
- **Idempotencia (Anti Doble Clic):** Headers UUID inyectados para amortiguar re-sumisiones idénticas asíncronas.
- **Upload-First:** Archivos pesados pre-cargados a S3/SGDEA enviando sólo el UUID de la referencia al Backend central.
- **Escudo Integridad / Implicit Locking:** Bloqueos automáticos para colisiones transaccionales (Usuario adulterando UUIDs en JWT).
- **Saga Fallback:** Si Camunda Motors no responde, se mata el POST original y se prohíben 202 Asyncs.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Destrucción Prematura de Documentos (⚠️ CRÍTICO DE NEGOCIO):** El CA-13 impone un Cron Job nocturno con un TTL superior a 24 horas para incinerar archivos en la zona "temporal" si no hay `/complete`. Dado que los analistas guardan Drafts y pueden tardar >72 hrs en terminar y firmar un caso, cuando den click al botón, el UUID que envía el front apuntará a un archivo exterminado. Un UUID huérfano trabará todo.
- **Choque Temporal Draft vs BFF:** El CA-8 "Hibridación de Data Historica" dice que "Lazy Patching" no exige data del V2 si el caso V1 inició ayer. Pero, si el analista hizo draft, el LocalStorage puede corromperse al chocar contra la forma del DTO V2.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Envío de binarios directamente embebidos a Camunda (Estrictamente prohibido por reglas ACID).
- Transacciones persistentes (Guardado "202 Aceptado") por delegación asíncrona en eventos rotos.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Activo:** Es mandatorio corregir el TTL en Base de Datos de Documentos Temporales. Redactaré un alcance SRE indicando que el TTL debe corresponder al *Tiempo Máximo Parametrizado de Expiración del Token / Tarea* en todo el Workflow, jamás unas escuálidas 24 horas.
