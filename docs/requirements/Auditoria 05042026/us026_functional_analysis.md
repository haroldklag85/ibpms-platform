# Análisis Funcional Definitivo: US-026 (Portal del Cliente Externo - B2C/B2B)

## 1. Resumen del Entendimiento
La US-026 abstrae la robustez de Camunda de los ojos de los clientes corporativos/naturales. Funciona como una capa BFF (Backend For Frontend) que lee limitadamente qué etapa va, expone un Tracker simple y, audazmente, permite a Camunda asignarle Tareas Directas a un CUST_ID y que este la diligencie desde el Portal B2C.

## 2. Objetivo Principal
Mitigar tráfico de call-center ofreciendo Trazabilidad viva, descarga de documentos generados finales, e Ingesta Colaborativa (El cliente siendo un operario de Camunda sin saberlo).

## 3. Alcance Funcional Definido
**Inicia:** Cliente accede via Identity Provider Externo (Ej Cognito) (CA-1).
**Termina:** El BFF extrae info, previene BOLA (CA-2) y provee descarga Temporal SGDEA.

## 4. Lista de Funcionalidades Incluidas
- **Aislamiento BOLA/IDOR (CA-2):** Control anti-lecturas horizontales arrojando 404 intencionalmente frente a manipulaciones de Id.
- **Data Masking (CA-3):** Esconde Score IA, Comentarios Backoffice, Confidence Ratings.
- **Micro UI Inyectado (CA-5):** Camunda transfiere flujo hacia "User Task [Role: Customer]". El cliente lo ve en B2C, diligencia el form de Zod (iForm) y finaliza la tarea empujando el motor hacia adelante.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Vulnerabilidad Crítica de Compleción Directa de Tarea (⚠️ CA-5):** Promueve delegar un Form al cliente para que subsane documentación y hacer directamente `un POST a /complete` avanzando Camunda desde el exterior. **GAP:** El Task `/complete` nativo de los BPM engines requiere usualmente rol interno de "Assignee". Si la API pública B2C expone indirectamente el endpoint de Task Complete usando el Service Account interno, un cliente web con JWT Scope=Customer y conocimientos de Postman, podría forzar submits falsificados (By-pass de variables, auto-aprobarse reclamos) sobre sus Tareas Asignadas si el BFF omite hacer validación Semántica (Zod Validator de Servidor) antes de pasar el Submit al Motor.
- **Caducidad Sub-Zero en Sharepoints (⚠️ CA-4):** Usar URLs pre-firmadas (15 min) para bajar documentos legales finalizados. Muy seguro, pero exige proxy dinámico si el sistema ECM (Documental) no soporta Presigned URLs nativas (Como lo haría S3), añadiendo un Bottleneck transaccional severo en Java.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Chatbots interactivos con el Motor de BPM no contemplados. (Solo forms Zod estáticos Inyectados).

## 7. Observaciones de Alineación o Riesgos
La usabilidad "Data Masking" es imperativa y de estricto cumplimiento para GDPR. Nunca inyectar atributos crudos del Motor Camunda al Gateway REST que va de cara a la web abierta, porque la metadata exfiltraría nombres de analistas y correos corporativos sensibles.
