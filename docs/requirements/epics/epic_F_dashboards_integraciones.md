# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---

### US-009: Visualizar Salud del Proceso (BAM Dashboard)
**Como** Líder de Operaciones / Gerente
**Quiero** acceder visualizar un dashboard analítico con el conteo de tareas retrasadas y volúmenes operativos
**Para** identificar cuellos de botella y reasignar cargas de trabajo en tiempo real.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Process Health Analytics
  Scenario: Renderizado exitoso del Dashboard de Grafana (CA-1)
    Given un usuario autenticado con Rol "Gerente_Operaciones"
    When la aplicación frontend solicita renderizar el iframe interactivo en la Pantalla 5
    Then el API Gateway debe emitir un JWT de corta duración (Grafana Auth Proxy) con rol de "Viewer"
    And el iframe debe renderizar correctamente el tablero pasándole variables de entorno `&var-TenantID=T123`
    And el dashboard debe mostrar obligatoriamente un panel de "Tareas Vencidas por SLA" consultando la vista materializada `vw_task_sla_breach`

  Scenario: Aislamiento Estricto de Datos (Multi-Tenancy) (CA-2)
    Given la arquitectura SaaS multi-cliente de la plataforma iBPMS
    When el JWT de Grafana es generado por el Backend para renderizar la Pantalla 5
    Then el token debe inyectar criptográficamente el `Tenant_ID` del usuario activo
    And la Base de Datos o la consulta subyacente de Grafana debe forzar obligatoriamente el filtrado por este Tenant (Ej. Row-Level Security) previniendo fugas de datos operativos hacia clientes vecinos.

  Scenario: Capacidad de Perforación Interactiva (Drill-Down UI) (CA-3)
    Given el Dashboard visual en la Pantalla 5 que muestra una alerta de "15 Tareas Bloqueadas"
    When el gerente hace clic sobre el segmento de la gráfica circular
    Then el sistema debe interceptar el evento de anclaje de Grafana
    And redireccionar la UI del iBPMS automáticamente a la Bandeja de Trabajo (Pantalla 1) o Hub Ágil (Pantalla 10)
    And pre-filtrar la vista exacta con las 15 tarjetas implicadas para tomar acción inmediata.

  Scenario: Segregación de Roles para Monitoreo Activo (RBAC) (CA-4)
    Given un empleado raso con rol "Analista" o "Ejecutor" intentando acceder a URL de reportes macro
    When navegue hacia la Pantalla 5 (BAM)
    Then el Frontend interceptará la ruta y mostrará un mensaje de "Acceso Denegado"
    And el Backend rechazará la generación del Token de Grafana, reservando esta vista exclusivamente para jerarquías directivas (Ej. `Gerente_Operaciones`, `Scrum_Master`).

  Scenario: Frecuencia de Refresco Asíncrona (Protección Transaccional) (CA-5)
    Given el inmenso volumen de eventos emitidos en tiempo real por el motor Camunda
    When Grafana ejecute los queries analíticos pesados para renderizar la Pantalla 5
    Then NO atacará directamente la base de datos transaccional caliente (Master DB)
    And leerá de una Base de Datos Analítica o Réplica (Ej. Elasticsearch o DataWarehouse) alimentada por un CronJob/CDC que se actualiza estrictamente cada 10 minutos para proteger la estabilidad del servicio en vivo.

  Scenario: Autoservicio de BI Analítico (Grafana Editor Nativo) (CA-6)
    Given que los tableros pre-cargados (Vencimientos, Costos, Ciclos) no cubren una métrica atípica solicitada por un cliente
    When el gerente seleccione la opción "BAM Avanzado" en la Pantalla 5
    Then el iBPMS cargará la Interfaz Nivel Editor Nativa de Grafana embebida
    And otorgará permisos formales de "Editor" al usuario, permitiéndole arrastrar bloques, cambiar colores de tortas y personalizar sus propias métricas ad-hoc limitadas a su Tenant_ID.

  Scenario: Aplanamiento de Datos Transaccionales para Analítica Rápida (Data Flattening / CDC) (CA-7)
    Given que el motor de Dashboards (Grafana) necesita graficar variables de negocio almacenadas en los JSON de Camunda
    When una tarea se completa o una variable es inyectada en el motor
    Then la arquitectura TIENE PROHIBIDO permitir que Grafana haga queries complejos (Full Table Scans) sobre las tablas operativas Blob de Camunda (`ACT_RU_VARIABLE`).
    And el Backend iBPMS implementará un proceso asíncrono de "Aplanamiento" (Change Data Capture o Event Listener).
    And extraerá las variables estratégicas del JSON y las insertará en una tabla relacional plana y columnar (Ej: `ibpms_business_metrics_flat`).
    And Grafana consumirá exclusivamente esta tabla plana, garantizando tiempos de carga en milisegundos sin impactar el Core.


```
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards y Panel de Control - BAM).

---


### US-018: Métricas de desempeño y calidad
**Como** líder de operación/servicio
**Quiero** ver métricas de desempeño del asistente
**Para** medir Retorno de Inversión (ROI) y mejora continua.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Cuadro de Mando de Desempeño Inteligente (AI Dashboards)
  Scenario: Consolidación de retorno y eficiencia
    Given el Líder de Operación ingresa al dashboard
    Then puede reportar la tasa de aceptación, edición, rechazo, y tiempo medio de respuesta
    And visualizar las acciones orgánicas creadas segmentadas por buzón y por idioma
    And configurar comparativas "antes vs después" mediante un 'baseline' histórico
    And visualizar en un panel de control la telemetría de fallos de integración (Graph/CRM)
```
**Trazabilidad UX:** Wireframes Pantalla 5 (Dashboards / BAM).

---

### US-010: Generar y Descargar PDF a partir de datos del caso
**Como** Analista / Gestor Documental
**Quiero** que el sistema ensamble un PDF inmutable (Ej. un Contrato) con los datos finales del caso
**Para** enviarlo a firma o entregarlo al cliente sin errores de "copy-paste".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Legal PDF Generation from Template
Scenario: Ensamblar PDF usando plantilla del SGDEA y Variables de la Instancia (CA-1)
    Given la instancia de proceso "PI-888" finalizada en estado "APPROVED"
    And la plantilla "Contrato_Laboral_V3.docx" almacenada en el repositorio maestro
    When el usuario realiza un POST a "/api/v1/documents/generate/PI-888"
    Then el motor Documental (FOP/PDFBox) inyecta el árbol `json_variables` en las etiquetas `<<key>>` de la plantilla
    And registra el checksum SHA-256 en `ibpms_audit_log` para inmutabilidad legal
    And el sistema retorna HTTP STATUS 200 OK con un enlace temporal de SharePoint Graph API (Pre-Authenticated Link) expirable en 15 minutos para su visualización.

  Scenario: Tolerancia a Fallos por Variables Ausentes (Missing Keys) (CA-2)
    Given una plantilla `.docx` que incluye la etiqueta `<<segundo_apellido>>` obligatoria en su sintaxis
    When el motor documental (FOP) sea invocado y la variable no exista o sea NULA en el payload enviado por Camunda
    Then el motor NO debe abortar la transacción (Evitando HTTP 400 y rotura de flujos de negocio)
    And debe sobrellevar la carencia inyectando automáticamente la frase "N/A" o un espacio en blanco seguro en el documento final.

  Scenario: Expansión Dinámica de Tablas y Vectores (Bucles) (CA-3)
    Given que el JSON de entrada contiene un Array de objetos (Ej: Lista de 5 productos comprados)
    When la plantilla documental contenga sentencias iterativas de tipo `#foreach` en filas de una tabla de Word
    Then el motor SGDEA clonará la fila tantas veces como elementos existan en el array inyectando sus respectivas propiedades, posibilitando documentos hiper-dinámicos de longitud variable en la V1.

Scenario: Gobernanza de Persistencia (SharePoint Vault vs Vuelo Efímero) (CA-4)
    Given la invocación del servicio REST `/api/v1/documents/generate`
    When el proceso configure explícitamente el flag `storageMode`
    Then el Back-End acatará rígidamente la directriz:
    And Si es `EPHEMERAL`: El documento se renderiza, se entrega el link de 15min y se destruye físicamente de RAM/Disco del servidor.
    And Si es `PERSISTENT`: El PDF se traslada e inyecta inmutablemente en Microsoft SharePoint (Única Bóveda Oficial SGDEA), amarrado a la sub-carpeta del UID del Expediente (Acorde a la US-035), garantizando registro perenne exigible por Ley, evadiendo cobros duplicados en S3/Azure.

  Scenario: Acorazado Forense y Firma Digital del Documento Físico (CA-5)
    Given la configuración de una plantilla de Alto Riesgo Legal
    When el motor finaliza el ensamblado del PDF final
    Then NO se limitará a guardar el Hash SHA-256 en la base de datos (ibpms_audit_log)
    And incrustará en paralelo un "Certificado Criptográfico PKI" estructural dentro del mismo archivo PDF
    And y estampará visualmente en los márgenes de las páginas un Código QR (o Sello de Agua Legal) verificable externamente, asegurando la no-repulsa de autoría.

  Scenario: Versión Retroactiva Activa en Auditorías Históricas (CA-6)
    Given un Cliente instanciado hace 2 años cuando regía el "Contrato Laboral V1"
    When un auditor re-visite en Pantalla 12 dicho caso y el sistema requiera re-descargar o consultar su contrato
    Then el motor SGDEA buscará y ensamblará el PDF contra la plantilla V1 almacenada en el repositorio histórico (Time-Travel Rendering)
    And prohibirá rotundamente la utilización de la plantilla "V4" actual para casos pasados, protegiendo las cláusulas vigentes al momento de la firma original.
```
**Trazabilidad UX:** Wireframes Pantalla 12 (Bóveda Documental y Generación).

---


### US-035: Integración SharePoint y Auditoría Documental
**Como** Analista / Auditor de Cumplimiento
**Quiero** que el iBPMS gestione los expedientes en SharePoint manteniendo trazabilidad matemática estricta
**Para** garantizar que la evidencia aportada por clientes o generada por IA sea inmutable, centralizada y legalmente auditable.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: SharePoint Vault and Single Source of Truth
  Scenario: Almacenamiento Delegado Basado en Enlaces (CA-1)
    Given la subida de un documento de 10MB en la Pantalla 16 (Intake)
    Then la arquitectura postula a SharePoint como "Single Source of Truth" físico de los PDFs
    And el iBPMS únicamente almacena en su base de datos relacional la URL directa del activo, su ID referencial y la Metadata de auditoría, evadiendo duplicación de costos de Storage (S3).

  Scenario: Creación Dinámica de Taxonomía Sub-Carpetas (CA-2)
    Given un Arquitecto modelando una captura documental en la Pantalla 6
    When configura la actividad paramétricamente para generar "Casos Independientes"
    Then el iBPMS expone un Pop-Up para definir la ruta base en SharePoint
    And en tiempo de ejecución, el motor invoca el API de SharePoint creando proactivamente la sub-carpeta unívoca para ese expediente (Ej: `/ProcesoA/Caso1234/`) antes de inyectar los documentos.

  Scenario: Elusión de Seguridad Perimetral SharePoint (Service Account) (CA-3)
    Given un Usuario de Negocio que posee Rol de Lectura en el iBPMS pero carece de licencia SharePoint
    Then el módulo documental utiliza un App Registration (Súper Cuenta de Servicios - EntraID) para extraer el PDF del repositorio
    And lo proyecta en la Pantalla 12 evadiendo los bloqueos nativos de SharePoint frente al usuario final.
    # NOTA: Diferido a V2 el "RBAC Cruzado" (User Delegation OAuth2).

  Scenario: Marcado Metadato para Tablas de Retención V1 (CA-4)
    Given la necesidad legal de destruir tutelas tras 5 años (TRD)
    Then en la V1, el iBPMS inyecta una Fecha de Expiración como Metadato estructurado directo a la taxonomía de SharePoint
    And delega la incineración automatizada (Deletion Policies) al motor nativo de Microsoft 365.
    # NOTA: Diferido a V1.2 el cronómetro destructor interno propio del iBPMS.

  Scenario: Inmutabilidad por Versionamiento Incremental (CA-5)
    Given un analista intentando "Reemplazar" un contrato que quedó mal redactado en el sistema
    Then en el expediente de la Pantalla 12 el botón de sobre-escritura destructiva está censurado
    And forcejea la obligatoriedad funcional de subir el nuevo archivo bajo el mecanismo de "Nueva Versión" (v1.1) reteniendo acceso forense e inmutable al borrador v1.0.

  Scenario: Despacho de Integración E-Signature (CA-6)
    Given un documento generado que requiere validez legal del firmante
    Then el Módulo Documental posee el andamiaje (Hooks) para interactuar vía API con proveedores de Firma Digital (Ej: DocuSign/AdobeSign)
    And actualiza el estado del expediente en la Pantalla 12 a "Firmado" una vez los Webhooks Inbound confirman el OTP legal del ciudadano.

  Scenario: Componente Visor Empotrado (Iframe Preview) (CA-7)
    Given la necesidad de leer un anexo para tomar una decisión en un proceso
    Then la interfaz de Tareas (Pantalla 12 empotrada en Workdesk) renderiza un Visor de Documentos Nativos asíncrono
    And impide obligar al analista a descargar el PDF ciegamente hacia las carpetas `Descargas/` locales de su Sistema Operativo, reteniendo el foco en el flujo iBPMS.

  Scenario: Blindaje Criptográfico Anti-Fraude (SHA-256) (CA-8)
    Given la delegación del archivo físico hacia el servidor SharePoint de TI (CA-1)
    Then en el milisegundo anterior a la carga, el iBPMS calcula el HASH criptográfico SHA-256 del binario original
    And sella esta huella matemática inmutablemente en la Base de Datos transaccional del iBPMS para detectar futuras y silenciosas alteraciones directamente en SharePoint.

  Scenario: Lector Óptico Diferido (OCR Zonal) (CA-9)
    Given imágenes de documentos de identidad (Cédulas) escaneadas
    Then en V1 estas se gestionan como Binary/Image objects convencionales
    # NOTA: Diferido a V2 el procesamiento neuronal OCR para extracción estructurada de texto zonal.

  Scenario: Inyección Activa de Metadata de Negocio (CA-10)
    Given el traspaso exitoso del documento PDF hacia la granja SharePoint
    Then el iBPMS adjunta un Payload extendido de Propiedades Personalizadas (Ej: `ibpms_processName`, `ibpms_caseStatus`) al nodo del documento
    And permitiendo a los usuarios externos buscar documentos utilizando las herramientas de búsqueda Nativas de O365 mediante filtros semánticos del negocio.

  Scenario: Límite Infraestructural de Carga y Silencio Parcial (CA-11)
    Given un usuario intentando subir un archivo estúpidamente pesado (Ej: Video 4K de 5GB)
    Then la Pantalla 12 intercepta la carga en el Fronend guiada por un parámetro global `MAX_FILE_SIZE` (Ej: 50MB) configurado por IT
    And emite un error de UI "genérico" o "silencioso" al usuario final (Ej: "Error en la Carga, archivo muy pesado")
    And simultáneamente dispara una alerta técnica detallada en el Log del Administrador del Sistema para auditoría de abusos.

  Scenario: Lista Blanca Estricta de Extensiones (MIME Types) (CA-12)
    Given el riesgo inminente de inyección de Malware (Ej: `.exe`, `.bat`)
    Then la Bóveda SGDEA opera exclusivamente bajo arquitectura de "Lista Blanca" (Whitelist)
    And rechaza radicalmente cualquier archivo que no esté explícitamente parametrizado (Ej: `application/pdf`, `image/jpeg`, `application/msword`).

  Scenario: Visibilidad Transparente de Atributos de Auditoría (CA-13)
    Given la Tabla/Grilla visual del expediente en Pantalla 12
    Then el diseño UI no esconde la data legal
    And expone nativamente en columnas primarias la "Fecha Extrema (Vencimiento TRD)" y el "Hash SHA-256" para que el analista confirme la inmutabilidad física con un solo vistazo.
    # NOTA: Opciones de "Botón del Pánico/Censura de archivos erróneos" diferido a V2.

  Scenario: Consolidación Multi-Anexo (PDF Merge Tool) (CA-14)
    Given un expediente con 10 archivos PDF fragmentados que deben enviarse a una Superintendencia
    Then la Pantalla 12 posee un botón de acción masiva `[Combinar PDFs (Merge)]`
    And el iBPMS compila transitoriamente las páginas de los archivos seleccionados en un único documento maestro PDF sin corromper los originales.

  Scenario: Delegación de Escaneo Anti-Malware (CA-15)
    Given el ingreso de nuevos documentos a la Bóveda
    Then la arquitectura V1 asume ciegamente la robustez de los Defensores Nativos de Microsoft
    And confía en que SharePoint 365 interceptará virus silenciosamente, librando al iBPMS de conectar con AntiVirus dedicados en este MVP.
    # NOTA: Herramientas de "Anotación Gráfica (Highlighting)" sobre PDFs diferidas a V1.2.

  Scenario: Búsqueda Semántica Delegada (Full-Text Search) (CA-16)
    Given un analista utilizando el buscador global del iBPMS para buscar la palabra "Tornillo"
    Then el iBPMS delega la consulta profunda al motor nativo de indexación de SharePoint
    And este último busca el término *dentro del contenido textual* de los PDFs y devuelve los matches, inyectando los resultados en la UI del iBPMS.

  Scenario: Orquestación SGDEA a Inteligencia RAG (Vectorización Segura) (CA-17)
    Given un analista en Pantalla 12 que decide oprimir `[Usar para IA]` sobre un contrato de 100 páginas
    Then el módulo documental envía asíncronamente el ID de ese archivo a la "Cola de Eventos IA (RabbitMQ - CA-34)"
    And el cerebro LLM procede a desencolar y devorar el contenido (si es PDF o WORD habilitado) para poblar su memoria de Embeddings sin congelar la ventana del usuario.
	
	Scenario: Storage Garbage Collector para Archivos Huérfanos (Evitar Fuga Financiera) (CA-18)
    Given el patrón arquitectónico "Upload-First" (US-029) donde los archivos pesados se suben a la sub-carpeta `/upload-temp` de manera asíncrona temprana
    When el operario humano abandona la tarea, cierra la pestaña o descarta un Intake sin oprimir jamás el botón de [Enviar] Formulario
    Then esos archivos se convierten en "Archivos Huérfanos" (Binarios sin un Process_Instance_ID asociado en BD).
    And el iBPMS ejecutará un CronJob nocturno perentorio a las 03:00 AM
    And el Job consultará la API de Storage eliminando físicamente (Hard-Delete) cualquier archivo en `/upload-temp` que supere las 24 horas de antigüedad, tapando la hemorragia de costos por almacenamiento de basura no transaccional.
	
	
```
**Trazabilidad UX:** Wireframes Pantallas 12,16 y 6.

---

### US-033: Catálogo de API y Mapeo Visual
**Como** Arquitecto Técnico / Interfaz
**Quiero** disponer de un Hub centralizado para configurar conectores HTTP
**Para** asociar de forma visual las variables de procesos BPMN con endpoints externos, gestionando tolerancia a fallos y reglas de seguridad de nivel Enterprise.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: API Connector Configuration and Resiliency
  Scenario: Exclusividad Rest JSON (Delegación a APIM) (CA-1)
    Given la necesidad de interactuar con un sistema Core de tecnología antigua (Ej: SOAP XML)
    Then el Integration Hub del iBPMS emite su comunicación internamente en formato estándar `REST JSON`
    And la arquitectura delega imperativamente la conversión de protocolos a la capa de API Management (APIM) middleware corporativo, manteniendo el iBPMS libre de librerías legacy.

  Scenario: Compatibilidad de Autenticación Segura (CA-2)
    Given que el Arquitecto configura un nuevo Conector en la Pantalla 11
    Then el formulario debe disponer de soporte imperativo para inyección de Headers `Basic Auth` y credenciales `OAuth 2.0 / JWT`
    And estos credenciales deberán estar almacenados en la Bóveda de Secretos encriptada, jamás en texto plano.

  Scenario: Ausencia de Agentes Locales On-Premise (CA-3)
    Given la necesidad de conectar el iBPMS en la Nube con el ERP interno corporativo
    Then el iBPMS no provee "Agentes VPN Inversos" ni demonios de instalación local
    And la arquitectura asume que el acceso infraestructural está resuelto mediante puertos habilitados en el FireWall corporativo bajo responsabilidad exclusiva del área de IT.

  Scenario: Tolerancia a Caídas y Retry Asíncrono (CA-4)
    Given la invocación a la API externa de SharePoint que retorna error HTTP 500
    Then el motor encola la petición fallida en base de datos (Dead Letter Queue controlada)
    And realiza intentos de backoff silenciosos (Ej: cada 5 minutos por 1 hora)
    And si persisten los fallos, enruta el task a modalidad de "Fallback" alertando al analista humano en el Workdesk de la interrupción integrativa.

  Scenario: Data Mapping Gráfico (Drag and Drop) (CA-5)
    Given la configuración de un catálogo de retorno (Ej: CRM devuelve el perfil del cliente)
    Then la interfaz expone visualmente un árbol JSON de variables de entrada a la izquierda vs variables del BPMN a la derecha
    And permite dibujar conexiones (Drag & Drop mapping) sin requerir que el Arquitecto estructure JSONPath a mano.

  Scenario: Trazabilidad y Logs Híbridos (CA-6)
    Given una prueba de conexión fallida por Timeout
    Then la Pantalla 11 emite en su consola técnica el Log Raw inmediato del error para el diseñador
    And las ejecuciones fallidas en producción se delegan adicionalmente al sistema APIM o a la bitácora interna de Kibana para trazabilidad forense.

  Scenario: Validación Profunda de Payload Fantasma (CA-7)
    Given una API mal implementada que devuelve un estatus `HTTP 200 OK` pero el cuerpo del JSON incluye el key `{"error": true, "code": "USER_NOT_FOUND"}`
    Then el Hub permite al Arquitecto declarar "Reglas Límite de Payload" para que el iBPMS separe visualmente si una petición fue exitosa lógicamente (no solo analizando el status header HTTP).

  Scenario: Censura DLP en Logs del Sistema (CA-8)
    Given que la API devuelve inintencionalmente la llave primaria secreta o contraseña de una transacción
    Then la política general de censura y DLP intercepta el contenido saliente hacia el log (`stdout`/Kibana/Pantalla11)
    And enmascara los atributos coincidentes (Hashes visuales ocultos `***`) bloqueando la exposición a un desarrollador o Arquitecto BPMN no autorizado.

  Scenario: Directorio Global Reutilizable de Contenedores (CA-9)
    Given un Arquitecto a punto de conectar la extracción del SharePoint en un proceso
    Then la UI de "Agregar Conector" en la Pantalla 6 ofrece primero un buscador sobre el "Directorio de Conectores Registrados" 
    And permite re-utilizar el conector genérico sin volver a ingresar claves ni endpoints globales, favoreciendo el reciclaje.

  Scenario: Componente Playground de Pruebas (CA-10)
    Given que el Arquitecto terminó el Mapping Drag and Drop
    Then dispone de un botón `[▶️ Run / Probar Conector]` en la misma Pantalla 11
    And puede inyectar variables estáticas Dummy obteniendo el Raw Response de SharePoint/CRM en ese instante para verificar funcionamiento antes de comitear al catálogo.

  Scenario: Parametrización Humana de Timeouts (CA-11)
    Given un conector que apunta a un ERP legacy lento
    Then la interfaz de Pantalla 11 expone un input para definir `Timeout (ms)` exacto por Conector
    And previene que un solo API sature el motor Camunda por quedarse en estado colgante esperando infinitamente.

  Scenario: Lista Negra de Seguridad de Red (SSRF Prevention) (CA-12)
    Given un Arquitecto intentando mapear un Endpoint hacia un host interno malicioso (Ej: `localhost`, `127.0.0.1` o IPs locales de la BD)
    Then el sistema de Validación de Guardrails de IT intercepta el guardado
    And rechaza conectores que apunten a dominios listados en la "Blacklist Confidencial" configurada por el equipo de ciberseguridad.

  Scenario: Parseo Inteligente de Fechas (CA-13)
    Given una respuesta de CRM en donde el campo `birth_date` llega formateado de forma exótica (`20241231`)
    When el usuario arrastra la variable al Drag & Drop
    Then el Hub ofrece una opción "Forzar Formateo de Fecha"
    And convierte automáticamente el valor al estándar corporativo ISO-8601 impidiendo errores de parseo en el motor BPMN más adelante.

  Scenario: Interfaz Multipart/Form-Data para Anexos Pesados (CA-14)
    Given la necesidad imperativa de recuperar y enviar PDFs al SharePoint
    Then el Hub de Integración no se limita únicamente al Content-Type `application/json`
    And provee soporte técnico transparente para subida y descarga asíncrona de Binary Large Objects (BLOBs) mediante `multipart/form-data`.

  Scenario: Agresivo Ahorro de Red por Caché en Memoria (CA-15)
    Given 50 procesos BPMN simultáneos pidiendo un catálogo inmutable (Ej: Catálogo de Sucursales ERP)
    Then el Arquitecto puede encender el "Switch de Caché" interactivo en el Conector
    And parametrizar un TTL (Ej: Valid for 10 min) para que el iBPMS responda instantáneamente desde RAM local evadiendo 49 llamadas de red innecesarias al ERP.

  Scenario: Despliegue Manual de Entornos V1 / Variables V2 (CA-16)
    Given la necesidad de apuntar conectores a Producción (Ej: de `crm-qa` a `crm-prod`)
    Then en el alcance de V1, el Arquitecto de integraciones debe actualizar las URLs manualmente
    # NOTA: Diferido a V2: Orquestación automática transversal mediante Variables de Entorno (`{{crm_base_url}}`).

  Scenario: Autonomía de Firmas Criptográficas de Payload (CA-17)
    Given una integración hacia una pasarela bancaria que exige firma HMAC-SHA256
    Then la configuración del Conector exhibe un panel de `Security Signatures`
    And permite auto-firmar ciegamente el payload adjuntando la rúbrica matemática en los Headers garantizando no-repudio técnico.

  Scenario: Traversado Nativo de Paginación Recursiva (CA-18)
    Given que el CRM expone un listado masivo en páginas pequeñas (offset/limit de a 100)
    Then el Conector es consciente de estructuras de paginación
    And permite configurar la navegación automática "NextPage" hasta obtener el dataset completo sin que el Arquitecto deba modelar un 'For Loop' grotesco en el BPMN.

  Scenario: Versionamiento Estricto No Destructivo (CA-19)
    Given un Arquitecto modificando el Conector "CRM Cliente v1" que está amarrado ya a 50 procesos vivos
    When agrega un campo obligatorio nuevo 
    Then el iBPMS prohíbe el Sobre-escritura instantánea (la cual rompería la empresa)
    And fuerza el guardado estricto como nueva reliquia inmutable "CRM Cliente v2", forzando una migración gradual proceso por proceso.

  Scenario: Refreshing Invisible de Identidades Temporales (CA-20)
    Given una conexión JWT/OAuth2 donde el token de acceso expira a los 60 minutos
    When un proceso se despierta a las 2 horas intentando conectar
    Then el Hub ejecuta internamente, sin intervención humana, el `refresh_token` contra el Identity Provider
    And obtiene un nuevo Token válido, emite la invocación y mantiene el flujo operativo limpio ininterrumpidamente.

  Scenario: Habilitación de Entradas Inbound (Webhooks) (CA-21)
    Given la necesidad de que el iBPMS sea un ente reactivo a sistemas externos (Ej: CRM actualiza un dato on-demand)
    Then la interfaz de Pantalla 11 expone la capacidad de generar dinámicamente "URLs de Webhooks Inbound"
    And asignando tokens generados nativamente para que sistemas externos llamen al iBPMS e interactúen con instancias de proceso activas (Signal Events / Message Events).

  Scenario: Transformación mediante Inyección de Código Custom (CA-22)
    Given una respuesta legacy en XML con estructuras irregulares inmanejables por el mapeo Drag & Drop simple
    Then el conector habilita opcionalmente un editor integrado "Code Injector" (JS/Python seguro - Sandboxed)
    And permitiendo al Arquitecto escribir scripts deterministas obligados a transformar el payload crudo hacia el JSON esperado por el Engine de forma manual.

  Scenario: Tercerización de Tráfico y Encolamiento (Throttling) (CA-23)
    Given picos de transaccionalidad donde 10,000 procesos invocan a un SharePoint externo simultáneamente
    Then el iBPMS delega la contención (Rate Limiting) a la Cola de Mensajería corporativa (Ej: RabbitMQ / Apache Kafka)
    And no asume internamente la gestión masiva de peticiones limitantes para evitar caídas de servidor.

  Scenario: Aislamiento por Seguridad Condicionada (CA-24 - Diferido a V2)
    Given la existencia de Conectores clasificados como "Altamente Confidenciales" (Saldos ERP)
    Then en el alcance actual de V1 todos los arquitectos autenticados en el BPM Designer tienen visibilidad transversal del catálogo de conectores
    # NOTA: Diferido a V2 la securización del catálogo de conectores por roles estrictos de RBAC.

  Scenario: Encriptación de Payload Militar (CA-25)
    Given el mandato de transferir payloads (Cuerpos HTTP) ultra-sensibles (Ej: Historias Clínicas)
    Then el Hub además de forzar TLS (HTTPS) en tránsito
    And posee soporte de auto-cifrado y descifrado nivel Payload utlizando criptografía asimétrica (PGP) garantizando impenetrabilidad absoluta incluso en reposo en logs intermedios de APIM del proveedor.


  Scenario: Privacidad de Auditoría Cognitiva (CA-29)
    Given que el proceso generó métricas de "Confidence Score" y "Chain of Thought"
    Then estas métricas son de consumo estrictamente interno
    And por ningún motivo se exponen al Ciudadano Externo en la Pantalla 18 (Portal B2B/B2C).

  Scenario: RAG Multimodal Controlado (V1) (CA-30)
    Given la ingesta de documentos anexos para contextualizar a la IA
    Then el Agente RAG soporta en su V1 la lectura de documentos PDF, DOCX e Imágenes (OCR integrado a la API de visión)
    And excluyendo formalmente notas de voz o video (Diferido a V2).

  Scenario: Eficiencia de Contexto Pre-Empaquetado (IA Amarrada) (CA-31)
    Given una tarea generativa que requiere datos externos (Ej: Saldos ERP)
    Then la IA tiene prohibido usar "Function Calling" autónomo para ir a buscar datos por su cuenta (Gasto excesivo de tokens y memoria de razonamiento)
    And la arquitectura dicta que Camunda Engine, mediante Service Tasks previas y baratas, extraiga la data y se la entregue pre-empaquetada en el Prompt a la IA para que esta se limite únicamente a redactar.
```
**Trazabilidad UX:** Pantalla 6 (BPMN Designer Palette), Pantalla 12 (SGDEA), Pantalla 7 (Form Builder UI).
**Trazabilidad UX:** Wireframes Pantalla 11 (Integration Hub).
---


### US-034: Orquestación a través de RabbitMQ
**Como** Administrador de Infraestructura / Backend
**Quiero** delegar el rate-limiting y el encolamiento asíncrono a un Message Broker de grado Enterprise (RabbitMQ)
**Para** garantizar resiliencia extrema frente a picos transaccionales, evitando desbordamientos de memoria (OOM) y caídas de subsistemas.


> [!IMPORTANT]
> **Dependencias Externas Críticas de la US-034:**
> - **US-004 (Webhook):** Los webhooks entrantes que exceden la capacidad del motor se encolan en RabbitMQ (CA-6 de US-004: Resiliencia Periférica con Colas).
> - **US-000 (Resiliencia Integrada):** El health check del clúster RabbitMQ (CA-10) se integra como componente del circuito de salud compuesto de la plataforma.
> - **US-049 (Notificaciones):** Todas las notificaciones por email se despachan como mensajes P2 vía las colas de RabbitMQ.
> - **US-033 (Hub de Integraciones):** Los conectores a sistemas externos (MS Graph, ERP) producen mensajes en las colas de integración.
> - **US-017 (IA Copilot):** Las generaciones de IA (RAG, DMN) son productores Nivel P3 (Batch) en el sistema de prioridades.
> - **US-036 (RBAC):** El acceso al Dashboard DLQ (CA-8) está restringido al rol `ADMIN_IT` administrado en la Pantalla 14.
> - **US-038 (JWT/Seguridad):** El botón de Purga de DLQ requiere autenticación Sudo-Mode definida en la infraestructura de seguridad de US-038.
> - **US-039 (Formulario Genérico):** Los Error Events disparados por los Botones de Pánico se enrutan a través del broker como mensajes P1.


**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Central Message Queue Orchestration
  Scenario: Broker Exclusivo de Alta Demanda (CA-1)
    Given la necesidad de procesar transacciones asíncronas pesadas (IA, Mails, Integraciones)
    Then el iBPMS enruta estos eventos imperativamente a `RabbitMQ` (o Kafka) configurado como clúster
    And prohíbe explícitamente el uso de tablas relacionales (SQL) como mecanismo de encolamiento de alto tráfico para prevenir bloqueos de base de datos (Database Deadlocks).

  Scenario: Dashboard Técnico de DLQ (Monitor Visual) (CA-2)
    Given un fallo masivo en un proveedor externo que atasca 5,000 mensajes en la cola de errores
    Then el iBPMS provee una pantalla de monitoreo transversal para el Rol de Administrador IT
    And permite visualizar el tamaño de la 'Dead Letter Queue' (DLQ)
    And expone botones críticos de acción masiva: `[Purgar Cola]` y `[Reintentar Mensajes Forzosamente]`.

  Scenario: Jerarquización de Supervivencia (Priority Queues) (CA-3)
    Given una saturación temporal de procesamiento en los Workers del sistema
    When ingresan simultáneamente eventos VIP (Ej: Notificaciones de aprobaciones financieras críticas) y eventos de latencia tolerable (Ej: Generación RAG de resúmenes)
    Then RabbitMQ clasifica el tráfico en "Priority Queues" pre-configuradas basándose en metadatos del evento
    And asegura que los procesos de Nivel 1 (Críticos) sean desencolados y procesados antes que las tareas de Nivel 3 (Batch), garantizando el SLA de negocio intacto a pesar del cuello de botella global.


  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us034_functional_analysis.md
  # Tickets: REM-034-01 a REM-034-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Catálogo Oficial de Exchanges, Queues y Routing Keys (CA-4)
    # Origen: REM-034-01 — GAP-1 del us034_functional_analysis.md
    Given la necesidad de prevenir la proliferación desordenada de colas y exchanges en el clúster RabbitMQ
    Then el Arquitecto de Software TIENE OBLIGACIÓN de mantener un catálogo centralizado de la topología de mensajería en el repositorio bajo `docs/architecture/rabbitmq_topology.md` que incluya:
    And 1. Exchange principal: `ibpms.exchange.topic` (tipo Topic) como punto de entrada único para todos los productores.
    And 2. Colas nombradas con convención: `ibpms.{dominio}.{accion}` (Ej: `ibpms.notifications.email`, `ibpms.ai.generation`, `ibpms.integrations.webhook`, `ibpms.bpmn.events`).
    And 3. Routing Keys con convención: `{dominio}.{prioridad}.{accion}` (Ej: `notifications.p1.send`, `ai.p3.generate`, `integrations.p2.sync`).
    And 4. Dead Letter Exchange: `ibpms.exchange.dlx` que enruta a la cola `ibpms.dlq.global`.
    And TIENE PROHIBIDO que cualquier developer cree exchanges o colas ad-hoc sin registrarlas previamente en el catálogo y obtener aprobación del Arquitecto.

  Scenario: [REMEDIACIÓN] Idempotencia Obligatoria en Workers Consumidores (CA-5)
    # Origen: REM-034-02 — GAP-2 del us034_functional_analysis.md
    Given el riesgo de procesamiento duplicado por reintentos manuales desde la DLQ (CA-2) o reintentos automáticos
    Then todo Worker consumidor del iBPMS TIENE OBLIGACIÓN de implementar un mecanismo de idempotencia basado en `message_id`:
    And cada mensaje producido incluirá un header `x-idempotency-key` (UUID v4 generado por el productor).
    And el Worker consultará una tabla `ibpms_processed_messages` (columnas: `idempotency_key`, `processed_at`, `queue_name`, TTL: 72 horas) antes de procesar.
    And si el `idempotency_key` ya existe en la tabla, el Worker hará ACK silencioso del mensaje sin reprocesarlo.
    And la tabla `ibpms_processed_messages` se purgará automáticamente vía un scheduled job cada 24 horas, eliminando registros con más de 72 horas de antigüedad.
    And como alternativa de mayor rendimiento, el Arquitecto podrá reemplazar la tabla SQL por un SET de Redis con TTL de 72 horas (`SISMEMBER ibpms:idempotency {key}`).

  Scenario: [REMEDIACIÓN] Taxonomía Formal de Niveles de Prioridad (CA-6)
    # Origen: REM-034-03 — GAP-3 del us034_functional_analysis.md
    Given la necesidad de jerarquizar el tráfico en Priority Queues (CA-3) con criterios claros
    Then el sistema implementará exactamente 3 niveles de prioridad con la siguiente taxonomía fija:
    And Nivel P1 (Crítico / SLA < 5min): Notificaciones de aprobaciones financieras, Kill-Session (US-036 CA-14), Error Events de Camunda, alertas de seguridad. Prefetch count: 1 (procesamiento atómico garantizado).
    And Nivel P2 (Normal / SLA < 30min): Envío de emails transaccionales (US-049), sincronización EntraID (US-038), webhooks de integración (US-004). Prefetch count: 10.
    And Nivel P3 (Batch / SLA < 4h): Generación RAG de resúmenes (US-017), reportes masivos (US-036 CA-16), limpieza de borradores (US-003 CA-92). Prefetch count: 50.
    And la prioridad se asignará como header del mensaje (`x-priority: P1|P2|P3`) por el productor en el momento de publicar. Si no se especifica, el default es P2.
    And TIENE PROHIBIDO que un productor asigne P1 a eventos que no cumplan con la definición anterior sin aprobación del Arquitecto.

  Scenario: [REMEDIACIÓN] Estrategia de Retry Automático con Backoff Exponencial (CA-7)
    # Origen: REM-034-04 — GAP-4 del us034_functional_analysis.md
    Given la ausencia de reintentos automáticos antes de enviar un mensaje a la DLQ
    Then el clúster RabbitMQ implementará una política de retry automático obligatoria antes de derivar a la Dead Letter Queue:
    And Intento 1: Inmediato (0ms delay).
    And Intento 2: Delay de 5 segundos (via `x-message-ttl` en cola de retry).
    And Intento 3: Delay de 30 segundos.
    And Intento 4 (final): Delay de 2 minutos. Si falla, el mensaje se enruta al DLX (`ibpms.exchange.dlx`) con header `x-delivery-count: 4`.
    And el Worker diferenciará errores transitorios (IOException, TimeoutException → reintentar) de errores permanentes (ValidationException, IllegalArgumentException → DLQ directo sin reintentos).
    And todo mensaje que llegue a la DLQ llevará los headers: `x-original-queue`, `x-first-death-reason`, `x-delivery-count`, `x-last-error-message` para diagnóstico.

  Scenario: [REMEDIACIÓN] Implementación del Dashboard DLQ como Pantalla Custom del iBPMS (CA-8)
    # Origen: REM-034-05 — GAP-5 del us034_functional_analysis.md
    Given la necesidad de un Dashboard visual de DLQ accesible para el Administrador IT (CA-2)
    Then el Dashboard será una pantalla custom del iBPMS (componente Vue) accesible desde la navegación principal, NO un enlace externo al Management UI de RabbitMQ.
    And la pantalla consumirá un endpoint Backend `GET /api/v1/admin/queues/dlq/summary` que retornará: total de mensajes, agrupación por cola de origen (`x-original-queue`), y timestamp del mensaje más antiguo.
    And el botón `[Reintentar Mensajes]` invocará `POST /api/v1/admin/queues/dlq/retry` y requerirá un modal de confirmación con la advertencia: "Se reintentarán N mensajes. Los Workers deben ser idempotentes (CA-5)."
    And el botón `[Purgar Cola]` invocará `DELETE /api/v1/admin/queues/dlq/purge` y requerirá autenticación Sudo-Mode (US-038) con justificación obligatoria de 20+ caracteres.
    And toda acción sobre la DLQ quedará registrada en `ibpms_audit_log` con: `user_id`, `action` (RETRY|PURGE), `message_count`, `timestamp_utc`.
    And el acceso a esta pantalla estará restringido al rol `ADMIN_IT` configurado en la Pantalla 14 (US-036).

  Scenario: [REMEDIACIÓN] Política de TTL y Purgado Automático de la Dead Letter Queue (CA-9)
    # Origen: REM-034-06 — GAP-6 del us034_functional_analysis.md
    Given el riesgo de crecimiento indefinido de la DLQ en producción
    Then la cola `ibpms.dlq.global` implementará un TTL de 30 días naturales (`x-message-ttl: 2592000000ms`) para todos los mensajes.
    And los mensajes que excedan 30 días serán purgados automáticamente por RabbitMQ sin intervención humana.
    And ANTES de purgar, un scheduled job (`DlqArchiveJob`, ejecutado diariamente) copiará los mensajes próximos a expirar (TTL < 48h) a una tabla de archivo `ibpms_dlq_archive` (columnas: `message_id`, `original_queue`, `headers_json`, `body_summary` truncado a 1KB, `archived_at`) para auditoría forense.
    And la tabla `ibpms_dlq_archive` tendrá su propia política de retención: 180 días, purgada por el `LocalStorageGarbageCollector` de infraestructura.

  Scenario: [REMEDIACIÓN] Health Check del Clúster RabbitMQ Integrado al Circuito de Resiliencia (CA-10)
    # Origen: REM-034-07 — GAP-7 del us034_functional_analysis.md
    Given la criticidad del clúster RabbitMQ como infraestructura troncal de la plataforma
    Then el Backend expondrá un endpoint de salud `GET /actuator/health/rabbitmq` que verifique la conectividad al clúster cada 15 segundos.
    And si el health check falla 3 veces consecutivas (45 segundos sin respuesta), el sistema activará un Circuit Breaker (estado OPEN) en todos los productores de mensajes.
    And durante el Circuit Breaker OPEN, los productores almacenarán temporalmente los mensajes en un buffer local en memoria (máximo 1000 mensajes, FIFO) por un máximo de 5 minutos.
    And si RabbitMQ regresa dentro de los 5 minutos (Circuit Breaker HALF-OPEN → CLOSED), el buffer se drenará automáticamente reenviando los mensajes encolados.
    And si RabbitMQ NO regresa en 5 minutos, los mensajes del buffer se persistirán en una tabla de emergencia `ibpms_queue_fallback` y se disparará una alerta crítica al SysAdmin: "RabbitMQ Offline — N mensajes en fallback SQL de emergencia."
    And este endpoint de salud se integrará con la US-000 (Resiliencia Integrada) como parte del health check compuesto `/actuator/health`.


```
**Trazabilidad UX:** Operación Backend e Infraestructura (Dead Letter Queue IT Dashboard).

---


### US-044: Gobernanza de Inteligencia Artificial (AI Limits & MLOps)
**Como** Súper Administrador
**Quiero** una pestaña de configuraciones dedicada al Motor Cognitivo
**Para** gobernar empíricamente el grado de libertad de la IA, auditar sus sesgos, gestionar las listas negras y controlar el ciclo de vida de los modelos sin colapsar la base de datos de producción.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: AI Governance Center, Telemetría MLOps y Micro-Control Cognitivo

  # ==============================================================================
  # A. GOBERNANZA DE INTAKE Y AUTO-PILOT
  # ==============================================================================
  Scenario: Feature Toggle Global del "Auto-Pilot" y No-Retroactividad (CA-01)
    Given el panel de administración central de IA en la Pantalla 15.A
    Then debe existir un Master Switch de `[Permitir Instanciación Autónoma AI]`.
    And este switch opera de forma GLOBAL (apaga o enciende la IA para todos los procesos en V1).
    And si está apagado, TODAS las Action Cards caerán forzosamente al Embudo Humano (Pantalla 16).
    And si el Administrador vuelve a ENCENDER el switch, la IA TIENE PROHIBIDO autoprocesar las tarjetas que ya estaban en cuarentena de manera retroactiva, exigiendo revisión humana para las antiguas y aplicando el Auto-Pilot solo a los correos nuevos.

  Scenario: Parametrización de Certeza Dinámica (Tolerance Score) (CA-02)
    Given el motor de inferencia que calcula la confianza matemática de sus predicciones
    Then el Súper Administrador DEBE disponer de un Slider o Campo Numérico (Ej: 0-100%) en la UI.
    And prohibiendo el "hardcoding" en el backend, este umbral dictará la Certeza Mínima Requerida en tiempo real.
    And cualquier inferencia por debajo del umbral parametrizado será enviada obligatoriamente a revisión humana (Fallback).

  # ==============================================================================
  # B. AUDITORÍA ANTI-OVERFITTING (TRANSPARENCIA Y AMNESIA)
  # ==============================================================================
  Scenario: Traducción Semántica de Tensores (Explainable AI - XAI) (CA-03)
    Given el proceso nocturno del Agente Data Scientist
    Then el Administrador posee una pantalla "AI Audit Log".
    And el sistema utilizará un micro-LLM auxiliar inverso (XAI) para traducir los deltas vectoriales matemáticos hacia "Jerga Legible por Humanos" (Ej: `Aprendizaje 1: La palabra 'Reclamo' levanta flag de prioridad Alta`).
    
  Scenario: Efecto Cascada de la Amnesia (Negative Prompting Cache) (CA-04)
    Given la pantalla de "AI Audit Log"
    When el Administrador selecciona un Patrón obsoleto o erróneo y oprime `[Eliminar Patrón]`
    Then el sistema NO ejecutará un costoso reentrenamiento de la BD Vectorial en caliente.
    And inyectará instantáneamente el patrón rechazado como un "Negative Prompt" (System Instruction) en la caché de memoria RAM del LLM.
    And forzará el desaprendizaje cognitivo en tiempo real en milisegundos, delegando el borrado físico de los vectores para el proceso Batch de la madrugada.

  # ==============================================================================
  # C. ROLLBACK Y GESTIÓN DE BASES DE DATOS VECTORIALES
  # ==============================================================================
  Scenario: Integridad Transaccional en Blue-Green Swapping y Límite N-1 (CA-05)
    Given un escenario de degradación aguda de la IA (Ej: Alucinaciones masivas)
    When el Administrador presione el botón de emergencia `[Revertir Modelo Anterior]`
    Then el Backend ejecutará un "Blue-Green Data Swapping" SQL en milisegundos (`is_active_model = FALSE/TRUE`).
    And la plataforma V1 solo soportará memoria de reversión **N-1** (El modelo de hoy y el de ayer) para proteger los costos Cloud.
    And las transacciones de Camunda en vuelo que fallen en ese microsegundo sufrirán Degradación Elegante, siendo reintentadas por RabbitMQ a los 5 segundos contra el modelo ya restaurado.

  Scenario: Garbage Collection Vectorial (Ahorro Cloud) (CA-06)
    Given la generación constante de snapshots vectoriales tras los reentrenamientos y rollbacks
    Then el sistema ejecutará un Job de Mantenimiento programado semanal (Ej: Domingos 03:00 AM).
    And ejecutará un `HARD DELETE` físico sobre cualquier modelo marcado como inactivo (`is_active_model = FALSE`) que supere los 7 días de antigüedad, evitando facturas desmesuradas en `pgvector`.

  # ==============================================================================
  # D. OPERATIVIDAD MLOPS DEL DATA SCIENTIST Y RESILIENCIA
  # ==============================================================================
  Scenario: Prevención de Solapamiento de Cron Jobs (ShedLock Mutex) (CA-07)
    Given el reentrenamiento masivo programado (Ej: Diario a las 02:00 AM)
    When el servidor intenta lanzar la instancia de hoy, pero la instancia de ayer sigue en estado `RUNNING` (Ej: el procesamiento tomó 26 horas)
    Then el Backend DEBE utilizar un Database Lock (Ej: librería `ShedLock` o Mutex nativo).
    And al detectar el candado, ABORTARÁ silenciosamente la ejecución del Job nuevo (Skip).
    And prevendrá el colapso del servidor por *Out of Memory* (OOM), emitiendo una alerta técnica al SysAdmin.

  Scenario: Manejo de Errores Silenciosos y Aislamiento Tenant (CA-08)
    Given una falla persistente en el Job Nocturno (Ej: Timeouts en la BD)
    When el Job de reentrenamiento falla durante 3 días consecutivos
    Then el sistema TIENE PROHIBIDO detener la operación diurna o apagar los Embudos de Inteligencia Artificial.
    And entrará en "Modo Supervivencia", operando con el último modelo estable conocido y encendiendo una Alerta Roja inborrable en el Dashboard de SysAdmin.
    And en despliegues Multitenant, el reentrenamiento usará Colas Dedicadas por Tenant en RabbitMQ, asegurando que el volumen de un Cliente no asfixie el reentrenamiento de los demás.

  # ==============================================================================
  # E. SEGURIDAD PERIMETRAL
  # ==============================================================================
  Scenario: Sensibilidad y Normalización Absoluta de Lista Negra (El Guardia Tonto) (CA-09)
    Given el componente de inyección de dominios prohibidos (Blacklist)
    When el Administrador inyecta un dominio "sucio" en la UI (Ej: `  @GMAIL.COM  `)
    Then el Interceptor del Backend TIENE ESTRICTAMENTE PROHIBIDO guardar el input crudo.
    And aplicará obligatoriamente una normalización de limpieza (`.toLowerCase().trim()`) ANTES del Commit SQL en la tabla `ibpms_public_domains_blacklist`.
    And el motor aplicará esta misma normalización a los correos entrantes antes de comparar, garantizando un blindaje matemático total contra bypasseos de ciberseguridad por errores de digitación.

```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Configuración Global / Súper Administrador).

---

### US-045: Restricciones de Dominio Ágil y Documental (System Limits)
**Como** PMO / Arquitecto del Sistema
**Quiero** configurar techos duros (Hard Limits) numéricos a las funcionalidades operativas
**Para** evitar la degradación de Base de Datos y prevenir malas prácticas gerenciales (como Tableros Ágiles infinitos).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Governing Agile Entropy and Storage Economics
  Scenario: Barrera de Densidad Kanban (US-008)
    Given la configuración de Restricciones UI
    Then el Administrador debe disponer de un control numérico `Kanban_Max_Columns`
    And no permitirá exceder el número pactado (Ej: 7) para evitar tableros ágiles inmanejables a nivel de scroll horizontal y UX. Cualquier intento de un Scrum Master para añadir una columna #8 fallará.
    
  Scenario: Ventana de Gracia Transaccional (Deshacer Intakes - US-024)
    Given la necesidad de proteger la creación humana accidental
    Then el panel expone un selector numérico en Segundos `[Creation_Grace_Period_Secs]`
    And dictamina universalmente cuánto tiempo dura vivo el Toast de "Deshacer" en todas las creaciones atómicas antes de que el Backend haga un COMMIT real a la base de datos transaccional en Camunda.

  Scenario: Gobernanza Económica de Ligas SGDEA Efímeras (US-010)
    Given la generación de PDFs Legales de alto costo que el cliente final puede consultar mediante una S3 URL Pre-Firmada
    When el usuario la comparta con los clientes para validación temporal ("Review Mode")
    Then el Administrador controla el `[Pre_Signed_URL_TTL_Hours]` dictando globalmente en el sistema la caducidad (TTL) de todos los links transaccionales generados (Volar el acceso al archivo tras 12 o 24 horas por seguridad).

  Scenario: Persistencia Híbrida de Formularios en Ágil (JSONB Pocket)
    Given que la arquitectura Ágil/Kanban rechaza el uso de Camunda (CMMN/BPMN) para favorecer la velocidad pura de Base de Datos Relacional (JPA)
    When un Arquitecto asocie un Formulario Zod (iForm Maestro o Genérico) a una Tarjeta Kanban y el operario oprime [Guardar Progreso]
    Then la tabla relacional `ibpms_kanban_tasks` DEBE contar con una columna especializada de tipo `JSONB` (o su equivalente estructurado).
    And el Backend serializará y guardará el Payload completo validado por Zod directamente dentro de esta columna de la entidad.
    And garantizando que la tarjeta Ágil soporte la captura de datos estructurados sin ensuciar la base de datos con tablas hijas.
    And OBLIGATORIAMENTE este ID KanBan convivirá con el ecosistema de Zeebe en una capa de Proyección CQRS Central (Ej: ibpms_global_worklist_view), inyectando una "Vista 360" en ES/RDBMS que aborte el divorcio entre tareas CMMN y Tareas Ágiles.
```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Restricciones Arquitectónicas / PMO).

---

### US-046: Gobernanza de Rendimiento e Integraciones (Data & Perf)
**Como** Analista de Infraestructura (SysAdmin)
**Quiero** manipular el comportamiento de lectura/escritura y polling del iBPMS
**Para** evitar saturar la red y proteger a las bases de datos de colapsos DWH.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: API Polling & Telemetry Thresholds
  Scenario: Master Switch del CRM ONS (US-013/019 Fallback Toggle)
    Given una caída nacional o intermitencia catastrófica en el CRM Externo de la empresa
    When el Administrador entra al Panel de Integraciones (Connections Dashboard)
    Then debe tener acceso directo a apagar el master switch `[Integración Continua CRM: OFF]`
    And al hacer esto, ordena inmediatamente al motor IBPM a refugiarse en la Metadata Interna local (Service Delivery Local Catalog) mitigando el error 500 y permitiendo facturar a pesar del CRM caído.

  Scenario: Cadencia de Polling de Dashboards (Anti-DDoS Interno - US-009)
    Given cientos de líderes de negocio con el "BAM Dashboard" abierto simultáneamente
    Then para evitar que las pantallas colapsen las réplicas de la Base de Datos con peticiones asíncronas
    And el Administrador manipula el `[BAM_Refresh_Rate_Ticks]` dictando cada cuántos minutos (Globalmente) el Frontend pedirá repintar gráficas a la BD, anulando comandos de refresco interactivos.

  Scenario: Regla de Retención y Purgado de Logs MLOps (Cold Storage NFR)
    Given el crecimiento exponencial de la tabla `ibpms_mlops_feedback_log` (Los Deltas capturados en el día por la US-015) en PostgreSQL 15+
    And que tenemos PROHIBIDO delegar la purga a `pg_cron` para proteger la salud del almacenamiento SSD transaccional
    Then un Scheduled Task del Backend (Spring Boot) buscará los JSONs pasados (>40 días)
    And los consolidará y trasladará por red segura (HTTPS SDK) al Azure Blob Storage
    And SOLO tras verificar el Hash/Éxito de la transferencia a Azure, el Backend lanzará el DELETE físico al motor SQL para borrarlos permanentemente.

  Scenario: Telemetría Global de Infraestructura (Mailbox Health)
    Given la caída de un Token OAuth de un buzón transaccional (US-016)
    When el motor detecte la falla de lectura
    Then además de la alerta local, se emitirá una Notificación Global en la Pantalla 15.A y se despachará un correo/webhook crítico al SysAdmin informando: "Integridad de Entrada Comprometida: Buzón X Desconectado".

  Scenario: Telemetría de Desfase Comercial (Sync Health)
    Given la falla definitiva de la sincronización nocturna o manual del Catálogo CRM (Agotamiento de reintentos RabbitMQ de la US-020)
    Then al arrancar la operación humana en la mañana (Ej: 8:00 AM)
    And el iBPMS forzará la exhibición de un "Banner Rojo Permanente" en la cabecera de la Pantalla 15.A indicándole al SysAdmin: *"CRÍTICO: La sincronización de catálogo falló. El iBPMS opera con una versión desactualizada de más de 24 horas"*.
```
**Trazabilidad UX:** Nueva pestaña en Pantalla 15.A (Performance y Conexiones / SysAdmin).

---


### US-049: Motor Central de Notificaciones y Plantillas (Outbound Engine)
**Como** Administrador del Sistema / PMO
**Quiero** disponer de un motor centralizado que gestione todas las salidas de correos electrónicos y notificaciones
**Para** administrar plantillas dinámicas visualmente, evitar textos quemados en el código fuente y garantizar que el envío de correos no bloquee transaccionalmente el motor de Camunda.

**Criterios de Aceptación (Gherkin):**
Feature: Central Outbound Notification Engine
```gherkin
  Scenario: Prohibición de Textos Quemados (Hardcoding) en Backend
    Given la necesidad estructural del sistema de enviar correos electrónicos (Ej: Confirmaciones US-022, Alertas US-040)
    Then la arquitectura TIENE ESTRICTAMENTE PROHIBIDO que los desarrolladores redacten el HTML o el asunto (Subject) de los correos dentro del código fuente (Java/Node).
    And el sistema debe proveer un CRUD de "Plantillas de Notificación" (Templates) en la Interfaz Administrativa (Pantalla 15), utilizando un motor de renderizado estándar (Ej: Thymeleaf, FreeMarker o Handlebars).
    And las plantillas deben soportar inyección de variables dinámicas (Ej: `Hola {{cliente.nombre}}, tu caso {{caso.id}} ha sido radicado`).

  Scenario: Despacho Asíncrono Estricto (Patrón Outbox)
    Given que el motor Camunda llega a una `SendTask` o `ServiceTask` configurada para notificar al cliente
    When el hilo de ejecución intenta despachar el correo a través del servidor SMTP o MS Graph API
    Then el motor de procesos NO ESPERARÁ la respuesta del servidor de correos (Evitando el bloqueo del Main Thread y Timeouts transaccionales).
    And empaquetará el payload del correo y lo arrojará a una Cola de Mensajería dedicada (Ej: `outbound-email-queue` en RabbitMQ definida en US-034).
    And un Worker independiente desencolará y ejecutará el envío real hacia internet.

  Scenario: Resiliencia y Tolerancia a Caídas del Servidor de Correo
    Given el Worker independiente procesando la cola de correos salientes
    When el servidor SMTP corporativo del cliente (Ej: Office 365 / Exchange) sufre una caída temporal (HTTP 503 / Timeout)
    Then el Worker no descartará el correo ni fallará la transacción de negocio principal.
    And aplicará una política de reintentos con "Exponential Backoff" (Ej: reintentar en 1 min, luego en 5 min, luego en 15 min).
    And si agota los reintentos máximos, trasladará el correo a una Dead-Letter Queue (DLQ) y emitirá una alerta visual en el Dashboard de TI para intervención manual.

  Scenario: Auditoría Forense de Salida (Outbound Audit Trail)
    Given un correo electrónico de respuesta de fondo o confirmación enviado al cliente
    When el servidor SMTP confirma el despacho (Status 200 OK)
    Then el Motor de Notificaciones debe generar una copia inmutable del HTML exacto y los metadatos de envío.
    And debe registrar esta copia en la tabla `ibpms_outbox_log` vinculada al `Process_Instance_ID`.
    And debe proyectar este registro visualmente en la pestaña "Contexto y Correos" del Workdesk proveyendo al analista de una prueba legal irrefutable de qué se le dijo al ciudadano y cuándo.

  Scenario: Agrupación Anti-Spam (Digest / Throttling)
    Given un error de diseño de un Arquitecto (Ej: Ciclo infinito en BPMN) o una caída masiva de SLAs donde 150 casos vencen simultáneamente
    When el motor dispara las alertas hacia el correo del "Jefe de Área"
    Then el Notification Engine aplicará una regla paramétrica de "Agrupación Temporal" (Throttling Window, Ej: 15 minutos) por destinatario.
    And en lugar de bombardear al Jefe con 150 correos individuales colapsando su bandeja, el motor consolidará los eventos en un único correo tipo "Digest": `[Alerta Masiva: 150 SLAs han sido vulnerados en los últimos 15 min. Vaya al Dashboard]`, protegiendo la reputación del dominio (Anti-Spam).

  Scenario: Extracción e Inyección de Anexos Físicos con Streaming Activo (Outbound Zero-RAM)
    Given el Motor de Notificaciones procesando un correo en la cola de salida (RabbitMQ)
    When la tarea transaccional de Camunda incluya un Array de identificadores documentales (Ej: `attachments: ["UUID-A"]`)
    Then el Worker de Notificaciones hará una pausa antes de conectarse al servidor SMTP.
    And se autenticará contra la Bóveda SGDEA (SharePoint - US-035) utilizando esos UUIDs.
    And TIENE PROHIBIDO descargar binarios corporativos hacia la memoria RAM (Heap) del Servidor para evitar Out Of Memory (OOM).
    And realizará Piping HTTP bidireccional (Streams directos) hacia MS Graph API, o en su defecto recaerá en staging OS de memoria Flash (`/tmp`).
    And conectará en caliente el pipeline al formato adjunto (`Attachments`) en la trama del correo electrónico saliente, manteniendo el NodeWorker inmutable.

Scenario: Infraestructura de Notificaciones In-App (WebSocket Campana)
    Given la necesidad de alertar a un usuario internamente (Ej: SLA a punto de vencer, Tarjeta IA asignada)
    When el Motor de Notificaciones procesa un evento configurado con el canal `IN_APP`
    Then el sistema persistirá el registro en la tabla relacional `ibpms_inapp_notifications` con estado `is_read = false`.
    And despachará instantáneamente un push payload vía WebSocket al Frontend del usuario objetivo.
    And el Frontend incrementará el contador rojo (Badge) de la Campana en el Master Header de forma reactiva, sin requerir refresco de pantalla (F5).
    And la UI proveerá un endpoint ligero `PATCH /read` que se disparará al abrir el panel, atenuando el contador.

```

---


