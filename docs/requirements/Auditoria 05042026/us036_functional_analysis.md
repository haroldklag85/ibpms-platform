# Análisis Funcional: US-036 - Matriz de Control de Acceso Basado en Roles (RBAC)

**Ejecutado por:** `[🤖 Agente QA / Analista]` | **Fecha:** 2026-04-22
**Workflow Aplicado:** `/analisisEntendimientoUs.md`
**Fuente de Verdad (SSOT):** `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (CA-1 al CA-18 y Remediaciones CA-19 al CA-25)

---

## 1. Resumen del Entendimiento
La **US-036** es la columna vertebral de la seguridad transaccional y la gobernanza del iBPMS. Define la arquitectura de Control de Acceso Basado en Roles (RBAC) gestionada desde la Pantalla 14. Esta historia establece cómo los permisos se engranan con el motor de procesos Camunda, garantizando que el diseño de privilegios soporte auditorías rigurosas (ISO 27001), segregación de funciones, delegación estructurada, revocación inmediata y privacidad de colas (Row-Level Security), operando tanto para humanos como para integraciones máquina a máquina (M2M).

## 2. Objetivo Principal
Proveer al Súper Administrador y al CISO de un módulo central y auditable capaz de gobernar todos los poderes de la plataforma, desde la visibilidad individual de tareas hasta el derecho a instanciar nuevos expedientes. Su fin último es garantizar un ecosistema "Zero-Trust", asegurando inmutabilidad en las asignaciones de permisos y bloqueos exactos para prevenir accesos no autorizados a datos sensibles.

## 3. Alcance Funcional
El alcance técnico abarca **la lógica de negocio RBAC, sus interfaces de administración y el diseño de base de datos relacional para permisos**:
*   **INICIA:** En el mapeo y sincronización de perfiles (Locales o EntraID) desde la Pantalla 14, pasando por la creación de matrices de acceso (Procesos vs. Acciones).
*   **TERMINA:** Con la aplicación efectiva de estos poderes en tiempo de ejecución (interceptando consultas en el backend y forzando bloqueos transaccionales) y el registro inmutable en la bitácora del CISO.
*(Nota: Comparte responsabilidades con la US-038, la cual provee la infraestructura del token JWT y Redis).*

## 4. Lista de Funcionalidades Incluidas
La US garantiza la construcción técnica de las siguientes características obligatorias:
*   **Gestión Híbrida de Identidades (CA-1 y CA-8):** Ingreso dual de Roles EntraID y roles puramente Locales, incluyendo el perfil por defecto `[Ciudadano_Interno]`.
*   **Súper Admin Inborrable (CA-2):** Presencia mandatoria de un root admin.
*   **Fábrica, Clonación y Herencia (CA-3 y CA-6):** Construcción de Roles Plantilla y herencia piramidal atómica para reducir mantenimiento de matrices.
*   **Matriz Iniciador vs Ejecutor (CA-4):** Distinción matemática entre `can_initiate_process` y `can_execute_tasks` vinculada a cada BPMN.
*   **Privacidad de Colas - RLS (CA-5 y CA-20):** Ocultamiento estricto a nivel de fila (interceptores en Backend) para que un analista solo vea lo que se le ha asignado o lo que pertenece a su grupo.
*   **Inmutabilidad y CISO Audit (CA-7 y CA-17):** Soft-Delete forzoso para conservar historia, y trazas indelebles (Delta JSON) ante cualquier elevación de privilegios.
*   **Delegaciones Temporales (CA-9 y CA-23):** Panel de autogestión para cesión de autoridad durante vacaciones, incluyendo el enrutamiento de tareas In-Flight a suplentes.
*   **Cuentas M2M API Keys (CA-10 y CA-22):** Generación de "Service Accounts" con expiración estricta y limitación de tokens.
*   **Integración y Prevención de Riesgos (CA-11 a CA-18):** Respeto a MFA externo, botón de Exorcismo/Kill-Session, Bypass Anónimo y reportes matriciales.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
Durante el análisis y refinamiento técnico, se detectaron los siguientes vacíos (GAP) que ya han sido canalizados a Remediaciones (CA-19 a CA-25):
*   **GAP-1 (Falta de Estructura SQL):** Originalmente no se definía el modelo relacional. *Remediado en CA-19 detallando tablas y recursive CTE.*
*   **GAP-2 (Fuga de Datos en Workdesk):** Riesgo de que endpoints omitan el `assignee_id`. *Remediado en CA-20 exigiendo un interceptor global AOP.*
*   **GAP-3 (API Keys sin Expiración):** Creaba llaves eternas vulnerables. *Remediado en CA-22 forzando TTL y hash SHA-256.*
*   **GAP-4 (Ambigüedad en Tareas Delegadas):** No era claro qué pasaba con los folios a mitad de proceso. *Remediado en CA-23 asegurando la herencia de tareas "in-flight" y su retorno automático.*
*   **GAP-5 (Choque Arquitectónico US-036 vs US-038):** Riesgo de crear validaciones JWT duplicadas. *Remediado en CA-25 separando responsabilidades y forzando el consumo de la misma Redis Blacklist.*

## 6. Lista de Exclusiones (Fuera de Alcance)
*   **Ocultamiento de Campos a nivel de DOM (CA-12):** La Pantalla 14 gobierna el acceso al formulario general; la lógica para ocultar columnas de campos específicos dentro del formulario pertenece al Form Builder (Pantalla 7).
*   **Manejo Interno de Roles Dinámicos de BPMN (CA-13):** La resolución matemática de "Expression Lanes" dentro del BPMN XML es tarea del motor Camunda, no del módulo RBAC.
*   **Generación de Reportes Automáticos ISO 27001 (CA-24):** El cronjob y envío automático por correo del informe del CISO se posterga a V2. V1 solo permite la descarga bajo demanda.
*   **Segregación de Funciones Automática "In-Flight" (CA-18):** El motor no abortará transacciones automáticamente si el flujo permite que un mismo usuario apruebe lo que creó (Juez y Parte); queda diferido a V2.

## 7. Observaciones de Alineación o Riesgos
### Clasificación MoSCoW
*   **Must Have:** Es un componente neurálgico del MVP. El iBPMS no puede certificarse operacional ni superar auditorías de seguridad sin la inmutabilidad de la Matriz RBAC.

### Resumen de Dependencias con otras User Stories
*   **Dependencia Obligatoria con US-038:** La arquitectura base de seguridad, JWT, Redis, Sudo-Mode y sincronización EntraID es el lienzo sobre el cual opera la US-036 (según CA-25).
*   **Sinergia con US-048 (IdP Local):** La Fábrica de Roles descrita en US-048 requiere que el modelo de base de datos RBAC de esta US-036 ya esté completamente migrado.

### Dependencia Bloqueante Absoluta (Riesgo Técnico)
*   Para garantizar la viabilidad del **CA-20 (Row-Level Security / Privacidad de Colas)**, el Backend Lead debe configurar el aspecto/interceptor JPA antes del inicio del desarrollo de los repositorios del Workdesk. Si se implementa un modelo distribuido sin RLS forzado a nivel Spring Data, se generará una deuda técnica crítica y potencial exposición de PII (Personally Identifiable Information).
