# Reporte de Auditoría US-036: Identity Governance & RBAC Architecture

## 📌 Contexto
- **Épica:** E — Seguridad, RBAC, Identidad & Configuración Global
- **User Story:** US-036 (Matriz de Control de Acceso Basado en Roles)
- **Foco de esta iteración:** CA-23 (Comportamiento de Delegación sobre Tareas In-Flight)

## 🔍 Ejecución de FASE 2 y 3: Navegación y Validación

### CA-11: Respeto ciego al Autenticador Perimetral (EntraID MFA)
**Requisito:** Según el índice de requerimientos, la arquitectura de seguridad V1 asume 100% de confianza en el Token emitido por Microsoft EntraID, prohibiendo explícitamente reconstruir o duplicar un componente de Doble Factor (MFA) propio en el iBPMS, delegando esta validación criptográfica al Identity Provider original.

**Validación Estructural (Top-Down):**
- **Exploración Frontend:** Se revisó la pantalla de autenticación principal (`Login.vue`).
- **Exploración Backend:** Se analizó el interceptor de seguridad canónico (`JwtAuthFilter.java`).
- **Hallazgos:**
  - ✅ **Frontend (Cero MFA):** La UI expone únicamente el flujo de SSO hacia EntraID (vía redirección) y un flujo de emergencia ("Break-Glass") de un solo factor (usuario y contraseña de bóveda). No existe ningún componente, input o modal de recolección de códigos OTP o factores secundarios.
  - ✅ **Backend (Respeto al JWT):** El filtro `JwtAuthFilter` se limita a validar la firma del token (`jwtTokenProvider.isValid(token)`) y establece el contexto de seguridad. No se exige la verificación de `amr` (Authentication Methods References) ni se obliga a un doble paso de autenticación.
  - ❌ **Corrección de Matriz:** La matriz de cobertura reportaba previamente una nota errónea (`SecurityAnomalyListener @Async REQUIRES_NEW, falta detección MFA`), originada por un comentario ambiguo ("CA-11: Desacoplamiento de Rollback") en `SecurityAnomalyListener`. El código fue limpiado y la matriz corregida para reflejar un cumplimiento al 100% de la directriz arquitectónica sin falsos positivos.
- **Estado de Cumplimiento:** ✅ Cumplido.

### CA-23: Comportamiento de Delegación sobre Tareas In-Flight
**Requisito:** Según el índice de requerimientos `docs/requirements/epics/epic_E_seguridad_identidad_config.md`, cuando un Gerente activa una delegación temporal a un suplente:
1. El suplente hereda el rol delegado y las tareas ya asignadas al delegante (in-flight).
2. Las tareas nuevas se enrutan al suplente.
3. Al expirar la delegación, las tareas no completadas regresan automáticamente a la bandeja del delegante original.
4. Toda transferencia queda registrada en la bitácora de auditoría.

**Validación Estructural (Top-Down):**
- **Búsqueda y Mapeo Backend:** Se analizó el archivo `TaskDelegationService.java`.
- **Hallazgos:**
  - Se encontró el método `evaluateAndRevertTaskIfNeeded` que contiene un cascarón lógico para revisar la expiración de la delegación y revertir la asignación al aire (On-the-fly).
  - Sin embargo, la operación de reversión de la base de datos está comentada: `// taskRepository.revertAssignee(taskId, originalOwner);`
  - Peor aún, `TaskDelegationService` ni siquiera inyecta el `TaskRepository` o `WorkdeskRepository` necesario para realizar esta actualización, lo que impide que el código sea funcional.
  - La auditoría en `ibpms_audit_log` estipulada por el CA-23 tampoco se realiza, existiendo solo un `log.warn(...)`.
- **Estado de Cumplimiento:** ⚠️ Parcial / ❌ Ausente en su núcleo de mutación de base de datos. (Brecha de Implementación Confirmada).

## 🏷️ FASE 4: Inyección de Trazabilidad
- Se inyectó el marcador `// @Traceability: US-036 - CA-23` en la firma del método `evaluateAndRevertTaskIfNeeded` dentro de `TaskDelegationService.java` para rastrear la lógica condicional que sí existe.
- Sin embargo, la resolución técnica de la mutación de la base de datos sigue ausente.

## 🚨 Brechas de Implementación y Violaciones de Arquitectura
- **Brecha Funcional CA-23:** La funcionalidad de retornar tareas "In-Flight" no está conectada a la base de datos.
- **Defecto de Diseño CA-23:** `TaskDelegationService` requiere inyección de dependencias (`TaskRepository` / `WorkdeskRepository`) e implementar la consulta JPA / JDBC pertinente para revertir la tarea.
- **Deuda Técnica CA-23:** Falta la inyección en `ibpms_audit_log`.

### CA-24: Alcance Explícito del Reporte ISO 27001 en V1
**Requisito:** Según el índice de requerimientos, el reporte de Identity Governance debe:
1. Generarse exclusivamente bajo demanda mediante un botón en la Pantalla 14 (sin cron).
2. Incluir fecha UTC, actor y un hash SHA-256 del contenido.
3. Guardar el registro histórico en la tabla `ibpms_audit_reports` para comparaciones de periodos.
4. Diferir la generación automática a V2.

**Validación Estructural (Top-Down):**
- **Exploración Backend:** Se navegó el directorio web usando `list_dir` y se localizó el controlador `Iso27001ReportController.java`.
- **Exploración Frontend:** Se navegó el directorio de vistas administrativas y se ubicó `RbacManagerView.vue`, donde reside el botón disparador.
- **Hallazgos:**
  - El backend cumple con prescindir de un cron, usa SHA-256 e imprime al log la traza de auditoría.
  - ❌ **Falla de Persistencia:** En `Iso27001ReportController`, existe un comentario explícito (`// En un entorno de producción, insertamos en ibpms_audit_reports`) pero el código que inserta a la base de datos está omitido (Brecha).
  - ❌ **Falla de Integración Front-Back (Bug 405):** El frontend (`RbacManagerView.vue`) ejecuta un `apiClient.post` hacia la URL de generación, pero el backend recibe dicha solicitud con un `@GetMapping("/generate")`. Esto resulta inevitablemente en un HTTP 405 Method Not Allowed.
- **Estado de Cumplimiento:** ⚠️ Parcial / ❌ Bug de Integración (Falso positivo en Matriz).

### CA-25: Directriz de Coordinación US-036 vs US-038
**Requisito:** La directriz prohíbe explícitamente que la US-036 construya su propia infraestructura paralela de invalidación de tokens (Blacklisting), estableciendo a la US-038 como la única dueña canónica (usando JPA/Redis verdadero).

**Validación Estructural (Top-Down):**
- **Exploración Backend:** Se inspeccionaron los paquetes `infrastructure/security` y `application/service`.
- **Hallazgos:**
  - ❌ **Violación de Responsabilidad Compartida:** Se encontraron las clases `JwtBlacklistService.java` y `JwtSecurityFilter.java` creadas por la US-036, ambas utilizando mapas en memoria (`ConcurrentHashMap` y `HashSet` simulados). Al mismo tiempo, coexiste el filtro canónico `JwtAuthFilter.java` (US-038) que valida tokens contra una base de datos real (`tokenBlacklistRepository`).
  - La presencia dual de filtros de token en el pipeline de Spring Security introduce duplicación, fragmentación de la verdad y sobrecarga innecesaria en la red.
- **Estado de Cumplimiento:** ❌ Ausente (Violación de Directriz Arquitectónica).

### CA-26: Experiencia de Caída Segura (UX Fallback)
**Requisito:** Si un usuario inicia sesión pero no posee menús activos debido a su rol, el sistema debe rutearlo a una página de bienvenida neutra, evitando bloqueos, errores de renderizado o menús fantasma vacíos.

**Validación Estructural (Top-Down):**
- **Exploración Frontend:** Se revisaron las capas de Router (`index.ts`, `RouteGuards.ts`), Stores (`useMenuStore.ts`) y Layouts (`MainLayout.vue`, `Portal.vue`).
- **Hallazgos:**
  - ✅ **Fallback Detectado:** `MainLayout.vue` cuenta con una estructura explícita `v-else-if="menuStore.layout.length === 0"` que muestra de forma elegante el mensaje de "Sin Topología de Menús" si no hay módulos habilitados.
  - ✅ **Ruteo Neutro Activo:** Al no poder acceder a los componentes, el enrutador delega el renderizado central al `Portal.vue`, el cual actúa como un Dashboard de "Bienvenida" donde las secciones adicionales solo se pintan si se poseen roles específicos. El usuario nunca queda atrapado en pantallas de error técnico o con navegación rota.
- **Estado de Cumplimiento:** ✅ Cumplimiento Total.

### CA-27: Inmutabilidad de Roles Nativos del Sistema
**Requisito:** Según el índice de requerimientos, cuando el CISO intenta editar los permisos de menú de un rol fundacional (ej. `SUPER_ADMIN`), la interfaz de selección de módulos (checkboxes) estará bloqueada (Read-Only/Disabled), garantizando acceso total.

**Validación Estructural (Top-Down):**
- **Exploración Frontend:** Se revisaron las vistas en `src/views/admin/RbacManager` (`RbacManagerView.vue`, `RbacTabs.vue`, `GlobalRolesTable.vue`, `ProcessRolesTable.vue`) y el almacenamiento en `rbacStore.js`.
- **Hallazgos:**
  - ❌ **Ausencia de Interfaz (Deuda Visual):** Aunque existe un botón de "Editar" en `GlobalRolesTable.vue` que se oculta inteligentemente para el rol `ROLE_SUPER_ADMIN` (cumpliendo un mecanismo de defensa), el componente para editar la "selección de módulos (checkboxes)" requerido explícitamente en la historia no ha sido desarrollado en absoluto. El botón de edición es actualmente un cascarón sin evento `@click`.
  - En consecuencia, el mecanismo de asignación de menús a roles todavía no se ha integrado, lo que impide asegurar el comportamiento exacto de solo-lectura sobre los checkboxes de los roles inmutables.
- **Estado de Cumplimiento:** ❌ Pendiente (Deuda Funcional Frontend).

### CA-28: Granularidad Macro de la Topología Visual
**Requisito:** Según el índice de requerimientos, la interfaz para editar la topología visual de los roles debe operar estrictamente a nivel de los 7 Módulos Macro principales (Workdesk, Service Delivery, BAM, Modeler, Integración, Proyectos, Administración), evitando la microgestión (sin submenús granulares).

**Validación Estructural (Top-Down):**
- **Exploración Frontend:** Se continuó la revisión en la vista de configuración RBAC (`GlobalRolesTable.vue`, `RbacManagerView.vue`, `RbacTabs.vue`).
- **Hallazgos:**
  - ❌ **Ausencia de Interfaz (Deuda Visual):** Acorde al diagnóstico del CA-27, la interfaz visual de "Topología de Menús" no ha sido desarrollada. No existe un panel, switches, ni checkboxes que listen los 7 Módulos Macro requeridos para habilitar o deshabilitar secciones de la UI por rol.
- **Estado de Cumplimiento:** ❌ Pendiente (Deuda Funcional Frontend).

### CA-29: Diseño Limpio del Modal de Roles (Tablas/Tabs)
**Requisito:** Según el índice de requerimientos, la Pantalla 14 (donde el CISO forja o edita un nuevo rol) debe implementar un Modal dividido en Pestañas (Tabs) para no saturar verticalmente la UI. Debe existir un "Tab 1: Información Básica" y un "Tab 2: Topología de Menús".

**Validación Estructural (Top-Down):**
- **Exploración Frontend:** Se revisó la estructura del modal de creación/edición de roles que reside en `GlobalRolesTable.vue` (desde la línea 104 en adelante). Adicionalmente, se cotejó con la cobertura reportada en `coverage_matrix.md` que señalaba a `RbacTabs.vue`.
- **Hallazgos:**
  - ❌ **Falso Positivo:** La matriz de cobertura adjudicaba un avance parcial (⚠️) sustentado en la existencia de `RbacTabs.vue`. Tras la revisión top-down, se confirma que esto es un error conceptual: `RbacTabs.vue` provee la tabulación de la *pantalla principal* de gestión (Roles Globales, Roles de Procesos, Anomalías), **no** la del *Modal de Roles*.
  - ❌ **Ausencia Estructural:** El verdadero modal inserto en `GlobalRolesTable.vue` es un formulario `flat` vertical simple, el cual no cuenta con divisiones en Tabs. Faltan estructuralmente "Tab 1: Información Básica" y "Tab 2: Topología de Menús".
- **Estado de Cumplimiento:** ❌ Ausente (Falso Positivo Corregido).

### CA-30: Superposición Inclusiva Multirrol (Unión Matemática)
**Requisito:** Según la épica (donde figuraba con un error tipográfico como CA-20 entre el CA-29 y CA-31), cuando un usuario tiene múltiples roles asignados, el Backend debe calcular los menús que puede ver aplicando una "unión matemática inclusiva de los permisos de ambos roles", garantizando que entregue un listado unificado sin colisiones al Frontend.

**Validación Estructural (Top-Down):**
- **Exploración Backend:** Se buscó en el portafolio de servicios de seguridad (`UserService`, `RoleService`, `RbacAuthorizationService`) y controladores (`UserController`, `AuthBffController`) la existencia de lógica de mapeo de menús o layouts visuales.
- **Hallazgos:**
  - ❌ **Falta de Base de Datos y Lógica (Deuda Backend):** El Backend carece absolutamente de soporte de datos para ligar Módulos o Menús UI (`MenuLayout`) con la entidad `RoleEntity`. Al no existir la estructura que determine qué menú corresponde a qué rol, la funcionalidad de superposición inclusiva (Unión Matemática) para consolidar los permisos es inexistente. `RbacAuthorizationService` unifica (usa `distinct`) carriles BPMN, pero omite por completo los menús.
- **Estado de Cumplimiento:** ❌ Pendiente (Criterio Omitido Previamente / Deuda Estructural Backend).

## 🏷️ FASE 4: Inyección de Trazabilidad
- Se inyectó el marcador `<!-- @Traceability: US-036 - CA-11 -->` en el evento de inicio de sesión federado dentro de `Login.vue`.
- Se inyectó el marcador `// @Traceability: US-036 - CA-11` en el bloque de validación criptográfica de `JwtAuthFilter.java`.
- Se inyectó el marcador `// @Traceability: US-036 - CA-23` en `TaskDelegationService.java`.
- Se inyectó el marcador `// @Traceability: US-036 - CA-24` en `Iso27001ReportController.java`.
- Se inyectó el marcador `<!-- @Traceability: US-036 - CA-24 -->` en el botón de generación de reporte dentro de `RbacManagerView.vue`.
- Se inyectó el marcador `// @Traceability: US-036 - CA-25` en `JwtBlacklistService.java` y `JwtSecurityFilter.java` documentando los artefactos de la deuda.
- Se inyectó el marcador `<!-- @Traceability: US-036 - CA-26 -->` en el Fallback Visual dentro de `MainLayout.vue`.

## 🚨 Conclusiones de Deuda Arquitectónica Combinada
- **CA-23:** Las delegaciones de tareas temporales operan parcialmente y carecen de base de datos para la reversión real.
- **CA-24:** El botón de reporte ISO 27001 del Dashboard Administrativo fallará al clickearse (GET vs POST), y si se soluciona, no guardará el registro histórico para auditorías de cumplimiento.
- **CA-25:** El pipeline de seguridad se encuentra contaminado con filtros Dummy duplicados (`JwtSecurityFilter` y `JwtBlacklistService`) que fueron expresamente prohibidos por la arquitectura. Deben ser deprecados y su lógica (de ser necesaria) movida a `JwtAuthFilter`.
- **CA-27:** El UI de asignación de menús por rol es inexistente; la interfaz de checkboxes para proteger la inmutabilidad de los roles fundacionales debe construirse desde cero.
- **CA-28:** El panel de control UI para habilitar/deshabilitar los 7 módulos macro principales de la aplicación por rol no ha sido desarrollado en el Frontend.
- **CA-29:** Existe un falso positivo en QA. El "Modal de Roles" no tiene Pestañas (Tabs) como requiere la UX; es un formulario plano. Se requiere rediseñar el modal para incluir el "Tab 1: Información Básica" y el "Tab 2: Topología de Menús".
- **CA-30:** El Backend no cuenta con las entidades de base de datos para mapear "Menús/Módulos" a "Roles", impidiendo totalmente la ejecución de la "unión matemática inclusiva multirrol" de permisos de menú. Este CA había sido ignorado anteriormente por un error tipográfico en la documentación (estaba marcado como CA-20).

## 📝 Conclusión de Iteración
Se ha completado la inspección estructurada para el CA-11, CA-23, CA-24, CA-25, CA-26, CA-27, CA-28, CA-29 y CA-30. La Matriz de Cobertura ha sido actualizada para exponer las brechas (incluyendo el rescate de un CA omitido por error de tipeo y la corrección de un falso positivo sobre MFA) y el documento central de tareas `task.md` posee las asignaciones críticas de limpieza arquitectónica pendientes para el desarrollador.
