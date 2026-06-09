# [TÍTULO DE LA ÉPICA]
> **Fuente:** Modularizado desde `v1_user_stories.md` | **Fecha:** 2026-04-10
> **Coverage:** `.agentic-sync/coverage_matrix.md` (centralizada)

---

### US-036: Matriz de Control de Acceso Basado en Roles (RBAC)
**Como** Oficial de Seguridad de la Información (CISO) / Super Admin
**Quiero** gestionar centralizadamente todos los permisos, perfiles y delegaciones en la Pantalla 14
**Para** garantizar cumplimiento ISO 27001, prevenir accesos no autorizados a datos sensibles y auditar la segregación de funciones (SoD).

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Identity Governance & RBAC Architecture
  Scenario: Hibridación de Roles EntraID vs Locales (CA-1)
    Given una organización iniciando el iBPMS
    Then la Pantalla 14 soporta doble motor de mapeo de identidades
    And permite importar automáticamente Roles/Grupos desde Microsoft EntraID (SSO) 
    And provee un "Fallback" interno para crear y asignar Roles 100% locales en la BD del iBPMS si el cliente tiene baja madurez corporativa.

  Scenario: El Guardián Absoluto (Root Super Admin) (CA-2)
    Given el despliegue inicial (Día Cero) del iBPMS
    Then el sistema inyecta por defecto un único usuario `[Super_Administrador]` inborrable a nivel de base de datos
    And este rol es el único con potestad absoluta para ingresar a la Pantalla 14 y delegar poder (crear otros administradores).

  Scenario: Clonación de Perfiles por Plantilla (CA-3)
    Given la necesidad de dar el mismo set de 15 permisos a 50 asesores nuevos
    Then la Pantalla 14 permite la creación de un `[Rol Plantilla]` lógico que atrapa esos permisos
    And permite asignar ese `[Rol Plantilla]` en bloque a los 50 usuarios con un solo click (Mass Assignment).

  Scenario: Segregación Iniciador vs Ejecutor (CA-4)
    Given la matriz de permisos de la Pantalla 14
    Then expone casillas de verificación (Checkboxes) granulares y explícitas para cada Proceso BPMN publicado
    And diferencia a nivel de base de datos el permiso booleano `can_initiate_process` (Cliente) vs `can_execute_tasks` (Cocinero).

  Scenario: Privacidad Visual de Colas (Data Segregation Local) (CA-5)
    Given dos analistas (Juan y María) pertenecientes al mismo Rol "Analista_Créditos"
    Then al ingresar a su Workdesk (Pantalla 5), la arquitectura forza un filtro de base de datos a nivel de registro (Row-Level Security)
    And garantiza que María SOLO visualice los folios/casos asignados a ella, ocultando tajantemente el trabajo de sus pares a menos que sea una "Cola Compartida Pública".

  Scenario: Herencia de Roles Piramidal (CA-6)
    Given una estructura jerárquica corporativa
    Then la Pantalla 14 permite que el rol `[Gerente_Riesgo]` sea configurado para "Heredar" atómicamente el 100% de los permisos subyacentes del rol `[Analista_Riesgo]` minimizando la redundancia de clics en la matriz.

  Scenario: Inmutabilidad por Desactivación Suave (Soft-Delete) (CA-7)
    Given que el empleado Juan renuncia a la empresa y es desconectado del SSO
    Then el Súper Admin en Pantalla 14 NO puede borrar físicamente (DELETE SQL) la identidad de Juan 
    And el sistema le asigna un sello de `[Usuario Inactivo]`, congelando su estado pero preservando eternamente su nombre en los registros de auditoría de los casos que resolvió en el pasado.

  Scenario: Aprovisionamiento de Transeúntes (Ciudadano Interno) (CA-8)
    Given un empleado recién contratado que se loguea en el iBPMS vía SSO por primera vez
    Then el motor RBAC le auto-provisiona un perfil inofensivo por defecto llamado `[Ciudadano_Interno]`
    And este perfil arranca con capacidades nulas hasta que el Súper Admin configure explícitamente en Pantalla 14 qué Procesos Generales (Ej: Vacaciones) tienen permitida "Autogestión por defecto".

  Scenario: Módulo de Delegación Autónoma Temporal (CA-9)
    Given un Gerente que se marcha a vacaciones por 15 días
    Then la Pantalla 14 le provee un panel de Autogestión (Delegación) para cedar sus poderes a un suplente (Ej: Su asistente)
    And esta cesión de Rol requiere obligatoriamente estampar el `[Rango_de_Fechas]` (Fecha Inicio / Fin) para revocarse automáticamente.
    And toda la transacción de traspaso de poder queda flaggeada transaccionalmente para la bitácora del CISO.

  Scenario: Creación de Robots de Integración (API Keys / Service Accounts) (CA-10)
    Given la necesidad de que el ERP corporativo lance casos en el iBPMS 24/7 sin interacción humana
    Then la Pantalla 14 posee un módulo paralelo de "Cuentas de Servicio M2M"
    And permite generar Tokens Criptográficos (API Keys) atándolos a Roles específicos, prohibiendo que los sistemas externos operen con credenciales de humanos vulnerables.
    
  Scenario: Respeto ciego al Autenticador Perimetral (EntraID MFA) (CA-11)
    Given una tarea crítica que un gerente va a ejecutar en su bandeja
    Then la arquitectura de seguridad V1 asume 100% de confianza en el Token emitido por Microsoft EntraID
    And el iBPMS NO reconstruye un componente duplicado de Doble Factor (MFA) propio en pantalla, delegando esta validación criptográfica al Identity Provider original.

  Scenario: Exclusión de Ocultamiento de Campos (Scope Limit) (CA-12)
    Given un usuario intentando ocultar la columna "Salario" de un formulario en base al rol
    Then la directriz aclara que la Pantalla 14 administra accesos a la "Instancia Completa" (El Formulario entero)
    And delega la responsabilidad técnica de ocultar campos individuales a la algoritmia del Pro-Code Builder (Pantalla 7) durante el diseño del Vue Component.

  Scenario: Desacoplamiento de Roles Estáticos vs Dinámicos (BPMN Lanes) (CA-13)
    Given la asignación de trabajo en el motor Camunda
    Then el módulo de Permisos reconoce y respeta dos vías de asignación: 
    Los Roles Estáticos (Asignados en la Pantalla 14 manualmente de por vida al usuario) y los Roles Dinámicos/Variables (Inyectados en tiempo real por el BPMN a través de *Expression Lanes*).

  Scenario: El Botón Táctico de Exorcismo (Kill-Session) (CA-14)
    Given un evento de despido disciplinario hostil a mediodía
    Then la Pantalla 14 expone un botón rojo `[Revocar Todo y Matar Sesión]` en la ficha del empleado
    And la arquitectura exige que el Backend destruya activamente los JWT almacenados en caché/Redis de ese analista forzando su deslogueo TCP instantáneo, sin tener que esperar que su Token de 1 hora expire.

  Scenario: Bypass Anónimo de Procesos (URLs Públicas) (CA-15)
    Given la necesidad ciudadana de radicar PQRS sin crear cuentas
    Then la Pantalla 14 (y la Configuración del Proceso) cuenta con un Switch Especial `[Permitir Trámite Público]`
    And al activarse, el generador de Formularios expone un endpoint/URL sin requerimiento de Bearer Token en las rutas de Vue Router, posibilitando el inicio ciego de instancias BPMN por navegadores anónimos en internet.

  Scenario: Informes Densos de Fiscalización (Auditoría CISO) (CA-16)
    Given la temporada de Auditoría ISO 27001
    Then la Pantalla 14 permite generar el reporte matrizal de *Identity Governance*
    And compila una sábana CSV / Excel descargable cruzando `[Todos los Usuarios/Robots]` contra `[Todos los Roles Activos]` y `[Todos los Procesos Iniciables/Ejecutables]`.

  Scenario: Traza Indeleble de Otorgamiento (CA-17)
    Given un practicante que misteriosamente amanece con el rol `[Gerente_Financiero]`
    Then la Pestaña de Auditoría de Seguridad de Pantalla 14 muestra el JSON delta exacto
    And expone qué Administrador Ejecutante (ID Humano), en qué milisegundo UTC (Timestamp), inyectó la sobre-escritura de permisos.

  Scenario: Omisión Estricta de Segregación de Funciones Automática (SoD) (CA-18)
    Given la ley del "Quien hace no aprueba"
    Then para el MVP V1, el motor iBPMS NO frena estructuralmente a un humano si el BPMN le enruta "Crear Cheque" y "Aprobar Cheque" al mismo tiempo
    And asume este riesgo operativo difiriendo los motores complejos de "Conflict of Interest Avoidance" a V2, confiando en que el diseño del proceso en Pantalla 6 asigne humanos distintos para el flujo iterativo.

  # ==============================================================================
  # B. REMEDIACIONES POST-AUDITORÍA (Sprint Remediation Brief 2026-04-05)
  # Origen: docs/requirements/us036_functional_analysis.md
  # Tickets: REM-036-01 a REM-036-07
  # Propósito: Cerrar GAPs de implementación detectados por el workflow
  #            /analisisEntendimientoUs.md tras finalizar las 17 iteraciones
  #            de la Auditoría Integral del Backlog.
  # ==============================================================================

  Scenario: [REMEDIACIÓN] Modelo de Datos Relacional para la Matriz RBAC (CA-19)
    # Origen: REM-036-01 — GAP-1 del us036_functional_analysis.md
    Given la necesidad de persistir roles, permisos, asignaciones y herencia piramidal definidos en los CA-1 a CA-18
    Then el Backend TIENE OBLIGACIÓN de implementar el siguiente esquema relacional mínimo en PostgreSQL:
    And Tabla `ibpms_roles` con columnas: `id`, `name`, `description`, `parent_role_id` (FK auto-referencia para herencia CA-6), `is_template` (boolean para Rol Plantilla CA-3), `source` (ENUM: ENTRA_ID | LOCAL), `created_at`, `updated_at`.
    And Tabla `ibpms_permissions` con columnas: `id`, `resource` (Ej: PROCESS, FORM, ADMIN_PANEL), `action` (ENUM: INITIATE, EXECUTE, READ, WRITE, DELETE), `process_definition_id` (FK nullable para permisos por proceso CA-4).
    And Tabla pivote `ibpms_role_permissions` para la relación N:M entre roles y permisos.
    And Tabla pivote `ibpms_user_roles` con columnas: `user_id`, `role_id`, `assigned_by`, `assigned_at`, soportando Mass Assignment (CA-3) mediante INSERT batch.
    And la herencia piramidal (CA-6) se resolverá mediante una query recursiva CTE (`WITH RECURSIVE`) que recorra `parent_role_id` para computar los permisos efectivos de un rol en tiempo de consulta.
    And el esquema se gestionará mediante scripts Liquibase versionados en `db/changelog/`.

  Scenario: [REMEDIACIÓN] Estrategia de Row-Level Security para Privacidad de Colas (CA-20)
    # Origen: REM-036-02 — GAP-2 del us036_functional_analysis.md
    Given la exigencia de que cada operario visualice SOLO sus folios asignados en el Workdesk (CA-5)
    Then la implementación V1 utilizará un interceptor centralizado a nivel de aplicación (Spring AOP `@Aspect` o un `Specification` base de JPA) que inyecte automáticamente el filtro `WHERE assignee_id = :currentUserId` en TODAS las queries del Workdesk.
    And TIENE PROHIBIDO implementar el filtro como un WHERE manual en cada Repository method, ya que un endpoint olvidado filtraría datos ajenos.
    And si en el futuro se migra a RLS nativo de PostgreSQL (`CREATE POLICY`), el interceptor de aplicación se desactivará sin afectar la lógica de negocio.
    And para las Colas Compartidas Públicas, el interceptor reconocerá un flag `is_shared_queue = true` en la definición del proceso y omitirá el filtro de usuario, permitiendo visibilidad colectiva.

  Scenario: [REMEDIACIÓN] Infraestructura de Blacklist JWT para Kill-Session (CA-21)
    # Origen: REM-036-03 — GAP-3 del us036_functional_analysis.md
    Given la funcionalidad de Kill-Session (CA-14) que exige destruir sesiones activas instantáneamente
    Then la implementación del botón Kill-Session en Pantalla 14 invocará un endpoint `POST /api/v1/admin/users/{userId}/revoke-session`.
    And este endpoint insertará el `jti` (JWT ID) del token activo del usuario en una blacklist de Redis con TTL igual al tiempo restante de vida del token (max 15 minutos según política de US-038 CA-01).
    And el Spring Security Filter consultará esta blacklist en cada request entrante en menos de 5ms.
    And esta implementación TIENE DEPENDENCIA DIRECTA con la US-038 CA-01 (Fail-Open Policy), la cual define el comportamiento cuando Redis no está disponible.
    And el equipo que desarrolle la US-036 TIENE OBLIGACIÓN de coordinarse con el equipo de la US-038 para compartir el mismo servicio de blacklist Redis, prohibiendo crear implementaciones paralelas.

  Scenario: [REMEDIACIÓN] Política de Seguridad para API Keys de Service Accounts (CA-22)
    # Origen: REM-036-04 — GAP-4 del us036_functional_analysis.md
    Given la funcionalidad de creación de Service Accounts M2M (CA-10) que genera API Keys sin política de ciclo de vida
    Then toda API Key generada en Pantalla 14 TIENE OBLIGACIÓN de incluir una fecha de expiración configurable (por defecto: 365 días, máximo: 730 días).
    And la API Key se almacenará hasheada con SHA-256 en la tabla `ibpms_service_accounts`; el valor en texto plano solo se mostrará UNA VEZ al momento de la creación (como GitHub Personal Access Tokens).
    And la Pantalla 14 mostrará un indicador visual de API Keys próximas a expirar (menos de 30 días) con alerta amarilla, y expiradas con alerta roja.
    And el Super Admin podrá regenerar (rotar) una API Key existente, deprecando la anterior inmediatamente e invalidando todas las sesiones activas del Service Account.
    And todo uso de API Key se registrará en la tabla `ibpms_audit_log` con: `service_account_id`, `endpoint_invocado`, `timestamp_utc`, `ip_origen`.

  Scenario: [REMEDIACIÓN] Comportamiento de Delegación sobre Tareas In-Flight (CA-23)
    # Origen: REM-036-05 — GAP-5 del us036_functional_analysis.md
    Given un Gerente que activa una delegación temporal a un suplente (CA-9)
    When la delegación entra en vigencia según el rango de fechas configurado
    Then el suplente heredará TANTO el rol delegado COMO las tareas ya asignadas al delegante en la bandeja del Workdesk (tareas in-flight).
    And las tareas nuevas que lleguen durante el periodo de delegación también se enrutarán al suplente.
    And al expirar la delegación, las tareas NO completadas por el suplente regresarán automáticamente a la bandeja del delegante original con un sello visual: "[Retornada post-delegación]".
    And toda la operación de transferencia y retorno de tareas quedará registrada en `ibpms_audit_log` para trazabilidad CISO.

  Scenario: [REMEDIACIÓN] Alcance Explícito del Reporte ISO 27001 en V1 (CA-24)
    # Origen: REM-036-06 — GAP-6 del us036_functional_analysis.md
    Given la funcionalidad de generación de reportes de Identity Governance (CA-16)
    Then para V1 el reporte se generará exclusivamente bajo demanda (on-demand) mediante un botón en Pantalla 14, sin generación programada automática (cron).
    And el reporte incluirá la fecha y hora UTC de generación, el usuario que lo solicitó, y un hash SHA-256 del contenido para certificar integridad.
    And cada reporte generado se persistirá como registro histórico en la tabla `ibpms_audit_reports` para comparación entre periodos (Ej: "Estado de permisos en Enero vs Febrero").
    And la generación programada (cron + envío por email al CISO) queda explícitamente DIFERIDA a V2.

  Scenario: [REMEDIACIÓN] Directriz de Coordinación US-036 vs US-038 (CA-25)
    # Origen: REM-036-07 — GAP-7 del us036_functional_analysis.md
    Given el solapamiento funcional entre US-036 (UI y reglas de negocio RBAC) y US-038 (infraestructura JWT, Redis, Sync EntraID)
    Then la directriz oficial de separación de responsabilidades es:
    And US-036 es responsable de: la Pantalla 14 (UI completa), la lógica de negocio de roles/permisos, los CRUDs de usuario/rol/delegación, y la generación de reportes.
    And US-038 es responsable de: la infraestructura de autenticación (JWT lifecycle, Redis blacklist, Fail-Open Policy), la sincronización periódica con EntraID, y el Sudo-Mode para operaciones destructivas.
    And el servicio de blacklist Redis es un componente COMPARTIDO: ambas historias lo consumen pero su implementación canónica reside en US-038.
    And TIENE PROHIBIDO que la US-036 implemente su propia lógica de invalidación de tokens separada de la US-038.
    And ambas historias DEBEN ser asignadas al mismo Arquitecto de Software para garantizar coherencia en el diseño de seguridad.

 # ==============================================================================
  # [REFINAMIENTO] GOBERNANZA DINÁMICA DE TOPOLOGÍA VISUAL (MENÚ) (2026-04-22)
  # ==============================================================================
  Scenario: [REFINAMIENTO] Experiencia de Caída Segura (UX Fallback) (CA-26)
    Given un usuario que inicia sesión pero su rol asignado no posee ningún menú activo (o le han sido revocados todos)
    Then el Frontend ruteará al usuario hacia una "Página de Bienvenida" en blanco o Dashboard base neutral
    And nunca lo dejará en un estado bloqueado con errores o con menús fantasma.

  Scenario: [REFINAMIENTO] Inmutabilidad de Roles Nativos del Sistema (CA-27)
    Given la necesidad de proteger la plataforma de bloqueos accidentales
    When el CISO intenta editar los permisos de menú de un rol fundacional (ej. `SUPER_ADMIN` o `SYSTEM_ADMIN`)
    Then la interfaz de selección de módulos (checkboxes) estará bloqueada (Read-Only/Disabled)
    And se garantizará que los roles nativos siempre retengan acceso total al menú de Administración.

  Scenario: [REFINAMIENTO] Granularidad Macro de la Topología Visual (CA-28)
    Given la configuración de los accesos de menú para un nuevo rol en la V1
    Then la interfaz permitirá habilitar/deshabilitar estrictamente los 7 Módulos Macro principales (Workdesk, Service Delivery, BAM, Modeler, Integración, Proyectos, Administración)
    And no se exigirá selección granular de submenús internos, gobernando el acceso a nivel de macro-módulo por ahora.

  Scenario: [REFINAMIENTO] Diseño Limpio del Modal de Roles (Tablas/Tabs) (CA-29)
    Given la Pantalla 14 donde el CISO forja o edita un nuevo rol
    Then la UI implementará un diseño dividido en Pestañas (Tabs) para no saturar verticalmente el modal
    And existirá un "Tab 1: Información Básica" y un "Tab 2: Topología de Menús" aplicando buenas prácticas de UX/UI.

  Scenario: [REFINAMIENTO] Superposición Inclusiva Multirrol (Unión Matemática) (CA-30)
    Given un usuario al que se le han asignado múltiples roles (Ej: Rol A y Rol B)
    When el Backend calcula los menús que el usuario puede ver
    Then el sistema realizará la unión matemática inclusiva de los permisos de ambos roles
    And entregará un listado unificado sin colisiones donde el usuario podrá ver tanto los módulos del Rol A como los del Rol B.

  Scenario: [REFINAMIENTO] Arquitectura Endpoint Dinámico (Anti-JWT Bloat) (CA-31)
    Given la prohibición estricta de usar JWT para gestionar la topología UI de gran tamaño
    Then el Frontend consumirá obligatoriamente un Endpoint Dinámico dedicado (`GET /api/v1/users/me/menu-layout`)
    And este endpoint retornará el JSON estricto con los módulos permitidos para el usuario logueado.

  Scenario: [REFINAMIENTO] Caché Híbrida y Auto-Curación Zero-Trust (CA-32)
    Given que el Frontend no debe saturar la red preguntando el menú en cada clic
    Then el sistema implementará memoria en Frontend (Pinia `useMenuStore`) cargando el menú 1 sola vez por sesión
    And el Backend cacheará esto en Redis (`@Cacheable`) purgándolo (`@CacheEvict`) si un rol es modificado
    And si el usuario intenta navegar a un menú recién revocado por caché vieja, el Backend emitirá un 403, y el Interceptor Axios del Frontend purgará el Pinia y emitirá un Toast: "Sus accesos han sido actualizados por el Administrador".


```
**Trazabilidad UX:** Wireframes Pantallas 14, 6, 7 y Workdesk (5).

---


### US-038: Asignación Multi-Rol y Sincronización EntraID
**Como** Administrador de Seguridad
**Quiero** asignar o sincronizar múltiples roles (Globales y de Proceso) a un mismo usuario autenticado
**Para** que pueda acceder a las distintas bandejas y tareas correspondientes a todos sus 'sombreros' operativos sin necesidad de tener cuentas separadas.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Multitenant RBAC, EntraID Sync & Identity Governance (Microservices Ready)

  # ==============================================================================
  # A. INICIO DE SESIÓN, JIT Y LA MUERTE SÚBITA (Arquitectura Stateless)
  # ==============================================================================
  Scenario: Tolerancia a Fallos del Kill-Switch (Redis Fail-Open Policy) (CA-01)
    Given la arquitectura de validación de Tokens (JWT) que consulta una Lista Negra en memoria (Redis) en <5ms para bloquear usuarios despedidos
    When el clúster de Redis sufre una caída temporal (Timeout) o partición de red (SPOF)
    Then la arquitectura exigirá un TTL máximo de 15 minutos al Token JWT base, y aplicará protección "Fail-Open Degradado".
    And el Gateway validará matemáticamente el Token JWT y PERMITIRÁ peticiones de sólo lectura (GET) para mantener viva la vista 360.
    And FORZARÁ "Fail-Closed" en toda mutación destructiva de estado (POST/PUT/DELETE) exigiendo escalamiento "Sudo-Mode", taponando vulnerabilidades de Separación de Funciones (SoD) si un usuario revocado aprovecha sus 15 minutos en la sombra.
    And paralelamente disparará una alerta técnica crítica al SysAdmin indicando: "Caché Offline - Operando en Degradación Segura sin Lista Negra".

  Scenario: Filtro de la Mochila Pesada (Anti-Token Bloat) (CA-02)
    Given un usuario (Ej: Gerente General) que pertenece a más de 150 grupos de seguridad en Microsoft EntraID
    When el Backend recibe el payload de grupos/roles del Identity Provider (o el flag de exceso `_odata.nextLink`)
    Then la arquitectura TIENE PROHIBIDO empaquetar cientos de roles irrelevantes dentro del Token JWT, previniendo que los servidores web colapsen con el error HTTP 431.
    And aplicará un "Filtro de Prefijo" estricto, ingiriendo e inyectando únicamente los roles que comiencen con la nomenclatura oficial de la plataforma (Ej: `ibpms_rol_*`).

  Scenario: Aprovisionamiento Just-In-Time (JIT) con Guardrail de Claims Mínimos Vitales (CA-03)
    Given un usuario nuevo que ingresa por primera vez a la URL del iBPMS vía SSO (EntraID)
    When el motor de Aprovisionamiento (JIT) parsea el Token de Microsoft para crearlo localmente
    Then el Backend evaluará el Token contra una matriz de "Claims Mínimos Vitales" (Ej: `Sucursal_ID`, `Codigo_Jefe`) necesarios para el enrutamiento de Camunda.
    And si el perfil corporativo de EntraID viene COMPLETO, lo deja pasar al Workdesk asignándole el rol inofensivo `[Ciudadano_Interno]`.
    But si el perfil corporativo está INCOMPLETO, el Frontend interceptará el acceso.
    And renderizará un Modal bloqueante de `[Completar Perfil Local]`, forzando al empleado a seleccionar o digitar los datos faltantes antes de habilitarle la plataforma, protegiendo la integridad del motor BPMN.

  # ==============================================================================
  # B. INFRAESTRUCTURA DE EMERGENCIAS Y CIERRE DE CICLO
  # ==============================================================================
  Scenario: Protocolo Break-Glass con Cierre de Ciclo Obligatorio (CA-04)
    Given que la infraestructura de Microsoft EntraID sufre una caída global (HTTP 503)
    Then el sistema habilitará un login de emergencia local ("Break-Glass Account") protegido por IP Whitelisting (Solo Red Corporativa/VPN).
    And por cumplimiento estricto de ciberseguridad (ISO 27001), ESTÁ PROHIBIDO referirse a esto como un "Backdoor" u ocultar la URL en el Frontend.
    And su uso exitoso disparará automáticamente alertas de Severidad Alta a la Gerencia de TI.
    When se restablezca el servicio de EntraID (resolución de la crisis)
    Then el iBPMS bloqueará las pantallas administrativas del Súper Admin con una alerta crítica (Tablero de Anomalías).
    And le exigirá mediante un Modal Inevitable rotar la contraseña o destruir las credenciales locales de la cuenta Break-Glass utilizada, erradicando el riesgo de dejar una "Puerta Trasera" durmiente.

  # ==============================================================================
  # C. LA PELEA DE PERMISOS Y ANOMALÍAS
  # ==============================================================================
  Scenario: Resolución Aditiva de Permisos (RBAC Simple) (CA-05)
    Given un usuario que hereda simultáneamente "Rol A" (Solo Lectura) y "Rol B" (Lectura y Escritura)
    Then el motor de políticas aplicará un modelo "Aditivo" estándar (Allow-Overrides) para la visibilidad de la UI.
    And el usuario gozará del súper-conjunto de permisos, evitando la extrema complejidad computacional de motores de reglas negativas (Deny-Overrides) en el MVP V1.

  Scenario: Detección y Contención de Segregación de Funciones (Juez y Parte) (CA-06)
    Given un usuario al que EntraID le ha inyectado por error roles incompatibles (Ej: "Creador de Pedido" y "Aprobador Financiero")
    When el usuario intenta aprobar una instancia de proceso que ÉL MISMO originó
    Then el sistema DEBE BLOQUEAR matemáticamente la transacción en el backend (Regla Fija: `Creator_ID != Approver_ID`).
    And el sistema le permitirá operar el resto de su día (Ej: Aprobar los pedidos de sus compañeros), pero JAMÁS cruzar el límite ético sobre su propia data.
    And disparará una Alerta Roja asíncrona hacia el Tablero de Anomalías de Seguridad (CA-12).

  # ==============================================================================
  # D. DELEGACIÓN Y RESCATE DE TAREAS (SRE Guaranteed Delivery)
  # ==============================================================================
  Scenario: Proxy Temporal de Autoridad y Exorcismo de Tareas Garantizado (CA-07)
    Given una Directora ("María") que sale de vacaciones por 15 días y tiene tareas operativas retenidas bajo su usuario (`assignee = maria`)
    When utiliza la Pantalla 14 para delegar su Rol jerárquico a un Coordinador ("Carlos")
    Then el sistema exige definir una [Fecha_Inicio] y [Fecha_Fin] estricta para la delegación.
    And la bitácora de auditoría estampará en cada acción de Carlos: "Ejecutado por: Carlos (En representación de: María)".
    And SIMULTÁNEAMENTE el iBPMS encola un evento asíncrono de "Auto-Unclaim Masivo" en el Message Broker (RabbitMQ) hacia Camunda.
    And si Camunda se encuentra Offline o en mantenimiento (HTTP 503), el Worker aplicará una Política de Reintentos (Retry Policy) y Dead Letter Queue (DLQ).
    And garantizando matemáticamente que el evento no se pierda y las tareas de María sean devueltas a la "Cola de Grupo" cuando el motor reviva, erradicando los Zombies irrecuperables.

  Scenario: El Exorcismo de Tareas por Despido (CA-08)
    Given una tarea operativa en Camunda asignada explícitamente a un empleado (`assignee = juan.perez`)
    When Juan renuncia y su perfil es desactivado en el módulo de seguridad
    Then el iBPMS no asumirá que Camunda se entera automáticamente.
    And el módulo de Identidad emitirá un evento interno asíncrono hacia RabbitMQ (con política de reintentos y DLQ igual al CA-07).
    And el Worker desencolará la orden, irá a Camunda y ejecutará un `Unclaim` masivo sobre TODAS las tareas vivas de Juan, devolviéndolas a disponibilidad pública para salvar los SLAs.

  # ==============================================================================
  # E. CONSOLIDACIÓN VISUAL Y TRAZABILIDAD EXTREMA
  # ==============================================================================
  Scenario: Trazabilidad Quirúrgica (Distributed Tracing V2 Ready) (CA-09)
    Given un usuario multi-rol ejecutando una transacción crítica
    When el Backend estampa el evento en la bitácora de auditoría
    Then almacenará el `user_id`, `timestamp` y un JSON inmutable con los "Roles Activos" (Claims) de su JWT en ese milisegundo exacto.
    And OBLIGATORIAMENTE inyectará un `Correlation-ID` o `Trace-ID` transversal en los Headers HTTP, garantizando que al migrar a Microservicios (V2), los auditores puedan rastrear el hilo de la transacción a través de todas las bases de datos.

  Scenario: Consolidación Transversal e Insignia de Procedencia (CA-10)
    Given un usuario con 3 roles operativos distintos
    When abre su vista de Workdesk
    Then el sistema consolida TODAS sus tareas en una única grilla unificada sin forzar saltos de perfil.
    And inyecta un Badge visual discreto en cada fila (Ej: `Rol: Aprobador_Nivel_2`) explicándole al usuario bajo qué prerrogativa de negocio se le exige resolver ese caso específico.

  Scenario: Indicador Tipográfico de Dominio en Cabecera (CA-11)
    Given el usuario multi-rol navegando la plataforma
    Then el Master Header renderizará un micro-texto o chip resumiendo visualmente sus 2 o 3 "Sombreros Principales" (Ej: `Director Comercial | Aprobador VIP`), validando que su sincronización con EntraID fue exitosa.

  # ==============================================================================
  # F. TABLERO DE ANOMALÍAS Y MANTENIMIENTO
  # ==============================================================================
  Scenario: Tablero de Resolución de Anomalías de Seguridad (CA-12)
    Given que el sistema detecta alertas de seguridad pasivas (Ej: El Conflicto SoD del CA-06 o el Break-Glass del CA-04)
    When el Administrador de Seguridad ingresa a la Pantalla de Configuración / RBAC (Pantalla 14)
    Then el sistema debe darle acceso a una pestaña especializada denominada "Tablero de Anomalías"
    And este tablero listará en color Rojo todas las incidencias de seguridad vivas detectadas por el motor.
    And obligará al Administrador a revisar el caso, subsanar el error a nivel EntraID/Local, y presionar físicamente un botón `[ ✅ Marcar como Subsanado ]` para limpiar la alerta del sistema.

  Scenario: Postergación de Reset de Password para V2 (CA-13)
    Given que el sistema opera en modo de IdP Local (Tabla propia de usuarios sin Azure)
    When un usuario olvida su contraseña
    Then la responsabilidad del Frontend y Backend de crear pantallas transaccionales de "Recuperar Contraseña via Email / OTP" queda estrictamente aplazada fuera del alcance del MVP V1.
    And el proceso de recuperación manual en V1 queda relegado a una solicitud verbal/correo al Administrador del Sistema.
```
**Trazabilidad UX:** Wireframes Pantalla 14 (Seguridad RBAC) y Tablero de Anomalías.

---



### US-048: Módulo Gestor Propio de Identidades (Internal IdP)
**Como** Súper Administrador del Sistema
**Quiero** gestionar centralizadamente los usuarios y roles internos del iBPMS
**Para** tener un control granular sobre quién accede a qué funcionalidades, incluso si el cliente no tiene un IdP externo robusto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Gestión de Identidades Internas (Internal IdP)
  Scenario: Creación Exclusiva por Administrador (V1 Centralizada) (CA-1)
    Given la necesidad de registrar un nuevo empleado en el iBPMS (Modo Standalone)
    Then para el MVP V1, el Súper Administrador es el único facultado para crear cuentas mediante un formulario interno (Pantalla 14)
    And la creación de cuentas mediante portales públicos de "Regístrate Aquí" queda estrictamente diferida para V2.

  Scenario: Gobernanza Estricta de Contraseñas Seguras (CA-2)
    Given la creación o actualización de una credencial local
    Then el Frontend (Formulario y Login) y el Backend deben acatar y forzar políticas Enterprise de seguridad
    And exigiendo obligatoriamente: Mínimo 8 caracteres, 1 Mayúscula, 1 Número y 1 Símbolo Especial
    And bloqueando el botón de [Guardar] si la entropía de la clave es débil.

  Scenario: Destrabe Administrativo de Credenciales (Reset Manual) (CA-3)
    Given un usuario bloqueado u olvidadizo (sin soporte de Auto-Reset vía Email en V1)
    Then el Súper Administrador visualizará un botón de emergencia `[Generar Clave Temporal]` en la ficha del empleado
    And al accionarlo, el sistema reemplaza el hash anterior y devuelve una cadena temporal visible por única vez para que el Admin la comunique verbalmente.

  Scenario: Fábrica de Roles Dinámicos (Role CRUD) (CA-4)
    Given una organización que no cuenta con grupos de Directorio Activo (EntraID)
    Then el módulo Interno de Identidad (Pantalla 14) debe poseer una pestaña de "Gestión de Roles"
    And permitiendo al Súper Administrador bautizar roles nuevos a voluntad (Ej: `Analista_Riesgo_Senior`)
    And otorgando una interfaz drag-and-drop o checkboxes multiselect para asociar usuarios hacia esos sombreros creados.

  Scenario: El Botón de Emergencia (Kill Switch Activo/Inactivo) (CA-5)
    Given un empleado enfrentando un proceso disciplinario o despido en tiempo real
    Then la tabla de usuarios (CRUD) expone un Toggle Switch visible `[Estado: Activo/Inactivo]`
    And al apagarlo, el Backend no solo marca el registro lógico como inactivo, sino que destruye activamente cualquier sesión viva (JWT/Redis) de ese usuario, expulsándolo instantáneamente al Login.

  Scenario: Asignación Híbrida de Múltiples Sombreros Locales (CA-6)
    Given la vista de edición de un usuario
    Then la interfaz debe permitir la selección múltiple (Ej: un Dropdown de selección múltiple)
    And logrando que el Administrador asigne libremente 1, 2 o N roles organizacionales al mismo individuo convergentes en una única sesión local (Sinergia con US-038).

  Scenario: Mutación de Interfaz en Modo Híbrido EntraID (CA-7)
    Given que el sistema se configuró para delegar el "Login de Acceso" a Microsoft EntraID, pero gobernar los "Roles" de forma local (iBPMS)
    When el Súper Administrador edita el perfil de un individuo
    Then la interfaz de Frontend oscurece o desaparece forzosamente los campos de "Contraseña" y "Cambiar Clave" en esa ficha
    And impidiendo intentos de alteración de credenciales locales que le pertenecen exclusivamente al IdP externo, evitando colisiones de Sincronía.
```
**Trazabilidad UX:** Pantalla 14 (Panel IdP Local).

---


### US-043: Configuración Global de Service Level Agreements (SLA)
**Como** PMO / Administrador Estratégico
**Quiero** disponer de una pantalla matriz de configuración central
**Para** que el motor de orquestación y el BAM no cuenten domingos o feriados en horas inhábiles ajustando la métrica a las "Horas reales corporativas".

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Business SLA Matrix Configuration and Multi-Zone Time-Warp Prevention
  Scenario: Inyección Arquitectónica del BusinessCalendar en Camunda Engine (CA-1)
    Given el administrador accede a la Matriz de Negocio (Pantalla 19)
    When se habilitan los Días Hábiles forzosamente basados en Horas (Ej: Lunes a Viernes de 8:00 a 17:00)
    Then el iBPMS TIENE ESTRICTAMENTE PROHIBIDO dejar que Camunda calcule los SLAs operativos usando su reloj UTC absoluto (24/7).
    And el Backend DEBE inyectar un Custom `BusinessCalendar` en el *Job Executor* del Engine de Camunda.
    And este Custom Calendar interceptará matemáticamente los `Timer Boundary Events` y `Due Dates` de tareas Humanas (`UserTasks`), leyendo en caliente la Matriz SLA de la BD.
    And garantizando que si una tarea (SLA 4 Hrs) entra un Viernes a las 16:00, el motor pause su cronómetro el fin de semana, detonando el Lunes a las 11:00 AM, protegiendo las métricas operativas (BAM).

  Scenario: Exención de Pausa para Timers Netamente Sistémicos (CA-2)
    Given procesos transaccionales autónomos (Ej: Conciliaciones MLOps o Purga de Datos) que deben ejecutarse los Domingos a las 3:00 AM
    When el Timer Event de tipo "System" se dispare según su configuración BPMN (Start Timer / System Catch)
    Then el Custom `BusinessCalendar` TIENE PROHIBIDO pausar estos cronómetros o recalcularlos al Lunes.
    And el Arquitecto BPMN deberá estipular visualmente una propiedad de extensión en Camunda (Ej: `camunda:property name="isBusinessSla" value="false"`) para saltar el bloqueo del calendario corporativo en hilos de máquina.

  Scenario: Recálculo Retroactivo Restringido a Batch Job (Anti-Deadlocks) (CA-3)
    Given que el administrador altera el rango de horas hábiles (Ej: de 17:00 a 16:30) y activa el Toggle de "Aplicar Retroactivamente a Tareas Vivas"
    When el PMO oprime `[Aplicar Matriz]`
    Then el Backend REST rechaza estructuralmente ejecutar el recálculo masivo de manera síncrona/inmediata en esa misma petición HTTP para prevenir Timeouts y Deadlocks de BD.
    And el sistema encolará un Job Asíncrono de tipo Batch por detrás que consumirá exclusivamente gRPC o la API asíncrona de Zeebe 8, modificando los Timer Boundary Events de forma nativa sin interactuar jamás con bases relacionales SQL, preservando la arquitectura RocksDB Stateless.
    And el UI mostrará un Modal informativo: "Recálculo masivo en progreso. Los SLAs vivos se actualizarán gradualmente en los próximos minutos".

  Scenario: Husos Horarios Estrictos en Geografías Híbridas (Timezones) (CA-4)
    Given que el cliente (Tenant) opera con usuarios en diferentes zonas horarias (Ej: UTC-5 Bogotá y UTC+1 Madrid)
    When el Custom `BusinessCalendar` intercepta un Timer de una Tarea Humana Asignada
    Then el motor priorizará la Zona Horaria (Timezone) predefinida en el Perfil del Trabajador `Assignee` o del `Candidate Group` en su defecto.
    And si un analista Europeo recibe un tarea, el fin de semana del motor de Camunda comenzará a aplicar 6 horas antes que para su homólogo en América, asegurando justicia laboral y SLAs inquebrantables cross-border.

  Scenario: Automatización de Festivos Externos con Fallback (CA-5)
    Given la necesidad legal de bloquear los contadores de SLA durante días de asueto local
    Then la matriz se sincroniza con una API Pública gubernamental o en la nube para auto-poblar los Días Feriados del Tenant específico.
    And si la API proveedora se cae, el sistema hace un "Fallback" a un grid manual editable en la Pantalla 19 por el PMO.

  Scenario: Alertas Preventivas de Quiebre de Nivel (Early Warning) (CA-6)
    Given que el temporizador (Ticking Engine) de una tarea se aproxima al 80% o "2 Horas restantes" de su tiempo total
    Then el motor SLA dispara automáticamente una alerta (hacia el Motor de Notificaciones US-049).
    And garantizando tiempo de reacción humano antes del verdadero vencimiento legal/operativo.
```
**Trazabilidad UX:** Wireframes Pantalla 19 (Configuración SLA).

---

## Módulo: Developer Portal & Extensibilidad (Zero-Trust)

### US-042: DevPortal: Generación Segura de API Keys y Extensibilidad

**Como** Arquitecto de Software / Desarrollador Integrador
**Quiero** acceder a un Portal de Desarrolladores (Pantalla 13) para crear API Keys y registrar Módulos Externos
**Para** poder construir integraciones y "Súper Apps" externas que interactúen con el iBPMS sin comprometer el Performance ni la Seguridad del Core.

**Contexto de Diseño:**
El DevPortal habilitará el ecosistema "Componible" (V2-Ready). Dado que intervienen humanos creando integraciones, se requiere blindar la red aplicando límites perimetrales directamente asociados al registro de la llave. La arquitectura **no puede ser vulnerada** bajo ninguna circunstancia, garantizando que el desarrollador humano opere exclusivamente dentro de los carriles definidos por el Arquitecto.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Zero-Trust Developer Portal Security
  Scenario: Autodestrucción del Secreto (OWASP) (CA-01)
    Given que el desarrollador requiere un Service Principal (API Key) para su módulo externo
    When el sistema le revela el "Client Secret" en texto plano
    Then el sistema otorga un máximo de 3 oportunidades (intentos de visualización/copiado)
    And al agotar el tercer intento, el secreto se oculta permanentemente y "autodestruye" visualmente, obligando a generar uno nuevo si se perdió.

  Scenario: Aislamiento por Cliente (Row-Level Tenancy) (CA-02)
    Given un Módulo Externo autenticado
    When envía peticiones de consulta (GET) o mutación (POST)
    Then la arquitectura forza a nivel de Base de Datos que SÓLO pueda interactuar con la data y expedientes pertenecientes al Cliente que pagó y autorizó dicho Módulo.
    And tiene prohibición estructural de realizar Borrados Físicos (DELETE) en instacias Core de clientes.

  Scenario: Ceguera Intencional y Sub-scopes restrictivos (CA-03)
    Given una API Key generada
    Then su token JWT debe nacer "capado" con un Sub-Scope limitante (Ej: `App_Read_Only`)
    And garantizando que el módulo pueda listar o leer tareas para su procesamiento, pero matemáticamente el backend rechace cualquier intento de "Edición" (Ceguera Operativa forzada).

  Scenario: Prevención Anti-DDoS y Radar de Tráfico (CA-04)
    Given un módulo de terceros volviéndose errático y enviando ráfagas masivas
    Then el Azure APIM Gateway (o Kong local) activa un "Radar de Control" con Rate-Limiting estructurado
    And retorna HTTP 429 cortando la comunicación en el perímetro, protegiendo a la Base de Datos y al motor Camunda.

  Scenario: Cuarentena de Nuevos Módulos (Sandbox Inyectado) (CA-05)
    Given un Módulo Externo recién registrado en el DevPortal
    Then por defecto nace en estado `Quarantine` apuntando a las bases de datos `Sandbox/Mirror`
    And no puede interactuar con el entorno productivo real del iBPMS hasta que el Administrador Global certifique su comportamiento.

  Scenario: Revocación por Reporte Humano (CA-06)
    Given una sospecha de brecha de seguridad en un módulo externo
    When un administrador humano procesa el reporte y oprime `[Revocar Llave]` en la Pantalla 13
    Then el Token JWT principal del módulo y todos los de refresco caen de inmediato, generando un proceso de desconexión forzosa del entorno.

  Scenario: Fechas de Caducidad y Alertas Administrativas (CA-07)
    Given que todas las "Llaves de Sistema" nacen con un Time-to-Live (TTL) finito (Fecha de expiración)
    Then semanas antes del vencimiento, el sistema dispara automáticamente alertas tempranas hacia el correo del Administrador para su gestión oportuna, advirtiendo del inminente apagón del módulo.

  Scenario: Alertas Activas contra "Curiosidad Maliciosa" (CA-08)
    Given que el token de un módulo intenta ejecutar un Endpoint o tocar una carpeta / archivo fuera de su Scope pre-aprobado (HTTP 403 Forbidden)
    Then el iBPMS bloquea la petición
    And dispara inmediatamente una notificación/alerta en tiempo real al correo del Oficial de Seguridad detallando el intento de intrusión.

  Scenario: Trazabilidad Extrema (La Culpa Compartida) (CA-09)
    Given un Módulo Externo realizando acciones permitidas (Ej. Aprobando un caso)
    Then el Audit Ledger del sistema guarda el log asociando el autor indudablemente a `[App_De_Tercero: CRM_Bot]`, proveyendo evidencia legal irrefutable de que fue la máquina del proveedor quien manipuló los datos y no un humano de nuestra plantilla.

  Scenario: Sandboxing Frontend (Aislamiento de Módulos Custom) (CA-10)
    Given que el equipo ha desarrollado un "Súper Módulo" con una UI exótica en React o Angular
    When este módulo se despliega dentro del ecosistema iBPMS (V1)
    Then el iBPMS cargará dicha UI de forma dinámica utilizando Iframes aislados (`sandbox`)
    And cualquier comunicación dinámica entre el Core (Vue 3) y el Iframe externo se realizará de manera controlada usando `window.postMessage()`, garantizando cero colisiones en el DOM, CSS Global o memoria (Pinia).

  Scenario: Tokens OIDC con Audiencia Específica (Extensibility Scope) (CA-11)
    Given un "Súper Módulo" registrado en el DevPortal
    When el Módulo obtiene sus credenciales OIDC contra Entra ID
    Then el JWT generado poseerá internamente Claims distintivos de extensión (Ej: `aud: ibpms.extensibility.supermodules`)
    And el SecurityFilterChain (Spring Boot) del Core leerá esta audiencia y bifurcará explícitamente los permisos, denegando el acceso a APIs puras de administrador humano.

  Scenario: Obediencia al Hexágono y Prohibición de Bypass JPA (CA-12)
    Given un Agente de Desarrollo o Humano codificando el Backend funcional de un "Súper Módulo"
    When intente persistir un nuevo dato asociado al caso o leer una variables
    Then la arquitectura le prohíbe técnicamente usar Interfaces `JpaRepository` o conectarse por JDBC a la instancia maestra de MySQL del Core
    And está obligado orgánicamente a instanciar un WebClient/RestTemplate para consumir los "Driving Adapters" (APIs REST Transaccionales en `/api/v1/`) como si fuera un sistema completamente alienígena de internet (Arquitectura Hexagonal Estricta).
```
**Trazabilidad UX:** Pantalla 13 (DevPortal).

---

### US-050: Identidad y Onboarding de Clientes Externos (CIAM / Zero-Public-Signup)
**Como** Sistema Core (iBPMS)
**Quiero** enviar una invitación segura (Magic Link) al correo de un cliente externo
**Para** que pueda crear su contraseña y acceder al Portal B2C, amarrando su usuario criptográficamente a su CRM_ID sin abrir formularios de registro público.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Secure Customer Onboarding and Identity (CIAM)

  Scenario: Prohibición de Registro Público (Zero-Public-Signup) (CA-01)
    Given la pantalla de Login del Portal Externo (portal.ibpms.com)
    Then la interfaz NO DEBE tener ningún enlace, botón o formulario que diga "Registrarse" o "Crear Cuenta".
    And la creación de identidades ciudadanas (External Users) solo puede nacer desde el interior del iBPMS (Vía API o evento interno), blindando el sistema contra bots.

  Scenario: Disparo de Invitación (Magic Link) por Evento (CA-02)
    Given un Cliente nuevo registrado en el CRM con el ID `CUST-999` y correo `juan@gmail.com`
    When el proceso BPMN llega a una tarea de "Invitar a Portal" O un analista oprime [Invitar] en la Vista 360
    Then el sistema generará un Token criptográfico de uso único (Magic Link).
    And el Motor de Notificaciones enviará un correo a `juan@gmail.com` con el botón "Crear mi Contraseña de Acceso".
    And el Magic Link tendrá una caducidad (TTL) rígida paramétrica (Ej: 24 horas).

  Scenario: Aterrizaje y Vinculación Criptográfica (Account Claiming) (CA-03)
    Given el cliente Juan que hace clic en el Magic Link dentro del tiempo permitido
    When aterriza en la página de "Definir Contraseña" del Portal B2C
    Then el sistema verifica que el Token no haya sido usado antes y bloquea la edición del campo de correo electrónico (Read-Only).
    And Juan digita su contraseña (cumpliendo políticas corporativas).
    And el sistema inscribe la cuenta en el Identity Provider (Azure AD B2C / Local).
    And OBLIGATORIAMENTE graba el valor `CUST-999` como un atributo inmutable (Custom Claim) dentro del Token del usuario (El "Bolsillo Secreto").
    And garantizando que a partir de ese momento, el candado Anti-BOLA (US-026) lea este atributo en cada inicio de sesión, impidiendo matemáticamente que Juan vea datos de otros clientes.
```

---

### US-051: Matriz de Gobernanza Visual y Enrutamiento RBAC (Frontend)
**Como** Administrador de Seguridad (CISO) / Arquitecto Frontend
**Quiero** que el motor de Vue.js gestione la visibilidad del DOM, la navegación de rutas y el estado reactivo con seguridad militar
**Para** garantizar cero fugas de información por parpadeos visuales (FOUC), proteger contra la adivinación de rutas por atacantes (URL Guessing), y soportar la fusión de múltiples roles dinámicos sin asfixiar la UX.

**Criterios de Aceptación (Gherkin):**
```gherkin
Feature: Frontend Visual Governance, Anti-FOUC and SRE Router Guards

  # ==============================================================================
  # A. RESOLUCIÓN DEL ESTADO Y PREVENCIÓN DE AMNESIA (GAPs 16 y 17)
  # ==============================================================================
  Scenario: Hidratación Síncrona del Estado Reactivo (Anti-Amnesia de F5) (CA-01)
    Given la arquitectura Single Page Application (SPA) basada en Vue 3 y Pinia
    When un usuario logueado presiona [F5] o recarga directamente una URL profunda (Ej: `/admin/modeler`)
    Then el interceptor de navegación (`router.beforeResolve`) TIENE PROHIBIDO evaluar los permisos instantáneamente.
    And deberá invocar una promesa bloqueante (`await hydrateAuth()`) forzando al Router a esperar a que Pinia recupere el Token del LocalStorage y recalcule los Claims.
    And previniendo falsos positivos de expulsión (403) causados por la latencia de lectura de la memoria RAM.

  Scenario: Renderizado Progresivo Estricto y FOUC Controlado (LCP Optimization) (CA-02)
    Given el proceso de montaje de la aplicación (SPA Vue 3)
    When el usuario ingresa a la URL
    Then el Frontend renderizará INMEDIATAMENTE el App Shell (Sidebar y Header Maestros) basándose en los Claims básicos del JWT en Caché para garantizar una métrica óptima de Largest Contentful Paint (LCP).
    And el `[Skeleton Loader Transversal]` se aplicará ESTRICTAMENTE solo sobre el contenedor `<Router View>` (Main Content).
    And este Skeleton central solo se destruirá cuando las promesas asíncronas de permisos RBAC del Backend se resuelvan completamente.
    And garantizando fluidez de navegación ultrarrápida sin generar parpadeos (FOUC) de botones prohibidos en la zona de trabajo.

  # ==============================================================================
  # B. DEFENSA PERIMETRAL Y RUTAS (GAP 18)
  # ==============================================================================
  Scenario: Gaslighting Cibernético (Security by Obscurity 404 vs 403) (CA-03)
    Given un usuario operativo o externo que adivina e intenta acceder a una URL restringida (URL Guessing)
    When el Router Guard intercepta la navegación detectando permisos insuficientes (Token válido, pero sin Rol)
    Then la arquitectura TIENE PROHIBIDO redirigirlo al Workdesk `/` emitiendo un "403 Forbidden" (lo cual confirmaría que la ruta confidencial existe).
    And el Router inyectará de frente el componente `NotFound404.vue` (Página no encontrada) manteniendo intacta la URL en la barra de direcciones.
    And impidiendo matemáticamente que un hacker logre mapear la estructura de directorios del sistema.

  Scenario: Jerarquía de Redirección y Atesorador de Enlaces (CA-04)
    Given el Router Guard evaluando una excepción de acceso
    When determina la causal de la penalización
    Then si el Token JWT EXPIRÓ (401): Redirigirá pasivamente a `/login`, limpiando el Storage.
    And si el Token VIVE pero el usuario guardó un "Hyperlink Viejo" de un menú al que ya no tiene acceso: Aplicará el escenario de Falso 404 SIN destruir su LocalStorage, protegiendo los borradores lícitos que esté trabajando en otras pestañas.

  Scenario: Excepciones Perimetrales Controladas (Magic Links y Docs) (CA-05)
    Given la existencia de rutas transitorias y documentación técnica
    Then el Router Guard poseerá una bandera `meta: { isPublic: true }`.
    And omitirá la evaluación RBAC pesada para: Pantallas B2C accedidas mediante "Magic Links" (US-050), y Rutas técnicas locales (Swagger/Storybook), acelerando la carga sin comprometer el Core.

  # ==============================================================================
  # C. COMPOSICIÓN DINÁMICA DE MENÚS Y PRIVILEGIOS
  # ==============================================================================
  Scenario: Backend-Driven UI, Auto-Colapso de Nodos y Caché de Menú (CA-06)
    Given la fusión de múltiples roles en un mismo usuario
    When el Sidebar calcula las carpetas a renderizar
    Then la matriz de "Permisos vs Rutas" NO vivirá codificada en duro (Hardcoded) en el Router de Vue, sino que será inyectada mediante un JSON asíncrono desde el Backend.
    And si el cruce de roles oculta todos los sub-menús de una categoría padre (Ej: Ocultamos BPMN y Formularios), la carpeta padre completa "Administración" se ocultará automáticamente del DOM (Auto-Collapse).
    And el árbol de navegación resultante será cacheado en Pinia tras el Login para no re-computar directivas en cada transición de vista.

  Scenario: Dashboard Bifurcado por Composición de Widgets (CA-07)
    Given la ruta raíz del sistema `/` (Workdesk)
    When diferentes roles (Operador vs Súper Admin) acceden a la misma URL
    Then el sistema TIENE PROHIBIDO redirigir a rutas hardcodeadas separadas (Ej: `/dashboard-admin`).
    And utilizará la misma vista raíz inyectando dinámicamente (Component Composition) los *Widgets* (Grafana vs Grillas Kanban) según los permisos aditivos de Pinia en la misma coordenada web.

  Scenario: Dependencias Cruzadas y Privilegios de Solo Lectura (Granularidad CRUD) (CA-08)
    Given un Arquitecto de Procesos que necesita invocar una Regla IA dentro de su diagrama BPMN
    Then el Frontend le otorgará un privilegio degradado (Read-Only) hacia la ruta del Diccionario de la IA.
    And le permitirá consultar el catálogo, pero la directiva condicional a nivel de componente ocultará/destruirá físicamente los botones de `[+ Nueva Regla]` y `[Eliminar]`, reservados para el Administrador IA.

  # ==============================================================================
  # D. CONTROLES DE ALTA FRICCIÓN Y SALVAVIDAS
  # ==============================================================================
  Scenario: Re-Autenticación para Funciones Destructivas (Sudo Mode) (CA-09)
    Given una sesión iniciada bajo el rol máximo de `ROLE_SUPER_ADMIN`
    When este usuario intenta ejecutar una acción destructiva (Ej: Purgar BD, Borrar Tenant)
    Then la validación estándar del Router NO es suficiente.
    And el Frontend suspenderá el POST y renderizará un "Re-Prompt" (Modal de Seguridad) exigiendo la re-digitación de la contraseña o token EntraID para confirmar la transacción, previniendo secuestros de sesión en PCs desbloqueadas.

  Scenario: Auditoría Forzosa al Revelar Secretos API (El Ojo de Sauron) (CA-10)
    Given el rol `ROLE_INTEGRITY_ENGINEER` ingresando a la vista "Integraciones API"
    When el componente se monta para mostrar credenciales o Tokens OAuth estáticos
    Then los Secretos se renderizarán ofuscados por defecto (`*****************`).
    And al hacer clic en "Mostrar 👁️", el Frontend disparará obligatoriamente un evento asíncrono de Telemetría (Audit-Log POST) hacia el backend registrando la visualización del secreto en ese milisegundo.

  Scenario: Revocación en Caliente y Botón de Pánico Incondicional (Return Home) (CA-11)
    Given la operativa en tiempo real del Frontend
    When un Súper Administrador revoca un rol a un usuario conectado
    Then un evento WebSocket (`[ROLE_REVOKED]`) obligará a Pinia a expulsar al usuario al `/login` en vivo.
    And en caso de que un usuario quede atrapado en un "Dead Loop" de redirecciones por fallos de permisos locales, el *Master Layout* garantizará la renderización incondicional del botón `[Cerrar Sesión / Ir al Inicio]` por fuera del `router-view` para forzar la limpieza del estado.
```
**Trazabilidad UX:** Componentes de Navegación Global Vue Router (`router/index.ts`) y Menú Lateral (`MainLayout.vue`).

---
