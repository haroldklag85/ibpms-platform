# Journey J-05: Gobernanza Administrativa — RBAC → Multi-Rol → IdP → SLA → Dashboard

> **Journey:** J-05 — Gobernanza y Configuración Zero-Trust del Ecosistema Administrativo
> **Actor principal:** Súper Administrador / CISO / PMO
> **Criticidad:** 🔴 ALTA (Cubre las 5 US Críticas V1)
> **US Cruzadas:** US-048, US-036, US-038, US-025, US-043
> **Épica:** Seguridad, Identidad y Configuración (Épica E)
> **Fecha:** 2026-04-18
> **Autor:** Agente PO (Antigravity)
> **Formato:** Híbrido (Manual paso-a-paso + links a `.spec.ts`)

---

## Narrativa del Journey

Este Journey certifica el flujo completo de preparación administrativa del iBPMS. Un Súper Administrador configura desde cero el ecosistema de identidad, roles, SLA y visibilidad para que la plataforma quede operativa. El flujo es:

```
┌────────────────────────────────────────────────────────────────────────┐
│  1. Crear usuario local (US-048)                                      │
│  2. Asignar roles RBAC + herencia piramidal (US-036)                 │
│  3. Configurar multi-rol + sincronización EntraID (US-038)           │
│  4. Verificar visibilidad segmentada por rol (US-025)                │
│  5. Configurar SLA corporativo + calendario hábil (US-043)           │
│  6. Validar Dashboard dinámico con Cards filtradas por rol (US-025)   │
│  7. Ejecutar Kill-Session + delegación temporal (US-036, US-038)     │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Precondiciones

| # | Precondición | Verificación | US Origen |
|---|-------------|-------------|-----------|
| PRE-1 | iBPMS desplegado con BD PostgreSQL limpia (Día Cero) | `docker-compose up` exitoso | — |
| PRE-2 | Usuario `Super_Administrador` root inyectado por seed | Login exitoso en `/login` | US-036 CA-2 |
| PRE-3 | Redis operativo para blacklist JWT | `redis-cli ping` = PONG | US-038 CA-1 |
| PRE-4 | Licuibase/Migrations ejecutadas con esquema RBAC | Tablas `ibpms_roles`, `ibpms_permissions`, `ibpms_user_roles` existen | US-036 CA-19 |
| PRE-5 | Al menos 1 Process Definition publicado en Camunda | GET `/engine-rest/process-definition` retorna ≥1 | US-005 |
| PRE-6 | Pantalla 14 accesible desde la ruta `/admin/security` | Navegación exitosa | US-025 CA-1 |

---

## Escenarios UAT — Bloque 1: Internal IdP (US-048)

### CU-J05-01: Creación de Usuario Local por Administrador
**CA Mapeado:** US-048 CA-1, US-048 CA-2
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Navega a Pantalla 14 → Pestaña "Gestión de Usuarios" | Se renderiza la tabla CRUD de usuarios |
| 2 | Súper Admin | Pulsa botón `[+ Nuevo Usuario]` | Se despliega formulario con campos: Nombre, Email, Contraseña, Roles |
| 3 | Súper Admin | Ingresa contraseña débil: `abc123` | El botón `[Guardar]` permanece deshabilitado. Validación inline muestra: "Mínimo 8 caracteres, 1 mayúscula, 1 número, 1 símbolo" |
| 4 | Súper Admin | Corrige contraseña a `S3gur@2026!` | Validación pasa. Botón `[Guardar]` se habilita |
| 5 | Súper Admin | Pulsa `[Guardar]` | HTTP 201 Created. Usuario aparece en la tabla con estado `Activo` |
**Automatización:** `e2e/specs/j05/idp-user-creation.spec.ts`

### CU-J05-02: Reset Manual de Credenciales (Destrabe Administrativo)
**CA Mapeado:** US-048 CA-3
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Selecciona al usuario "ana.garcia" en la tabla de usuarios | Se abre ficha de detalle |
| 2 | Súper Admin | Pulsa botón de emergencia `[Generar Clave Temporal]` | Modal de confirmación: "¿Generar nueva clave temporal?" |
| 3 | Súper Admin | Confirma acción | Sistema muestra cadena temporal visible **por única vez**: `TmpK3y!x9z2` |
| 4 | Súper Admin | Cierra el modal y lo reabre | La cadena NO se muestra de nuevo. Texto: "La clave temporal ya fue revelada" |
| 5 | Ana García | Ingresa con la clave temporal desde otra sesión | Login exitoso. Sistema fuerza cambio de contraseña obligatorio |
**Automatización:** `e2e/specs/j05/idp-password-reset.spec.ts`

### CU-J05-03: Asignación Híbrida Multi-Rol Local
**CA Mapeado:** US-048 CA-4, US-048 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Abre pestaña "Gestión de Roles" en Pantalla 14 | Se renderiza lista de roles con botón `[+ Nuevo Rol]` |
| 2 | Súper Admin | Crea rol `Analista_Riesgo_Senior` | HTTP 201. Rol aparece en la tabla |
| 3 | Súper Admin | Edita usuario "pedro.lopez", selecciona roles múltiples: `Analista_Riesgo_Senior` + `Auditor_Junior` | Dropdown multi-select permite selección simultánea |
| 4 | Súper Admin | Guarda asignación | HTTP 200. La tabla `ibpms_user_roles` refleja 2 registros para pedro.lopez |
| 5 | Pedro López | Inicia sesión | JWT contiene ambos roles en Claims. Header muestra chip: `Analista Riesgo Senior | Auditor Junior` (US-038 CA-11) |
**Automatización:** `e2e/specs/j05/idp-multi-role-assignment.spec.ts`

### CU-J05-04: Mutación de Interfaz en Modo Híbrido EntraID
**CA Mapeado:** US-048 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Configura sistema en modo "Login EntraID + Roles Locales" | Feature flag activado |
| 2 | Súper Admin | Abre ficha de edición de un usuario sincronizado desde EntraID | — |
| 3 | Sistema | Renderiza formulario de edición | Los campos "Contraseña" y "Cambiar Clave" están **oscurecidos/ocultos** (`v-if=false`). No existen en el DOM |
| 4 | Súper Admin | Inspecciona DOM con DevTools del navegador | Confirma: NO hay inputs de contraseña renderizados. Ocultamiento físico, no CSS `display:none` |
**Automatización:** `e2e/specs/j05/idp-hybrid-mode-mutation.spec.ts`

---

## Escenarios UAT — Bloque 2: RBAC Zero-Trust (US-036)

### CU-J05-05: Herencia Piramidal de Roles
**CA Mapeado:** US-036 CA-6, US-036 CA-19
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Crea rol `Analista_Riesgo` con permisos: READ(Workdesk), EXECUTE(Proceso_Crédito) | Rol creado con 2 permisos |
| 2 | Súper Admin | Crea rol `Gerente_Riesgo` y configura `parent_role_id` = `Analista_Riesgo` | Herencia piramidal establecida |
| 3 | Súper Admin | Agrega permiso adicional a `Gerente_Riesgo`: WRITE(Dashboard_BAM) | Gerente_Riesgo tiene 3 permisos (2 heredados + 1 propio) |
| 4 | Sistema | Ejecuta query CTE recursiva para computar permisos efectivos | Retorna los 3 permisos fusionados correctamente |
| 5 | Gerente Riesgo | Inicia sesión | Accede a Workdesk + Proceso_Crédito (heredados) + Dashboard_BAM (propio) |
**Automatización:** `e2e/specs/j05/rbac-pyramidal-inheritance.spec.ts`

### CU-J05-06: Clonación de Perfiles por Plantilla (Mass Assignment)
**CA Mapeado:** US-036 CA-3, US-036 CA-19
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Crea un `[Rol Plantilla]` con `is_template=true` y 15 permisos configurados | Rol plantilla creado |
| 2 | Súper Admin | Selecciona 50 usuarios de la tabla (checkbox multi-select) | 50 usuarios seleccionados |
| 3 | Súper Admin | Pulsa `[Asignar Rol Plantilla]` y confirma | HTTP 200. INSERT batch de 50 registros en `ibpms_user_roles` |
| 4 | Sistema | Genera log de auditoría | 50 entradas en `ibpms_audit_log` con `assigned_by: Super_Admin`, `timestamp_utc` |
| 5 | Cualquier usuario de los 50 | Inicia sesión | JWT contiene los 15 permisos del Rol Plantilla |
**Automatización:** `e2e/specs/j05/rbac-template-mass-assignment.spec.ts`

### CU-J05-07: Segregación Iniciador vs Ejecutor
**CA Mapeado:** US-036 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Súper Admin | Configura matrix: usuario "carlos" tiene `can_initiate_process=true` para "Proceso_Credito" y `can_execute_tasks=false` | Permisos grabados en BD |
| 2 | Carlos | Inicia sesión y abre Pantalla 0 (Catálogo) | Botón `[+ Iniciar Nuevo Caso]` es VISIBLE para Proceso_Credito |
| 3 | Carlos | Instancia un caso de Proceso_Credito | HTTP 201. Caso creado exitosamente |
| 4 | Carlos | Navega al Workdesk para ejecutar la primera tarea del caso que creó | La tarea NO aparece en su bandeja (filtro RBAC: `can_execute_tasks=false`) |
| 5 | Analista autorizado | Ve la tarea en la Cola del Equipo y la reclama normalmente | Confirmación de segregación funcional |
**Automatización:** `e2e/specs/j05/rbac-initiator-vs-executor.spec.ts`

### CU-J05-08: Delegación Temporal Autónoma con Exorcismo de Tareas
**CA Mapeado:** US-036 CA-9, US-036 CA-23, US-038 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Gerente María | Abre Pantalla 14 → "Panel de Delegación" | Formulario con campos: Suplente, Fecha Inicio, Fecha Fin |
| 2 | Gerente María | Selecciona suplente "Carlos", rango: 15-30 Abril | Delegación configurada |
| 3 | Sistema | Al llegar la Fecha Inicio (15 Abril) | Auto-Unclaim masivo se encola en RabbitMQ. Tareas de María pasan a Cola de Grupo. Carlos hereda rol delegado + tareas in-flight |
| 4 | Carlos | Abre Workdesk | Ve las tareas de María con sello: `[Delegación temporal de María]`. Bitácora de cada acción refleja: "Ejecutado por: Carlos (En representación de: María)" |
| 5 | Sistema | Al llegar la Fecha Fin (30 Abril) | Tareas NO completadas por Carlos retornan automáticamente a la bandeja de María con sello: `[Retornada post-delegación]` |
| 6 | Auditor | Consulta `ibpms_audit_log` | Registros completos de transferencia y retorno de tareas |
**Automatización:** `e2e/specs/j05/rbac-delegation-lifecycle.spec.ts`

### CU-J05-09: Reporte ISO 27001 de Identity Governance
**CA Mapeado:** US-036 CA-16, US-036 CA-24
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | CISO | Navega a Pantalla 14 → "Informes de Fiscalización" | Botón `[Generar Reporte ISO 27001]` visible |
| 2 | CISO | Pulsa el botón de generación | Sistema procesa (spinner). Genera CSV/Excel |
| 3 | Sistema | Compila sábana matrizal | Cruce: `[Todos los Usuarios/Robots]` × `[Todos los Roles Activos]` × `[Todos los Procesos Iniciables/Ejecutables]` |
| 4 | Sistema | Incluye metadatos del reporte | Fecha/hora UTC, usuario solicitante, hash SHA-256 del contenido |
| 5 | CISO | Descarga el archivo | CSV bien formado con todas las columnas. Hash verificable para certificar integridad |
| 6 | Sistema | Persiste registro en `ibpms_audit_reports` | Auditoría disponible para comparación entre periodos |
**Automatización:** `e2e/specs/j05/rbac-iso27001-report.spec.ts`

---

## Escenarios UAT — Bloque 3: Multi-Rol y EntraID (US-038)

### CU-J05-10: Filtro Anti-Token Bloat (Prefijo ibpms_rol_*)
**CA Mapeado:** US-038 CA-2
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Recibe payload de EntraID con 150 grupos para usuario "gerente.general" | Payload contiene: `ibpms_rol_admin`, `ibpms_rol_auditor`, `Office365_Users`, `Teams_All`, ... (150 items) |
| 2 | Backend | Aplica Filtro de Prefijo estricto | Solo ingiere roles que comienzan con `ibpms_rol_*` |
| 3 | Backend | Genera JWT | Token contiene únicamente los 2-3 roles `ibpms_rol_*`. NO empaqueta los 148 roles irrelevantes |
| 4 | Sistema | Valida tamaño del JWT | Header HTTP total < 8KB (prevención HTTP 431) |
**Automatización:** `e2e/specs/j05/entraid-anti-token-bloat.spec.ts`

### CU-J05-11: Aprovisionamiento JIT con Guardrail de Claims Mínimos
**CA Mapeado:** US-038 CA-3
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Nuevo Empleado | Ingresa por primera vez vía SSO (EntraID) | Backend parsea el Token de Microsoft |
| 2 | Backend | Evalúa Claims Mínimos Vitales: `Sucursal_ID`, `Codigo_Jefe` | Perfil EntraID viene INCOMPLETO (falta `Sucursal_ID`) |
| 3 | Frontend | Intercepta acceso al Workdesk | Renderiza Modal bloqueante: `[Completar Perfil Local]` |
| 4 | Empleado | Selecciona `Sucursal_ID` del dropdown y pulsa `[Continuar]` | Modal se cierra. Claims completados |
| 5 | Sistema | Asigna rol inofensivo `[Ciudadano_Interno]` | Empleado puede acceder al Workdesk con capacidades nulas hasta que Admin configure procesos de autogestión |
**Automatización:** `e2e/specs/j05/entraid-jit-provisioning.spec.ts`

### CU-J05-12: Protocolo Break-Glass con Cierre de Ciclo
**CA Mapeado:** US-038 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Simula caída global de EntraID (HTTP 503) | Login SSO falla para todos los usuarios |
| 2 | Súper Admin | Accede a URL de login de emergencia local desde Red Corporativa/VPN | Pantalla de "Break-Glass Account" se renderiza |
| 3 | Súper Admin | Ingresa credenciales locales de emergencia | Login exitoso. Alerta Severidad Alta disparada a Gerencia IT |
| 4 | Sistema | Restablece servicio EntraID (simulación) | — |
| 5 | Sistema | Bloquea pantallas administrativas con alerta crítica | Modal Inevitable: "Se detectó uso de Break-Glass. Rote la contraseña inmediatamente" |
| 6 | Súper Admin | Rota/destruye credenciales Break-Glass | Modal se cierra. Puerta trasera erradicada. Auditoría completa grabada |
**Automatización:** `e2e/specs/j05/entraid-break-glass-protocol.spec.ts`

### CU-J05-13: Detección SoD (Juez y Parte)
**CA Mapeado:** US-038 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Configura usuario "pablo" con roles: `Creador_Pedido` + `Aprobador_Financiero` (inyectados por error desde EntraID) | Multi-rol con conflicto SoD |
| 2 | Pablo | Crea un Pedido de Compra #PC-500 (como Creador) | HTTP 201. Instancia creada correctamente |
| 3 | Pablo | Intenta aprobar el Pedido #PC-500 que ÉL MISMO creó (como Aprobador) | Backend BLOQUEA la transacción: `Creator_ID != Approver_ID`. HTTP 403 con mensaje: "No puede aprobar transacciones propias (SoD)" |
| 4 | Pablo | Intenta aprobar Pedido #PC-501 de un compañero | HTTP 200. Aprobación exitosa (solo se bloquea sobre su propia data) |
| 5 | Sistema | Dispara Alerta Roja asíncrona al Tablero de Anomalías | Incidencia registrada para resolución por CISO |
**Automatización:** `e2e/specs/j05/entraid-sod-detection.spec.ts`

---

## Escenarios UAT — Bloque 4: Visibilidad Segmentada por Rol (US-025)

### CU-J05-14: Privilegio Absoluto del System Admin (Omnipresencia)
**CA Mapeado:** US-025 CA-1
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | System Admin | Inicia sesión con rol `system_admin` | JWT contiene `role: system_admin` |
| 2 | Frontend | Renderiza Master Page | Sidebar despliega TODOS los accesos: Inicio, Workdesk, Inbox, Proyectos, Dashboards, Configuración, Integraciones, Seguridad, SGDEA |
| 3 | Frontend | Renderiza Header | Campana de Notificaciones Full + Búsqueda Inter-Dominio activas |
| 4 | Frontend | Renderiza Main Content | TODAS las Action Cards estratégicas sin censura. Botón `[+ Iniciar Nuevo Proceso]` visible |
**Automatización:** `e2e/specs/j05/cards-system-admin-omnipresence.spec.ts`

### CU-J05-15: Segregación Estructural del Operario Base (Workdesk Only)
**CA Mapeado:** US-025 CA-2, US-025 CA-10
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Auditor Junior | Inicia sesión con rol `auditor_junior` | — |
| 2 | Frontend | Renderiza Sidebar | Solo muestra: `[🏠 Inicio]`, `[📋 Workdesk]`, `[📂 Histórico Propio]`. Módulos Admin NO están en el DOM (`v-if` destrucción) |
| 3 | Auditor Junior | Inspecciona DOM con DevTools | Confirma: NO existen nodos DOM para "Configuración", "Seguridad", "Integraciones" (Ocultamiento Físico CA-10, no `disabled`) |
| 4 | Frontend | Renderiza Main Content | Botón `[+ Iniciar Nuevo Proceso]` NO existe en el DOM (rol sin derechos de instanciación) |
**Automatización:** `e2e/specs/j05/cards-operator-segregation.spec.ts`

### CU-J05-16: Selector de Perfil Activo (Conflicto Multi-Rol)
**CA Mapeado:** US-025 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Usuario Multi-Rol | Inicia sesión con roles: `auditor_junior` + `sac_leader` | JWT contiene ambos roles |
| 2 | Frontend | Detecta multiplicidad de roles contradictorios | NO fusiona caóticamente ambas interfaces |
| 3 | Frontend | Inyecta en Header Superior | "Selector de Perfil Activo" (Dropdown) con opciones: `Auditor Junior`, `Líder SAC` |
| 4 | Usuario | Selecciona `Auditor Junior` | Sidebar y Main Content redibujan instantáneamente mostrando solo vista operativa restringida |
| 5 | Usuario | Cambia a `Líder SAC` | Sidebar muestra acceso privilegiado a `[📥 Inbox Inteligente]`. Cards analíticas de "Volumen de Embudo" aparecen |
**Automatización:** `e2e/specs/j05/cards-multi-role-selector.spec.ts`

### CU-J05-17: Refresco Forzoso por Alteración de Privilegios en Caliente
**CA Mapeado:** US-025 CA-7
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Navega activamente en la plataforma | Sesión activa |
| 2 | System Admin | Modifica roles del operario desde Pantalla 14 (revoca `Analista_Riesgo`) | — |
| 3 | Sistema | NO refleja cambio "en vivo" mágicamente | Estado de formularios en progreso protegido |
| 4 | Sistema | Exige Log-Out / Log-In forzoso | Modal: "Sus privilegios han sido modificados. Debe re-autenticarse" |
| 5 | Operario | Re-inicia sesión | Nuevo JWT parseado desde cero. Sidebar muestra nueva visibilidad sin el módulo de Riesgo |
**Automatización:** `e2e/specs/j05/cards-privilege-hot-refresh.spec.ts`

---

## Escenarios UAT — Bloque 5: SLA Corporativo (US-043)

### CU-J05-18: Inyección de BusinessCalendar en Camunda
**CA Mapeado:** US-043 CA-1
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | PMO | Navega a Pantalla 19 (Configuración SLA) | Matriz de horarios hábiles renderizada |
| 2 | PMO | Configura: Lunes-Viernes, 8:00 AM - 5:00 PM | Persistido en BD |
| 3 | Sistema | Tarea con SLA 4h entra un Viernes a las 4:00 PM | Camunda inicia timer con BusinessCalendar inyectado |
| 4 | Sistema | Fin de semana (Sábado-Domingo) | Timer PAUSADO. Cronómetro no avanza |
| 5 | Sistema | Lunes 8:00 AM | Timer reanuda. Quedan 3h. Deadline real: Lunes 11:00 AM |
| 6 | Dashboard BAM | Muestra status | SLA en 🟡 Amarillo (75% consumido). No rojo prematuro por horas inhábiles |
**Automatización:** `e2e/specs/j05/sla-business-calendar-injection.spec.ts`

### CU-J05-19: Alertas Preventivas de Quiebre de Nivel
**CA Mapeado:** US-043 CA-6
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Timer de tarea TK-200 alcanza 80% del SLA total (2h restantes de 10h) | — |
| 2 | Motor SLA | Dispara alerta automática al Motor de Notificaciones | — |
| 3 | Operario Asignado | Recibe notificación in-app (Toast persistente) | "⚠️ Tarea TK-200: 2 horas restantes para quiebre de SLA" |
| 4 | Supervisor | Recibe correo electrónico de early warning | Correo con detalle: Tarea, Operario, Tiempo restante, Proceso |
**Automatización:** `e2e/specs/j05/sla-early-warning-alerts.spec.ts`

### CU-J05-20: Husos Horarios en Geografías Híbridas
**CA Mapeado:** US-043 CA-4
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | PMO | Configura: Tenent con usuarios en UTC-5 (Bogotá) y UTC+1 (Madrid) | Timezone configurada por perfil de trabajador |
| 2 | Sistema | Asigna tarea SLA 8h a analista europeo (Madrid, UTC+1) a las 16:00 UTC+1 | BusinessCalendar aplica timezone del assignee |
| 3 | Sistema | Fin de jornada Madrid: 17:00 UTC+1 | Timer PAUSA tras 1h hábil (quedan 7h) |
| 4 | Sistema | Siguiente mañana Madrid: 8:00 UTC+1 | Timer REANUDA. SLA continúa desde 7h restantes |
| 5 | Verificación | Misma tarea si fuera de Bogotá (UTC-5) | Pausaría 6 horas después que Madrid. Justicia laboral cross-border |
**Automatización:** `e2e/specs/j05/sla-timezone-fairness.spec.ts`

---

## Escenarios Negativos

### CU-J05-NEG-01: Kill-Session y Destrucción JWT
**CA Mapeado:** US-036 CA-14, US-038 CA-1, US-036 CA-21
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Empleado "Juan" está en proceso disciplinario. Tiene sesión activa operando casos |
| 2 | Súper Admin pulsa `[Revocar Todo y Matar Sesión]` en la ficha de Juan (Pantalla 14) |
| 3 | Backend inserta `jti` del JWT de Juan en blacklist Redis con TTL = tiempo restante del token |
| 4 | Siguiente request de Juan: Spring Security Filter consulta blacklist Redis. Encuentra match → HTTP 401 |
| 5 | Frontend de Juan expulsa instantáneamente a `/login`. LocalStorage purgado. Sesión destruida |
| 6 | Juan intenta re-loguearse → Estado `Inactivo` → Login rechazado: "Cuenta desactivada" |

### CU-J05-NEG-02: Navegación Forzada por URL a Módulo Prohibido (Route Guard)
**CA Mapeado:** US-025 CA-5
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario Base (sin permisos admin) pega URL directa: `/admin/security` en el navegador |
| 2 | `RouteGuards.ts` intercepta la navegación ANTES de montar el componente |
| 3 | Redirige forzosamente al `[🏠 Inicio]` |
| 4 | Despliega Toast: "No tiene permisos para acceder a esta sección" |
| 5 | En producción: se loguea como evento de seguridad en `ibpms_audit_log` |

### CU-J05-NEG-03: Desactivación de usuario con Tareas In-Flight (Exorcismo)
**CA Mapeado:** US-038 CA-8, US-048 CA-5
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Juan tiene 5 tareas asignadas en Camunda (`assignee = juan.perez`) |
| 2 | Súper Admin desactiva a Juan desde el Toggle Switch `[Estado: Inactivo]` |
| 3 | Backend marca registro como inactivo + destruye sesión JWT/Redis (US-048 CA-5) |
| 4 | Backend emite evento asíncrono a RabbitMQ para Exorcismo de Tareas (US-038 CA-8) |
| 5 | Worker desencola orden → ejecuta `Unclaim` masivo en Camunda sobre 5 tareas de Juan |
| 6 | Las 5 tareas regresan a disponibilidad pública en la Cola del Equipo para salvar SLAs |
| 7 | Si RabbitMQ offline → Retry Policy + DLQ garantizan entrega eventual |

### CU-J05-NEG-04: Fail-Open de Redis para Operaciones Destructivas
**CA Mapeado:** US-038 CA-1
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Redis cae (simular `docker stop redis`) |
| 2 | Operario ejecuta GET `/api/v1/workdesk/tasks` | 
| 3 | Gateway valida JWT matemáticamente. PERMITE petición de solo lectura (Fail-Open Degradado) |
| 4 | Operario intenta POST `/api/v1/tasks/{id}/complete` (mutación destructiva) |
| 5 | Gateway BLOQUEA: "Fail-Closed" para operaciones POST/PUT/DELETE. HTTP 503: "Servicio en modo degradado" |
| 6 | Sistema dispara alerta crítica al SysAdmin: "Caché Offline - Operando en Degradación Segura sin Lista Negra" |

---

## Matriz de Trazabilidad

| Escenario | US Principal | CAs Cubiertos | Prioridad |
|-----------|:-----------:|:------------:|:---------:|
| CU-J05-01 | US-048 | CA-1, CA-2 | MUST |
| CU-J05-02 | US-048 | CA-3 | MUST |
| CU-J05-03 | US-048 | CA-4, CA-6 | MUST |
| CU-J05-04 | US-048 | CA-7 | SHOULD |
| CU-J05-05 | US-036 | CA-6, CA-19 | MUST |
| CU-J05-06 | US-036 | CA-3, CA-19 | MUST |
| CU-J05-07 | US-036 | CA-4 | MUST |
| CU-J05-08 | US-036 | CA-9, CA-23 | MUST |
| CU-J05-09 | US-036 | CA-16, CA-24 | SHOULD |
| CU-J05-10 | US-038 | CA-2 | MUST |
| CU-J05-11 | US-038 | CA-3 | MUST |
| CU-J05-12 | US-038 | CA-4 | MUST |
| CU-J05-13 | US-038 | CA-6 | MUST |
| CU-J05-14 | US-025 | CA-1 | MUST |
| CU-J05-15 | US-025 | CA-2, CA-10 | MUST |
| CU-J05-16 | US-025 | CA-6 | MUST |
| CU-J05-17 | US-025 | CA-7 | SHOULD |
| CU-J05-18 | US-043 | CA-1 | MUST |
| CU-J05-19 | US-043 | CA-6 | MUST |
| CU-J05-20 | US-043 | CA-4 | SHOULD |
| CU-J05-NEG-01 | US-036 | CA-14, CA-21 | MUST |
| CU-J05-NEG-02 | US-025 | CA-5 | MUST |
| CU-J05-NEG-03 | US-038 | CA-8 | MUST |
| CU-J05-NEG-04 | US-038 | CA-1 | MUST |

---

## Resumen de Cobertura

| US | CAs Totales Epic | CAs Cubiertos J-05 | Cobertura |
|----|:-----------------:|:------------------:|:---------:|
| US-048 | 7 | 6 (CA-1,2,3,4,6,7) | 86% |
| US-036 | 25 | 10 (CA-3,4,6,9,14,16,19,21,23,24) | 40% |
| US-038 | 13 | 8 (CA-1,2,3,4,6,7,8,11) | 62% |
| US-025 | 34 | 7 (CA-1,2,5,6,7,10) | 21% |
| US-043 | 6 | 3 (CA-1,4,6) | 50% |

> **Nota:** La US-025 tiene 34 CAs que cubren UX transversal. Los CAs restantes (Skeleton, Breadcrumbs, Empty States, etc.) se validan dentro de los Journeys J-02, J-04 y J-06 donde el operario interactúa directamente con la UI. La US-036 CAs restantes se cubren en J-SEC (seguridad ofensiva) y los Journeys operativos (J-02, J-04).
