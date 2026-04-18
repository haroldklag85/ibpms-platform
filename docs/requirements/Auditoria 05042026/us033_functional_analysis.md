# Análisis Funcional Definitivo: US-033 (Catálogo de API y Mapeo Visual)

## 1. Resumen del Entendimiento
La US-033 es el corazón de la orquestación (EAI) del iBPMS. Sostiene un "Integrator Hub" visual (BFF/APIM Proxy local) donde los Diseñadores BPMN instalan y configuran la fontanería externa hacia sistemas Core (ERP/CRM/SGDEA), mediante Mapeo Drag&Drop sobre REST sin requerir scripting duro estándar, asegurando criptografía saliente en tránsito y TLS.

## 2. Objetivo Principal
Aislar al orquestador Camunda de la mugre sintáctica externa (`XML` legacy, `form-data` blobs) forzando una capa de abstracción donde el Business Architect solo entienda `JSON in/out`. Todo lo ajeno al Rest transaccional limpio es delegado a plataformas APIM Middleware perimetrales de la Organización.

## 3. Alcance Funcional Definido
**Inicia:** Arquitecto registra una credencial/API Base.
**Termina:** El Conector entra en la paleta global y Camunda lo llama delegándole la retentiva de Timeout, Retry-Backoff y Headers.

## 4. Lista de Funcionalidades Incluidas
- **Oclusión Parcial (Blackbox Legacy) (CA-3574):** Rehazos estrictos y nativos a integraciones SOAP/WSDL; deben atravesar APIM externos corporativos.
- **DLP y Anti-SSRF (CA-3609/CA-3629):** Logs del Hub filtran datos confidenciales accidentalmente escupidos. Bloqueo severo en IPs/Resoluciones internas limitadas (`localhost`, base de datos real).
- **Traducción Multipart Form (CA-3640):** Transacción nativa de BLOBs/Anexos por `multipart/form-data` para puentear SharePoint Docs sin saturar variables Camunda en Base64.
- **Mocking en Vivo "Playground" (CA-3619):** Prueba de APIs "Run/Probar" On-the-fly con payload artificial. 
- **Paginación Automática Iterativa (CA-3660):** Auto-recolecta N Páginas por Offset en un solo DataObject limitando Spans en Camunda.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Bombardero Local Code Injector "Sandboxed" RCE Vector (⚠️ CA-3682):** Indica que se le debe permitir al Arquitecto inyectar código custom JS/Python para payloads imposibles de mapear Gráficamente. **GAP de Seguridad y Gobernanza Severo:** Un sistema Low-Code no debería permitir a un Arquitecto de Negocio introducir ejecuciones de código imperativo en Runtime Java. Es el vector número uno de ataques RCE (Remote Code Execution) si el Sandboxing (GraalVM, Docker aislado) no es militar. Corromper el heap, generar loops infinitos o llamadas externas (`curl`) asestaría The iBPMS Platform. Se recomienda reemplazar scripts por Transformaciones de Declarativas (JSONPath, JOLT, DataWeave) 100% deterministas y no Turing Completas.
- **Caché Transaccional Agresivo Peligroso (⚠️ CA-3645):** Promueve salvar llamadas repetidas mediante Caché parametrizable (TTL=10 min). En un entorno BPMN Altamente concurrente con mutabilidad (Write-Path), una tarea puede modificar (POST) en el CRM el valor de una cuenta de cliente, y el paso siguiente a los 2 minutos consumir (GET) la cuenta y traer la data de hace 10 minutos (Stale Date). Configurable o no, el Hub no debe cachear Data Operacional sin invalidaciones Event-Driven (Cache Eviction por Webhooks de modificación). Su alcance debería quedar para "Catálogos Estacionarios Inmutables" (Países, Sucursales), de los contrario es Suicidio de Consistencia de Datos.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Autodescubrimiento SOAP o OData (Solo REST JSON V1).
- Orquestación automática de Variables de Entorno de Prod/QA (Manual V1, V2 Automático).
- Permisos RBAC aislados por conector (Todos ven todo).

## 7. Observaciones de Alineación o Riesgos
La idea de soportar Token Refresh (CA-3671) autónomamente tras una caída o vencimiento JWT (60 minutos de expiración de Oauth2) protege y estabiliza ejecuciones BPMN asíncronas de duración transaccional larga que normalmente colapsan silenciosamente con tokens HTTP 401 sin delegación automática. Gran diseño resiliente. 
