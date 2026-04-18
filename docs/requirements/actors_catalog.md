# Catálogo de Actores y Roles del Sistema iBPMS
**Última actualización:** 2026-04-05

> [!NOTE]
> Este catálogo define TODOS los tipos de usuario que interactúan con la plataforma iBPMS. Cada rol tiene permisos diferenciados gestionados centralmente en la US-036 (Pantalla 14 - RBAC Manager).

---

## 1. Roles Internos (Empleados de la Organización)

### 1.1 Operario / Analista
| Atributo | Valor |
|---|---|
| **Descripción** | Usuario que ejecuta el trabajo operativo diario: reclama tareas, llena formularios, mueve tarjetas Kanban. |
| **Pantallas con acceso** | Pantalla 1 (Workdesk), Pantalla 2 (Formularios), Pantalla 3 (Kanban) |
| **US asociadas** | US-001, US-002, US-008, US-029 |
| **Restricciones** | No puede crear procesos ni modificar configuraciones del sistema. Solo ve tareas de su grupo/rol. |

### 1.2 Líder SAC (Service Attention Center)
| Atributo | Valor |
|---|---|
| **Descripción** | Líder del equipo de atención al cliente. Gestiona buzones, revisa propuestas de IA, supervisa colas de correos. |
| **Pantallas con acceso** | Pantalla 1B (Docketing), Pantalla 2C (Copiloto IA), Pantalla 16 (Buzones) |
| **US asociadas** | US-011, US-012, US-013, US-014, US-016, US-037 |
| **Restricciones** | No ve dashboards BAM ni configuraciones SLA. Solo gestiona su(s) buzón(es) asignado(s). |

### 1.3 Arquitecto de Procesos (BPM Analyst)
| Atributo | Valor |
|---|---|
| **Descripción** | Diseña y modela procesos BPMN, reglas DMN y formularios. Usa las herramientas de diseño. |
| **Pantallas con acceso** | Pantalla 6 (Modelador BPMN), Pantalla 7 (iForm IDE), Pantalla 8 (WBS) |
| **US asociadas** | US-003, US-005, US-006, US-007, US-027, US-028, US-039 |
| **Restricciones** | Puede consultar el Diccionario IA en modo *Read-Only* (US-051) pero no crear reglas IA. |

### 1.4 PMO (Project Management Officer)
| Atributo | Valor |
|---|---|
| **Descripción** | Gestiona los proyectos ágiles y tradicionales. Configura SLAs, sprints, calendarios. |
| **Pantallas con acceso** | Pantalla 5 (BAM), Pantalla 10 (Proyecto Ágil), Pantalla 10.B (Gantt), Pantalla 19 (SLA) |
| **US asociadas** | US-009, US-018, US-030, US-031, US-043 |
| **Restricciones** | No accede a configuraciones de seguridad ni DevPortal. |

### 1.5 Ejecutivo de Cuenta / Gestor CRM
| Atributo | Valor |
|---|---|
| **Descripción** | Gestiona la relación con clientes externos. Crea expedientes, revisa Cards de estado, ejecuta intake manual. |
| **Pantallas con acceso** | Pantalla 4 (Service Delivery), Pantalla 2 (Formularios asociados) |
| **US asociadas** | US-019, US-020, US-021, US-022, US-023, US-024, US-025, US-040, US-041 |
| **Restricciones** | Solo ve expedientes de sus clientes asignados (Row-Level Security). |

---

## 2. Roles Administrativos (Gobernanza del Sistema)

### 2.1 Súper Administrador (Super Admin)
| Atributo | Valor |
|---|---|
| **Descripción** | Control total del sistema. Gestiona roles, identidades, configuraciones globales y limpia datos críticos. Requiere Sudo Mode para acciones destructivas. |
| **Pantallas con acceso** | TODAS |
| **US asociadas** | US-036, US-038, US-042, US-043, US-044, US-045, US-046, US-048 |
| **Restricciones** | Requiere re-autenticación (Sudo Mode) para acciones destructivas (US-051). Todas sus acciones son auditadas. |

### 2.2 Oficial de Seguridad (CISO)
| Atributo | Valor |
|---|---|
| **Descripción** | Gestiona la matriz RBAC, genera reportes ISO 27001, revoca sesiones, administra API Keys. |
| **Pantallas con acceso** | Pantalla 14 (RBAC Manager), Pantalla 13 (DevPortal - solo lectura) |
| **US asociadas** | US-036, US-038, US-048, US-051 |
| **Restricciones** | No diseña procesos BPMN ni gestiona configuraciones operativas (SLA, Kanban). |

### 2.3 Administrador de TI
| Atributo | Valor |
|---|---|
| **Descripción** | Gestiona integraciones técnicas, rendimiento, conexiones de buzón y health-checks. |
| **Pantallas con acceso** | Pantalla 15 (Settings), Pantalla 15.A (Performance), Pantalla 16 (Buzones) |
| **US asociadas** | US-034, US-037, US-046, US-049 |
| **Restricciones** | No accede a RBAC ni DevPortal. |

### 2.4 Administrador de IA / MLOps Engineer
| Atributo | Valor |
|---|---|
| **Descripción** | Configura los límites de tokens IA, define modelos LLM, gestiona feedback loops y parámetros de entrenamiento. |
| **Pantallas con acceso** | Pantalla 15 (Settings - sección IA), Command Center |
| **US asociadas** | US-015, US-044, US-052, US-053 |
| **Restricciones** | No gestiona identidades ni permisos RBAC. |

---

## 3. Roles Externos

### 3.1 Cliente Externo (B2B/B2C)
| Atributo | Valor |
|---|---|
| **Descripción** | Persona o empresa externa que consulta el estado de sus casos, descarga documentos finales y gestiona su cuenta vía Portal B2C. |
| **Pantallas con acceso** | Portal B2C (auto-consulta) |
| **US asociadas** | US-026, US-050 |
| **Restricciones** | Zero-Public-Signup: solo puede registrarse mediante Magic Link enviado por la empresa. Solo ve SUS datos (Anti-BOLA con Custom Claim). |

### 3.2 Desarrollador / Integrador Externo
| Atributo | Valor |
|---|---|
| **Descripción** | Desarrollador de terceros que construye integraciones mediante API Keys y el DevPortal. |
| **Pantallas con acceso** | Pantalla 13 (DevPortal) |
| **US asociadas** | US-042 |
| **Restricciones** | Solo accede vía API Key. Sin acceso al sistema interno. Rate-limited. |

---

## 4. Actores No Humanos (Bots y Sistema)

### 4.1 Agente IA (Bot)
| Atributo | Valor |
|---|---|
| **Descripción** | Componente autónomo de IA que ejecuta tareas especializadas: propone respuestas de correo, genera DMN, audita procesos. |
| **Representado por** | Service Account con API Key dedicada (US-042) |
| **US asociadas** | US-007, US-012, US-013, US-014, US-027, US-032, US-052, US-053 |
| **Restricciones** | Presupuesto de tokens limitado (US-053 - FinOps). Todas las acciones requieren validación humana (Human-in-the-Loop) excepto tareas automáticas pre-aprobadas. |

### 4.2 Motor Camunda (Sistema)
| Atributo | Valor |
|---|---|
| **Descripción** | Motor de procesos que orquesta el flujo BPMN. Actúa como usuario de sistema para avanzar tareas automáticas, ejecutar ServiceTasks y evaluar Gateways. |
| **Representado por** | Cuenta de servicio interna |
| **US asociadas** | US-005, US-017 |
| **Restricciones** | Aislado del dominio Core (Arquitectura Hexagonal). No accede directamente a RabbitMQ. |

### 4.3 Worker RabbitMQ (Sistema)
| Atributo | Valor |
|---|---|
| **Descripción** | Proceso en segundo plano que consume colas de mensajería para ejecutar tareas asíncronas (envío de correos, proyecciones CQRS, sincronización CRM). |
| **Representado por** | Proceso JVM interno |
| **US asociadas** | US-017, US-034, US-049 |
| **Restricciones** | Opera out-of-band. No tiene acceso a la sesión del usuario. Solo procesa mensajes de las colas asignadas. |

---

## 5. Matriz Resumida de Accesos

| Rol | P1 | P2 | P3 | P4 | P5 | P6 | P7 | P8 | P10 | P13 | P14 | P15 | P16 | P19 | B2C | CC |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Operario | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Líder SAC | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Arquitecto BPM | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PMO | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Ejecutivo Cuenta | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Super Admin | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| CISO | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | 👁️ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Admin TI | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Admin IA | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Cliente Externo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Dev Externo | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

**Leyenda:** ✅ = Acceso completo | 👁️ = Solo lectura | ❌ = Sin acceso | P = Pantalla | B2C = Portal cliente | CC = Command Center
