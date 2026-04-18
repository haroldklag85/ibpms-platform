# Análisis Funcional y de Entendimiento: US-036

## Historia Analizada
**US-036: Matriz de Control de Acceso Basado en Roles (RBAC)**

---

### 1. Resumen del Entendimiento

La US-036 define el **sistema nervioso central de seguridad y gobernanza de identidades** de la plataforma iBPMS. Es la historia que controla *quién puede hacer qué, sobre qué proceso, y cuándo*. Se materializa principalmente en la **Pantalla 14**, donde el Oficial de Seguridad (CISO) o Super Admin administra usuarios, roles, permisos, delegaciones y cuentas de servicio (M2M).

La historia establece un modelo RBAC híbrido que soporta dos fuentes de identidad:
- **Identidades federadas:** Importadas automáticamente desde Microsoft EntraID (SSO/MFA) para organizaciones con madurez digital.
- **Identidades locales (Fallback):** Creadas directamente en la BD del iBPMS para clientes sin infraestructura SSO.

El modelo es **piramidal con herencia**: un rol superior hereda atómicamente los permisos de un rol inferior, evitando redundancia. Los permisos operan a dos niveles:
- **Estáticos:** Asignados manualmente en Pantalla 14 (de por vida hasta revocación).
- **Dinámicos:** Inyectados en tiempo real por el motor BPMN a través de Expression Lanes de Camunda.

La historia abarca desde el aprovisionamiento automático de nuevos empleados (Ciudadano Interno), pasando por delegación temporal de poderes (vacaciones), hasta la revocación instantánea de sesiones (Kill-Session) y la generación de reportes de cumplimiento ISO 27001.

---

### 2. Objetivo Principal

Dotar a la plataforma de un **módulo centralizado de Identity Governance** que permita al CISO/Super Admin gestionar el ciclo de vida completo de identidades (creación, asignación, delegación, congelamiento, revocación) con trazabilidad indeleble, cumpliendo ISO 27001 y habilitando la segregación de datos (Row-Level Security) para que cada operario vea exclusivamente su propio trabajo.

---

### 3. Alcance Funcional Definido

| Dimensión | Hasta Dónde Llega | Dónde Termina |
|---|---|---|
| **Identidades** | Importación de EntraID + Fallback local (CA-1), auto-provisión para nuevos SSO (CA-8) | No implementa su propio proveedor de identidad OAuth/OIDC ni registro de usuarios públicos |
| **Roles** | Plantillas clonables (CA-3), herencia piramidal (CA-6), Mass Assignment (CA-3) | No implementa roles condicionales por horario ni geo-fencing |
| **Permisos** | Segregación Iniciador vs Ejecutor por proceso BPMN (CA-4), Row-Level Security por usuario (CA-5) | Ocultamiento de campos individuales se delega a US-003/Pantalla 7 (CA-12) |
| **Ciclo de vida** | Soft-Delete con sello Inactivo (CA-7), Kill-Session TCP instantáneo (CA-14) | No incluye flujos de aprobación multi-nivel para otorgar roles (workflow de aprobación) |
| **Delegación** | Delegación temporal con rango de fechas y revocación automática (CA-9) | No soporta "cadenas de delegación" (A delega a B que delega a C) |
| **M2M** | API Keys vinculadas a roles específicos (CA-10) | No incluye rotación automática de API Keys ni scopes OAuth2 granulares |
| **MFA** | Delegación total al Identity Provider EntraID (CA-11) | No implementa MFA propio ni step-up authentication dentro del iBPMS |
| **Auditoría** | Reporte CSV/Excel de Identity Governance (CA-16), traza JSON delta de otorgamiento (CA-17) | No incluye dashboards visuales/gráficos de cumplimiento ni alertas automáticas de anomalías |
| **SoD** | Diferido explícitamente a V2 (CA-18) | No tiene motor de detección de conflictos de interés |
| **Acceso público** | Switch "Permitir Trámite Público" sin JWT (CA-15) | No incluye gestión de identidad anónima ni portal ciudadano |

---

### 4. Lista de Funcionalidades Incluidas

#### A. Identidades y Aprovisionamiento
1. Hibridación de identidades EntraID + Fallback local (CA-1)
2. Usuario Root Super Admin inborrable inyectado Día Cero (CA-2)
3. Auto-provisión de perfil "Ciudadano Interno" para nuevos SSO logins (CA-8)

#### B. Gestión de Roles y Permisos
4. Clonación de perfiles por Rol Plantilla con Mass Assignment (CA-3)
5. Segregación granular Iniciador vs Ejecutor por Proceso BPMN (CA-4)
6. Row-Level Security por usuario en vistas del Workdesk (CA-5)
7. Herencia piramidal de roles (CA-6)
8. Desacoplamiento de Roles Estáticos vs Dinámicos BPMN Lanes (CA-13)

#### C. Ciclo de Vida de Identidades
9. Inmutabilidad por Soft-Delete — sello Usuario Inactivo preservando historial (CA-7)
10. Delegación temporal autónoma con rango de fechas y revocación automática (CA-9)
11. Botón Kill-Session para revocación instantánea de JWT/Redis (CA-14)

#### D. Machine-to-Machine y Acceso Público
12. Creación de Service Accounts con API Keys vinculadas a roles (CA-10)
13. Switch de Trámite Público sin JWT para formularios ciudadanos (CA-15)

#### E. Autenticación Federada
14. Respeto ciego al MFA de EntraID — no duplicar MFA propio (CA-11)

#### F. Restricciones de Alcance
15. Exclusión explícita: ocultar campos individuales NO es responsabilidad de Pantalla 14 (CA-12)

#### G. Auditoría y Cumplimiento
16. Reporte CSV/Excel matrizal de Identity Governance para ISO 27001 (CA-16)
17. Traza JSON delta indeleble de todo otorgamiento de permisos (CA-17)

#### H. Diferimiento Explícito
18. Omisión de SoD automático en V1 — diferido a V2 (CA-18)

---

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas

#### GAP-1: Ausencia de Modelo de Datos para la Matriz RBAC
La historia describe con precisión las reglas de negocio (herencia, segregación, delegación) pero **no especifica la estructura de tablas** que las soporta. No queda claro:
- ¿Existe una tabla `ibpms_roles`, `ibpms_permissions`, `ibpms_role_permissions`, `ibpms_user_roles`?
- ¿La herencia piramidal (CA-6) se resuelve con una columna `parent_role_id` (auto-referencia) o con una tabla de clausura transitiva?
- ¿El Mass Assignment (CA-3) opera sobre una tabla pivote `ibpms_user_roles` con INSERT masivo o es una operación batch diferente?

**Riesgo:** El desarrollador Backend tomará decisiones de diseño de esquema sin directriz, generando divergencia entre lo que el CISO espera y lo que se persiste.

#### GAP-2: Row-Level Security — ¿Implementación a Nivel de BD o de Aplicación?
El CA-5 exige que María "SOLO visualice los folios asignados a ella" mediante un "filtro de base de datos a nivel de registro". Pero no aclara:
- ¿Se usa Row-Level Security nativa de PostgreSQL (`CREATE POLICY`)? Esto requiere pasar el `user_id` al contexto de la conexión.
- ¿O se implementa como un `WHERE assignee = :currentUserId` a nivel de query en el Repository de Spring Boot?

La diferencia es arquitectónicamente significativa: RLS nativo es más seguro (imposible de bypassear accidentalmente), pero añade complejidad al pool de conexiones. El filtro a nivel de aplicación es más simple pero depende de que *todos* los queries pasen por el mismo interceptor.

#### GAP-3: Kill-Session (CA-14) — ¿Cómo se destruyen los JWT en caché?
El CA-14 exige "destruir activamente los JWT almacenados en caché/Redis". Pero:
- Los JWT son **stateless**: no se destruyen, se invalidan. La invalidación requiere una **blacklist** consultable en cada request.
- ¿La blacklist se almacena en Redis con TTL igual al tiempo de vida del JWT?
- ¿El Gateway (API Gateway / Spring Security Filter) consulta Redis en cada request para verificar si el token está revocado?
- ¿Qué pasa si Redis está caído al momento del Kill-Session? (Este escenario se aborda en la US-038 CA-01, pero la US-036 no lo referencia).

**Nota:** La US-038 define una política de "Fail-Open Degradado" (Redis caído), pero la US-036 no hace cross-reference. Esto es un riesgo de **implementación aislada** si los developers de cada US no se coordinan.

#### GAP-4: API Keys (CA-10) — Sin Política de Rotación ni Expiración
El CA-10 permite la creación de "Tokens Criptográficos" para Service Accounts M2M. Pero no define:
- ¿Las API Keys tienen fecha de expiración?
- ¿Existe un mecanismo de rotación (generar nueva key, deprecar la anterior)?
- ¿Qué algoritmo de hashing se usa para almacenarlas (bcrypt, SHA-256)?
- ¿Se audita el uso de API Keys (qué Service Account llamó qué endpoint a qué hora)?

**Riesgo:** Sin policy de expiración, una API Key comprometida opera indefinidamente.

#### GAP-5: Delegación (CA-9) — ¿Qué pasa con las tareas in-flight del delegante?
El CA-9 permite delegar poderes a un suplente con rango de fechas. Pero no especifica:
- ¿El suplente hereda las tareas *ya asignadas* al delegante en la bandeja?
- ¿O solo las tareas *nuevas* que lleguen durante el período de vigencia?
- ¿Al expirar la delegación, las tareas no completadas por el suplente regresan al delegante automáticamente?

**Riesgo:** Si el suplente ve la bandeja vacía porque solo hereda el rol pero no las tareas pre-existentes, el mecanismo es inútil operativamente.

#### GAP-6: Reporte ISO 27001 (CA-16) — ¿Periodicidad y Automatización?
El CA-16 permite "generar el reporte" bajo demanda. Pero un entorno ISO 27001 riguroso podría requerir:
- ¿Se genera solo on-demand (botón en Pantalla 14) o tiene generación programada (cron)?
- ¿Se envía automáticamente por email al CISO?
- ¿Se versiona cada reporte generado para comparar estado de permisos entre periodos?

Para V1, la generación on-demand puede ser suficiente, pero debe documentarse explícitamente como tal.

#### GAP-7: Relación US-036 vs US-038 — Solapamiento Funcional
La US-038 ("Asignación Multi-Rol y Sincronización EntraID") cubre temáticas muy similares a la US-036:
- Ambas hablan de roles, EntraID, JWT y Kill-Session.
- La US-038 amplía con Fail-Open Policy (Redis caído), TTL de 15 minutos y Sudo-Mode.
- El CA-14 de US-036 (Kill-Session) depende del mecanismo de blacklist que la US-038 define con mayor detalle.

**Riesgo de implementación:** Si dos equipos distintos implementan US-036 y US-038 por separado, podrían crear dos módulos RBAC paralelos que colisionen. Se requiere una directriz de que **US-036 define la UI y reglas de negocio (Pantalla 14)** y **US-038 define la infraestructura de seguridad subyacente (JWT, Redis, Sync EntraID)**.

---

### 6. Lista de Exclusiones (Fuera de Alcance)

1. **MFA propio del iBPMS** — Explícitamente delegado a EntraID (CA-11).
2. **Segregación de Funciones (SoD) automática** — Explícitamente diferido a V2 (CA-18).
3. **Ocultamiento de campos por rol** — Explícitamente delegado a US-003/Pantalla 7 (CA-12).
4. **Portal ciudadano completo** — Solo expone un switch de URL pública, no gestión de identidad anónima (CA-15).
5. **Dashboards visuales de cumplimiento** — Solo CSV/Excel, no gráficos interactivos (CA-16).
6. **Multi-tenancy con aislamiento de BD por tenant** — No mencionado.
7. **Flujos de aprobación para otorgamiento de roles** — El Super Admin asigna directamente sin workflow de aprobación.
8. **Rotación automática de API Keys** — No definido (CA-10).
9. **Paso de autenticación escalonada (step-up auth)** — No mencionado; toda la confianza está en el token EntraID inicial.
10. **Roles con vigencia temporal automática** (excepto delegación CA-9) — No hay roles que expiren por fecha.

---

### 7. Observaciones de Alineación o Riesgos para Continuar

> [!WARNING]
> **Riesgo de Fragmentación con US-038.** La US-036 y la US-038 son hermanas gemelas disfrazadas de historias independientes. La US-036 es la "cara" (Pantalla 14, reglas de negocio, UX) y la US-038 es el "motor" (JWT, Redis blacklist, Sync EntraID, gRPC). Si se desarrollan sin coordinación, el Kill-Session (CA-14 de US-036) no tendrá infraestructura porque la blacklist vive en US-038. **Recomendación:** Asignar ambas historias al mismo equipo o al menos al mismo Arquitecto para garantizar coherencia.

> [!IMPORTANT]
> **Dependencias Externas Críticas.** La US-036 irradia hacia:
> - **US-038**: Infraestructura JWT/Redis para Kill-Session y blacklist.
> - **US-003**: Delegación de ocultamiento de campos por rol al IDE (Pantalla 7).
> - **US-001**: El Workdesk (Pantalla 5) consume Row-Level Security definido aquí.
> - **US-029**: Los Smart Buttons respetan permisos RBAC para habilitar/deshabilitar acciones.
> - **US-051**: Vue Router consume `can_initiate_process` para renderizar menú de procesos disponibles.

> [!NOTE]
> **Fortaleza Arquitectónica.** La historia demuestra madurez en la separación de concerns: no mezcla autenticación con autorización, delega MFA al IdP externo, y diferencia roles estáticos vs dinámicos. El modelo de Soft-Delete (CA-7) y la trazabilidad JSON delta (CA-17) son decisiones sólidas para cumplimiento regulatorio.

> [!CAUTION]
> **Row-Level Security (CA-5) es crítico para la privacidad.** Si se implementa como un simple `WHERE` en el Repository sin un interceptor centralizado, existe riesgo de que un nuevo endpoint o un query ad-hoc filtre datos ajenos. Se recomienda evaluar RLS nativo de PostgreSQL o, como mínimo, un `@Aspect` de Spring AOP que fuerce el filtro en todas las queries del Workdesk.
