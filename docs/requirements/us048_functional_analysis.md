# Análisis Funcional: US-048 - Módulo Gestor Propio de Identidades (Internal IdP)

**Ejecutado por:** `[🤖 Agente QA / Analista]` | **Fecha:** 2026-04-22
**Workflow Aplicado:** `/analisisEntendimientoUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (CA-1 al CA-7)

---

## 1. Resumen del Entendimiento
La **US-048** define la creación de un Proveedor de Identidades (IdP) interno dentro del ecosistema iBPMS. Nace como una contingencia y requerimiento principal para organizaciones (clientes) que poseen baja madurez corporativa y no cuentan con herramientas de gestión de identidades centralizadas y federadas (como Microsoft EntraID u Okta). El módulo permite gobernar usuarios locales, contraseñas, roles y expulsiones de emergencia, asegurando los pilares de Zero-Trust a pesar de no contar con un SSO externo.

## 2. Objetivo Principal
Dotar al Súper Administrador del sistema de un panel centralizado (Pantalla 14) capaz de aprovisionar usuarios, forzar políticas de seguridad corporativas en contraseñas locales y gestionar el ciclo de vida de los accesos (creación, asignación de roles dinámicos y revocación absoluta), garantizando que el iBPMS pueda operar "Standalone" sin comprometer la seguridad arquitectónica.

## 3. Alcance Funcional
El alcance técnico abarca **desde el panel administrativo (Frontend) hasta el manejo seguro de credenciales y sesiones en el Backend**:
*   **INICIA:** En el formulario de "Crear Nuevo Usuario" o "Nuevo Rol" de la Pantalla 14 por un Súper Administrador.
*   **TERMINA:** Con el almacenamiento seguro del perfil en BD local (Hashes/Salt) y la respectiva propagación o destrucción de sesiones activas (JWT/Redis) en caso de revocación.
*(Nota: El mecanismo de verificación JWT y la lógica profunda del caché Redis le pertenecen a la US-038, operando esta US como el orquestador visual y de negocio sobre dicha infraestructura).*

## 4. Lista de Funcionalidades Incluidas
La US garantiza la construcción técnica de las siguientes características obligatorias:
*   **Zero-Public-Signup (CA-1):** Prohibición estructural de autorregistro (Register); la creación de identidades nace exclusivamente del administrador.
*   **Validación Zod Enterprise (CA-2):** Forzado de políticas de contraseñas locales fuertes (8+ caracteres, Alfanumérico, Símbolo, Mayúscula) evaluadas tanto en Frontend como Backend.
*   **Destrabe Manual (CA-3):** Botón para generar claves temporales efímeras para resolver olvidos de credenciales, omitiendo el envío de OTP por Email en V1.
*   **Role CRUD (CA-4):** Fábrica dinámica que permite bautizar, modificar y asignar roles transversales mediante interfaces drag-and-drop o selección múltiple.
*   **Muerte Súbita / Kill Switch (CA-5):** Botón "Activo/Inactivo" que ejecuta una aniquilación lógica y física de cualquier sesión vigente (Invalidación JWT vía lista negra).
*   **Asignación Híbrida (CA-6):** Capacidad técnica de vincular 1..N sombreros (roles) lógicos al mismo usuario en la misma sesión.
*   **Mutación Híbrida de Componentes (CA-7):** Regla de interfaz que oscurece y prohíbe tocar contraseñas si detecta que la cuenta proviene del IdP Externo (EntraID).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
Al comparar los CAs contra la consistencia global del sistema, identifico los siguientes vacíos (GAP) para graduar:
*   **GAP-1 (Vulnerabilidad de Exposición en Clave Temporal):** El CA-3 permite "generar una clave temporal visible por única vez para comunicarla verbalmente". Sin embargo, no se especifica si se fuerza al usuario a un "Password Change Required" en su primer login subsiguiente. Si esto no se obliga, la clave dictada por el Admin vivirá para siempre, comprometiendo el principio de Zero-Trust.
*   **GAP-2 (Huérfanos Tras el Kill Switch):** El CA-5 detalla el cierre de la sesión de red. Sin embargo, no se pronuncia sobre el "Unclaim" de las tareas vivas en Camunda (Esto lo hace la US-038 CA-08 para despidos, pero debe garantizarse que el botón inactivo dispare este mismo evento asíncrono hacia el motor BPMN para evitar que los folios queden retenidos).
*   **GAP-3 (Protección Anti-Bloqueo del Súper Admin):** No hay una restricción explícita que impida que el Súper Admin se desactive a sí mismo (Kill Switch sobre su propio registro), pudiendo generar un "Deadlock" donde nadie más pueda administrar la plataforma.

## 6. Lista de Exclusiones (Fuera de Alcance)
*   **Recuperación Self-Service Automática (OTP/Email):** Los flujos transaccionales de "Olvidé mi contraseña" enviados por correo electrónico quedan estrictamente diferidos a V2 (según CA-3 y US-038 CA-13).
*   **Portales Ciudadanos de Registro:** Formularios públicos o auto-onboarding (Fuera de alcance por CA-1 y complementado en US-050).
*   **Manejo de Grupos EntraID:** Esta historia gobierna los roles locales. La sincronización masiva y el ingestion pipeline de grupos AD pertenece a la US-038.

## 7. Observaciones de Alineación o Riesgos
### Clasificación MoSCoW
*   **Must Have:** Crítico. Sin la US-048, el sistema es inaccesible para despliegues Standalone (sin Azure AD) impidiendo las pruebas UAT y el inicio de operaciones del 50% de los clientes previstos.

### Resumen de Dependencias con otras User Stories
*   **Dependencia Estricta con US-038 (Infraestructura de Auth):** El botón Kill Switch (CA-5) asume que la US-038 ya construyó el "Blacklist" en Redis y la lógica interceptora del Spring Security Filter. La US-048 depende de ese Endpoint.
*   **Dependencia Core con US-036 (RBAC):** La Fábrica de Roles (CA-4) de esta historia se interrelaciona directamente con la "Matriz de Procesos BPMN" de la US-036. Esta US (048) permite "bautizar" el rol, pero es la 036 la que le da "poderes sobre los procesos". Deben desarrollarse simultáneamente.

### Dependencia Bloqueante Absoluta (Riesgo Técnico)
*   Para que el **CA-7 (Mutación Híbrida de Interfaz)** funcione, la tabla de usuarios en BD (`ibpms_users`) debe haber sido estructurada con una columna inmutable de origen (ej. `source: ENUM(LOCAL, ENTRAID)`). Si el script Liquibase inicial no contempla esta división, el Frontend nunca sabrá cuándo oscurecer el campo de contraseña, induciendo a errores de escritura.
