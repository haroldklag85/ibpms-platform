# 🔧 Handoff Backend — BUG-J02-006 (Menú vacío para ROLE_USER_INTERNAL)

> **Iteración**: Sprint 01-devDavid  
> **US**: BUG01-JORNEY  
> **Bug**: BUG-J02-006  
> **Rama de Trabajo**: `DevDavid` (**OBLIGATORIO** — prohibido trabajar en `main`)  
> **Orden de ejecución**: 🥇 **PASO 1 de 2** (este handoff va primero)  
> **SSOT**: `docs/qa/INFORME_TECNICO_QA_J02_PM01.md` → Línea 238  
> **Arquitecto Líder**: Agente Orquestador (chat principal)

---

## Pre-Handoff Checklist — BUG-J02-006

| # | Verificación | Estado | Evidencia |
|---|---|---|---|
| 1 | US en sprint actual (Roadmap PM-IA) | ✅ | Sprint PM-01, Cadena 4 (BPMN E2E) — Bugs de certificación J-02 |
| 2 | Endpoints definidos en API_CONTRACTS.md | ✅ | `GET /api/v1/users/me/menu-layout` — Sección 5.6 (⚠️ Assumed, comportamiento verificado para SUPER_ADMIN) |
| 3 | Prerrequisitos completados | ✅ | US-036, US-038, US-048 completadas (Cadena 1 Seguridad) |
| 4 | Matriz de cobertura actualizada | ✅ | Última auditoría: INFORME_TECNICO_QA_J02_PM01.md |

**Resultado**: ✅ APROBADO para handoff

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Bug ID** | BUG-J02-006 |
| **Tipo** | Bug Funcional |
| **Severidad** | P2 (Media) |
| **Misión UAT Origen** | M7 — RBAC Security by Obscurity |
| **Descripción** | El menú lateral para el usuario con rol `ROLE_USER_INTERNAL` (`operario_c@alpha.com`) aparece completamente vacío con mensaje "Sin Topología de Menús", a pesar de que Harold confirma que este rol **SÍ debería tener items de menú** asignados. |
| **Endpoint Afectado** | `GET /api/v1/users/me/menu-layout` |
| **Credenciales de prueba** | `root@ibpms.local` / `Root#Temp4Sys` |
| **Rama Git** | `DevDavid` |

---

## 2. Alineación Arquitectónica y ADRs

| ADR | Impacto |
|-----|---------|
| **ADR-001** (Hexagonal) | El servicio `MenuLayoutService` está en `application/service/ui/` (capa application). Su dependencia directa de `infrastructure/jpa/entity/` (UserEntity, RoleEntity, PermissionEntity) es deuda técnica existente. **NO empeores esta violación.** |
| **ADR-009** (PostgreSQL) | Los datos de permisos viven en tablas `ibpms_security_permission`, `ibpms_security_role`, `ibpms_security_role_permissions`. Todo cambio debe ser via Liquibase. **PROHIBIDO SQL directo.** |

**Stack confirmado**: Java 17 / Spring Boot / PostgreSQL / JPA / Liquibase. Sin violaciones.

---

## 3. Causa Raíz Verificada — Rutas Exactas y Contexto Preexistente

### 3.1 El Problema: `ROLE_USER_INTERNAL` se crea SIN permisos

**Archivo**: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/startup/DataSeeder.java` (líneas 52-56)

```java
// CA-08: Seed ROLE_USER_INTERNAL
if (roleRepository.findByName("ROLE_USER_INTERNAL").isEmpty()) {
    roleRepository.save(new RoleEntity("ROLE_USER_INTERNAL", "Ciudadano Interno - SSO Default"));
    log.info("====== ROLE_USER_INTERNAL SEED COMPLETED ======");
}
```

**Problema**: El rol se crea como un `RoleEntity` vacío. No se le asignan `PermissionEntity` ni se vincula a ningún módulo.

### 3.2 La Lógica de Topología que falla

**Archivo**: `backend/ibpms-core/src/main/java/com/ibpms/poc/application/service/ui/MenuLayoutService.java` (líneas 32-62)

```java
public Set<String> computeTopologyForUser(String username) {
    UserEntity user = userRepository.findByUsername(username).orElseThrow(...);

    // Early Return: SUPER_ADMIN y SYSTEM_ADMIN → retorna TODOS los módulos ✅
    boolean isNativeAdmin = user.getRoles().stream()
        .anyMatch(role -> "SUPER_ADMIN".equalsIgnoreCase(role.getName()) || ...);
    if (isNativeAdmin) {
        return Collections.unmodifiableSet(new HashSet<>(MACRO_MODULES));
    }

    // Para OTROS roles: busca en permissions
    Set<String> activeMenus = new HashSet<>();
    for (RoleEntity role : user.getRoles()) {
        if (role.getPermissions() != null) {
            for (PermissionEntity permission : role.getPermissions()) {
                String permName = permission.getName().toUpperCase();
                for (String module : MACRO_MODULES) {
                    if (permName.contains(module)) {  // ← AQUÍ: busca "WORKDESK" dentro del nombre del permiso
                        activeMenus.add(module);
                    }
                }
            }
        }
    }
    return activeMenus;  // ← Retorna VACÍO porque no hay permisos
}
```

**Módulos macro definidos** (hardcoded en línea 20-22):
```java
private static final List<String> MACRO_MODULES = List.of(
    "WORKDESK", "SERVICE_DELIVERY", "BAM", "MODELER", "INTEGRATION", "PROJECTS", "ADMINISTRATION"
);
```

### 3.3 El Controller que consume el servicio

**Archivo**: `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/ui/MenuLayoutController.java` (líneas 32-116)

El controller construye el menú en base al `Set<String>` de módulos activos. Si el set está vacío, el menú está vacío.

### 3.4 Entidades JPA Relevantes

| Entidad | Tabla | Archivo |
|---------|-------|---------|
| `PermissionEntity` | `ibpms_security_permission` | `backend/.../entity/security/PermissionEntity.java` |
| `RoleEntity` | `ibpms_security_role` | `backend/.../entity/security/RoleEntity.java` |
| Join Table | `ibpms_security_role_permissions` | Definida en `RoleEntity.java` línea 28-32 |
| `UserEntity` | — | `backend/.../entity/security/UserEntity.java` |

### 3.5 Cache Redis

`MenuLayoutService` usa `@Cacheable(value = "menuTopology", key = "#username")`. **Después de aplicar el fix, DEBES invalidar el caché** para los usuarios afectados o el menú seguirá vacío.

---

## 4. Tarea Prescriptiva — Investigación y Solución

### Paso 1: Investigar la BD (OBLIGATORIO antes de codificar)

Ejecuta estas queries contra PostgreSQL (puerto `5433` del Docker `ibpms-postgres-uat`) para entender el estado actual de los datos:

```sql
-- 1. ¿Qué roles existen?
SELECT id, name, description FROM ibpms_security_role ORDER BY name;

-- 2. ¿Qué permisos existen en el sistema?
SELECT id, name, description FROM ibpms_security_permission ORDER BY name;

-- 3. ¿Qué permisos tiene ROLE_USER_INTERNAL?
SELECT r.name AS role_name, p.name AS permission_name 
FROM ibpms_security_role r 
LEFT JOIN ibpms_security_role_permissions rp ON r.id = rp.role_id 
LEFT JOIN ibpms_security_permission p ON rp.permission_id = p.id 
WHERE r.name = 'ROLE_USER_INTERNAL';

-- 4. ¿Qué permisos tiene ROLE_SUPER_ADMIN? (para comparar)
SELECT r.name AS role_name, p.name AS permission_name 
FROM ibpms_security_role r 
LEFT JOIN ibpms_security_role_permissions rp ON r.id = rp.role_id 
LEFT JOIN ibpms_security_permission p ON rp.permission_id = p.id 
WHERE r.name = 'ROLE_SUPER_ADMIN';

-- 5. ¿Qué usuarios tienen ROLE_USER_INTERNAL?
SELECT u.username, u.email, r.name AS role_name
FROM ibpms_security_user u
JOIN ibpms_security_user_roles ur ON u.id = ur.user_id
JOIN ibpms_security_role r ON ur.role_id = r.id
WHERE r.name = 'ROLE_USER_INTERNAL';
```

**Documenta los resultados en tu `approval_request_BACKEND.md`** antes de proponer la solución.

### Paso 2: Diseñar una solución escalable, personalizable y SIN hard-code

> ⚠️ **PROHIBICIÓN ABSOLUTA DE HARD-CODE**: Queda estrictamente prohibido agregar mapeos estáticos de permisos a módulos dentro del código Java (no `Map.of("VIEW_TASKS", Set.of("WORKDESK"))` ni similares). La solución DEBE ser configurable desde la BD.

**Opciones a evaluar** (el agente Backend debe elegir la más adecuada tras la investigación):

**Opción A — Crear permisos con convención de nombres y asignarlos al rol**:
- Crear los permisos necesarios en la tabla `ibpms_security_permission` con nombres que contengan el macro módulo (ej. `WORKDESK_VIEW`, `WORKDESK_ACCESS`).
- Asignar esos permisos a `ROLE_USER_INTERNAL` via `ibpms_security_role_permissions`.
- El fix es un **changeset Liquibase** de datos (INSERT), no código Java.
- La lógica de `MenuLayoutService.computeTopologyForUser()` ya funciona con esta convención.

**Opción B — Crear tabla de mapeo `ibpms_module_permission_mapping`**:
- Crear una tabla relacional que mapee `permission_name` → `module_name`.
- Modificar `MenuLayoutService` para consultar esta tabla en lugar de hacer `permName.contains(module)`.
- Más escalable y desacoplada, pero requiere más cambios.

**Opción C — Añadir campo `module` a `PermissionEntity`**:
- Agregar una columna `module VARCHAR(50)` a `ibpms_security_permission`.
- Modificar `MenuLayoutService` para agrupar por `permission.getModule()`.
- Requiere migration Liquibase + cambio en la entidad.

**Criterios de evaluación**:
- ¿Cuál impacta menos código existente?
- ¿Cuál es más escalable para cuando se agreguen nuevos módulos o permisos?
- ¿Cuál NO introduce hard-code en Java?
- ¿Cuál respeta ADR-001 (Hexagonal)?

### Paso 3: Implementar la solución elegida

Sea cual sea la opción:
1. **Changeset Liquibase** para cualquier cambio de datos o esquema.
2. **Tests de integración** para verificar que `computeTopologyForUser("operario_c@alpha.com")` retorna módulos no vacíos.
3. **Invalidar caché** Redis después del fix.
4. **NO dañar** la funcionalidad existente de SUPER_ADMIN (que ya funciona correctamente).

### Paso 4: Verificación empírica

Después de aplicar el fix:
1. Arrancar Spring Boot: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`
2. Login con un usuario que tenga `ROLE_USER_INTERNAL`.
3. Llamar `GET /api/v1/users/me/menu-layout` → Debe retornar al menos 1 grupo con items.
4. Documentar la respuesta JSON como evidencia.

---

## 5. Matriz de QA (Validación Cruzada)

| Test | Bug Evaluado | Aserción Esperada |
|------|-------------|-------------------|
| `computeTopologyForUser retorna módulos para ROLE_USER_INTERNAL` | BUG-J02-006 | Set de módulos NO está vacío para usuario con ROLE_USER_INTERNAL |
| `computeTopologyForUser sigue retornando todos los módulos para SUPER_ADMIN` | Regresión | Set contiene los 7 MACRO_MODULES |
| `GET /api/v1/users/me/menu-layout retorna groups para ROLE_USER_INTERNAL` | BUG-J02-006 | Response HTTP 200 con array no vacío |
| `Caché se invalida tras cambio de permisos` | BUG-J02-006 | Segundo request refleja nuevos permisos |

---

## 6. Directivas Operativas

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> **NFR/QA Strategy:** Desarrollar sobre la arquitectura en `docs/architecture/arquitecturar.md`. Preservar la arquitectura hexagonal del proyecto. No alucinar, no imaginar, no salir del contexto dado. Referenciar BUG-J02-006 en toda documentación de la solución.

---

## INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_BACKEND.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_BACKEND.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama `DevDavid`. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).

> 🧠 **POLÍTICA ANTIAMNESIA:** Antes de codificar, LEE OBLIGATORIAMENTE:
> 1. `docs/architecture/arquitecturar.md` — Arquitectura Core
> 2. `docs/qa/INFORME_TECNICO_QA_J02_PM01.md` — Descripción exacta del bug (línea 238)
> 3. `docs/sprints/gobernanza_pm/GUIA_ARQUITECTO_LIDER.md` — Directrices estratégicas
