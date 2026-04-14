# Casos de Uso UAT — US-001: Workdesk Pendientes

> **US:** US-001 — Obtener Tareas Pendientes en el Workdesk
> **Actor principal:** Operario / Analista / Administrador
> **Criticidad:** 🔴 ALTA
> **Épica:** Motor Core BPMN & Workdesk (Épica A)
> **Fecha:** 2026-04-13
> **Autor:** Agente PO

---
## Precondiciones
| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-1 | Usuario autenticado y con roles asignados | JWT en localStorage |
| PRE-2 | Existen tareas pendientes reales en BD para el usuario y su grupo | GET `/api/v1/workdesk/tasks` |
| PRE-3 | El usuario tiene asignados los skills correspondientes | Matriz RBAC / Pantalla 14 |
| PRE-4 | Conexión WebSocket establecida exitosamente | WS conectado, no polling fallando |
---
## Escenarios UAT

### CU-US001-01: Desaparición instantánea y atenuada (WebSocket)
**CA Mapeado:** CA-06, CA-13, CA-27
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Observa su Workdesk (Cola Grupal) con una tarea TK-10 | — |
| 2 | Compañero | Reclama la tarea TK-10 desde otra sesión | Envía evento WS de acción REMOVE |
| 3 | Sistema (Operario) | Recibe payload atómico `{ action: 'REMOVE', taskId: 'TK-10' }` | — |
| 4 | Sistema (Operario) | Ejecuta desvanecimiento visual `opacity: 0` | La tarea no salta; desaparece suavemente |
| 5 | Sistema (Operario) | Muestra discretamente un Toast ("Tarea reclamada por otro equipo") | — |
**Criterio de aceptación:** Eliminación visual fluida de tareas reclamadas en fondo por otros operarios impidiendo errores de selección.

### CU-US001-02: Relleno automático tras purga WebSocket
**CA Mapeado:** CA-26
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Sistema | Contiene 15 tarjetas en la primera página | — |
| 2 | Compañero | Reclama una de estas tareas vía WS | Tarea es atenuada y eliminada |
| 3 | Sistema | Inicia ventana de throttling (5 segundos) | Acumula remociones |
| 4 | Sistema | Finaliza ventana de tiempo con la página en 14/15 tarjetas | — |
| 5 | Sistema | Dispara petición HTTP silenciosa (Fetch) | Solicita tarjetas faltantes para rellenar |
| 6 | Sistema | Inyecta las tareas con fade-in | El grid mantiene 15 registros sin saltos |
**Criterio de aceptación:** Las lagunas creadas por consumo WebSocket son reparadas discretamente para conservar Paginación perfecta de 15 tarjetas.

### CU-US001-03: Integridad visual del Ticking Engine SLA
**CA Mapeado:** CA-05, CA-11, CA-24
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Abre su Workdesk con tareas al límite | La vista carga 50 semáforos integrados |
| 2 | Sistema | Utiliza `requestAnimationFrame` en el Global Heartbeat | No hay colapsos por llamadas recurrentes de `setInterval` multiplicadas |
| 3 | Sistema | Detecta que el SLA restante cae por debajo del 15% | — |
| 4 | Sistema | Reactiva el badge SLA y cambia de Amarillo a 🔴 Rojo puro | Incluye un ícono legible (⚡/⏳/✔️) para accesibilidad visual |
| 5 | Operario | Visualiza cambio instantáneo sin F5 | Notificación cromática clara de la transición |
**Criterio de aceptación:** Desempeño óptimo y seguro frente a daltónicos de los estados transicionales temporales (SLA).

### CU-US001-04: Recálculo tras inactividad o pérdida de foco
**CA Mapeado:** CA-25, CA-31
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Minimiza o abandona la pestaña un Workdesk por 10 minutos | El browser pausa el `requestAnimationFrame` |
| 2 | Operario | Regresa (visibilidad recuperada) | Tira evento `visibilitychange` o similar |
| 3 | Sistema | Ejecuta "Recálculo Inmediato" del timestamp | Compara local `sla_deadline` vs `Date.now()` |
| 4 | Sistema | Restaura los semáforos que caducaron mientras estaba minimizado | El SLA parpadea de verde a rojo súbitamente |
| 5 | Sistema | Dispara auto-refresco (Pull HTTP) (CA-31) | Actualización silenciosa (shimmer) de data por superar los 5 minutos Inactivo |
**Criterio de aceptación:** Nunca se presentarán datos vencidos o oxidados si el operario vuelve de un break prolongado.

### CU-US001-05: Habilitación Segmentada por Delegación (No-IDOR)
**CA Mapeado:** CA-04, CA-15
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Ejecutivo | Abre Workdesk y pulsa en "Tareas de mi Asistente" | Se abre la interfaz cinética (Fondo alternativo / Destello) |
| 2 | Sistema | Añade banner: "Estás viendo el escritorio de X" | Prevención visual de confusión |
| 3 | Sistema | Lanza petición `/api/v1/workdesk/tasks/ID_ASISTENTE` | — |
| 4 | Backend (Seguridad) | Valida vía RBAC si el usuario que llama, es el jefe de ID_ASISTENTE | Retorna HTTP 200 OK y carga datos |
| 5 | Atacante | Edita URL para apuntar a ID de otro Gerente local | — |
| 6 | Backend (Seguridad) | Falla validación jerárquica cruzada | Retorna HTTP 403 Forbidden directo al rostro |
**Criterio de aceptación:** Inviolabilidad y protección estricta Anti-IDOR (Referencia directa insegura de objeto).

### CU-US001-06: Modo Reclamación Aleatoria y Enrutadora (Cherry-Picking Override)
**CA Mapeado:** CA-08, CA-16, CA-21, CA-28
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Administrador | Activa Switch Administrativo [Enrutamiento Forzoso] | Workdesk muta visualmente |
| 2 | Operario | Ingresa a Workdesk | La tabla clásica desaparece; solo ve Botón [Atender Siguiente] |
| 3 | Operario | Pulsa el botón [Atender Siguiente] (Simultáneo con Operario 2) | — |
| 4 | Backend | Emplea cruce de Score, Tiempo + Skills, blindado por SQL FOR UPDATE SKIP LOCKED | Toma decisión y asigna la tarea Óptima (1A) |
| 5 | Backend | Asigna tarea (1B) al Operario 2 si chocan a la vez | Ninguno sufre HTTP Conflict (CA-28) |
| 6 | Operario | Requiere evadir la tarea asignada (Skipeo) | Oprime [Skip], despliega select validado "Cliente ilocalizable" y libera (Audit Log) |
**Criterio de aceptación:** Distribución de carga matemática e inescrutable con barrera absoluta contra la evasión de tareas complejas.

---
## Escenarios Negativos

### CU-US001-NEG-01: Abuso y Throttling en Peticiones
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario lanza petición múltiple y desquiciada al API |
| 2 | API Gateway levanta límite a las 60 req (CA-30) |
| 3 | Frontend ataja un `429 Too Many Requests` |
| 4 | UI levanta notificación "Has realizado demasiadas consultas. Esperando..." y colapsa el botón de refresco artificial |

### CU-US001-NEG-02: Búsqueda Alterada con Manipulación de URL Pagination
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Atacante intercepta la llamada REST para ver Workdesk |
| 2 | Modifica en bruto `?size=1000` intentando ejecutar DDoS interno (CA-10) |
| 3 | Backend de Camunda/Relacional intercepta y rechaza inmediatamente con `HTTP 400 Bad Request` |
| 4 | Frontend responde reestableciendo paginación estándar a los 15 permitidos y expulsa un Exception Trace encapsulado de su consola local. |

### CU-US001-NEG-03: Delegación a Agente sin Vínculo Jerárquico IDOR
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario malintencionado edita payload para ver el tablero del Director Financiero |
| 2 | Backend detecta cruce nulo en el árbol RBAC (CA-15) |
| 3 | Genera log de seguridad y devuelve Error 403 HTTP. Pantalla permanece sin recargar tabla confidencial. |

---
## Matriz de Trazabilidad
| Escenario | US | CAs Cubiertos | Prioridad |
|-----------|:--:|:------------:|:---------:|
| CU-US001-01 | US-001 | CA-06, CA-13, CA-27 | MUST |
| CU-US001-02 | US-001 | CA-26 | MUST |
| CU-US001-03 | US-001 | CA-05, CA-11, CA-24 | MUST |
| CU-US001-04 | US-001 | CA-25, CA-31 | MUST |
| CU-US001-05 | US-001 | CA-04, CA-15 | MUST |
| CU-US001-06 | US-001 | CA-08, CA-16, CA-21, CA-28 | MUST |
| CU-US001-NEG-01 | US-001 | CA-30 | MUST |
| CU-US001-NEG-02 | US-001 | CA-10 | MUST |
| CU-US001-NEG-03 | US-001 | CA-15 | MUST |
