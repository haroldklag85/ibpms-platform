# Análisis Funcional Definitivo: US-038 (Asignación Multi-Rol y Autenticación EntraID Sincronizada)

## 1. Resumen del Entendimiento
La US-038 regula la entrada a la plataforma mediante Single Sign-On (SSO) corporativo (Microsoft EntraID). Intercepta Claims Incompletos e inyecta Multi-Rol (Badges/Pestañas Unificadas). Aplica posturas SRE "Fail-Open" si el mecanismo antibloqueo falla en memoria temporal temporal.

## 2. Objetivo Principal
Garantizar la Gobernanza de Múltiples Sombreros para un Analista, la tolerancia a desconexión y proveer un "Auto-Unclaim" (Reasignación Zombie) asíncrono para liberar el motor BPMN en caso de salida abrupta del empleado.

## 3. Alcance Funcional Definido
**Inicia:** Envío Bearer Token EntraID contra el Motor API Gateway.
**Termina:** El usuario goza su multi-bandeja unificada sin colisiones de permisos aditivos, y genera un Correlation ID JWT Log.

## 4. Lista de Funcionalidades Incluidas
- **Redis Fail-Open (CA-3873):** Si el servidor caché de Blacklist cae, permite logins válidos evitando un cuello de botella letal en redes corporativas (Asume riesgo sobre el Despido, salva continuidad SaaS).
- **Anti-Token Bloat Prefijo (CA-3881):** Sólo los Grupos Azure que arranquen con `ibpms_rol_` viajan al JWT local salvando límites HTTP Headers por sobre-carga.
- **Just-In-Time (JIT) Block Gating (CA-3887):** Identidad EntraID vacía (Sin sucursal o cédula) se estrella con un frontend "Modal Incompletitud", protegiendo la Base de Datos relacional BPMN de registros huérfanos.
- **Break-Glass Auditado y Roto (CA-3898):** Cuenta de Emergencia de Nube que activa paneles rojos y obliga a ser rota terminada la contingencia de Active Directory caído.
- **Auto-Unclaim RabbitMQ Asíncrono (CA-3934/CA-3925):** Emisión agresiva de Webhook Interno al Muro cuando alguien tiene "Muerte Lógica" (Despido/Ausencia). Rabbit libera las tareas Camunda "A la brava" devolviéndolas a the public Pool (Zombie Exorcise).
- **Correlation-ID HTTP (CA-3944):** Trazabilidad cross-módulo.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Activa US-036 SoD (⚠️ CA-3915):** Exige Bloqueo estricto del Originador sobre el Aprobador (`Creator_ID != Approver_ID`). Choca destructivamente con US-036 CA-3849. Se requiere una resolución inminente con PO para determinar la "Relajación Funcional".
- **Falsa Expectativa en Auto-Unclaim Zeebe (⚠️ CA-3934):** RabbitMQ se usará para "desasignarle" las tareas (Unclaim Task) a los empleados caídos. A nivel de arquitectura Camunda V8 (Zeebe), las tareas `UserTask` son controladas por Tasklist. Quitar una tarea requiere listar todas las activas, iterar y mandar API Gql Mutation de `Unclaim`. Hacer esto de forma "Masiva y Agresiva" puede desatar Rate Limits duros en el GraphQL de Operate/Tasklist de Camunda si el empleado despedido o ausente tenía 4,000 folios abiertos. La cola DLQ de RabbitMQ deberá implementar Throttling explícito "X request/sec" hacia la red interna Camunda.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Restablecimiento de contraseña por correo público (Diferido US-048 y CA-3970).

## 7. Observaciones de Alineación o Riesgos
El enfoque de permisos Aditivos "Allow-Overrides" (CA-3910) es una directriz valiosísima. Evita el "Infierno Deny". Si un humano tiene rol Consultor y Rol Administrador, el Backend no requiere motores P-Logic deductivos (Xor/Nor), simplemente fusiona permisos positivamente, descargando el 70% de complejidad de evaluación matemática por cada request.
