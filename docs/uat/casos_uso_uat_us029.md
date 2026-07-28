# Casos de Uso UAT — US-029: Ejecución y Envío de Formulario

> **US:** US-029 — Ejecución y Envío de Formulario (iForm Maestro o Simple)
> **Actor principal:** Operario / Analista / Usuario de Negocio
> **Criticidad:** 🔴 ALTA
> **Épica:** Motor Formularios & BFF (Épica B)
> **Fecha:** 2026-04-13
> **Autor:** Agente PO

---
## Precondiciones
| # | Precondición | Verificación |
|---|-------------|-------------|
| PRE-1 | Operario posee una Tarea "Reclamada" (Assignee configurado) en Workdesk | US-002 validada |
| PRE-2 | Tarea está ligada a un `"formKey"` válido que fue diseñado | US-003 validada |
| PRE-3 | El esquema Zod del formulario está certificado en Productivo | US-028 (CA-11) |

---
## Escenarios UAT

### CU-US029-01: Renderizado Inicial y Mega-DTO (Patrón BFF)
**CA Mapeado:** CA-05, CA-10, CA-33
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Abre la TK-100 desde "Mi Bandeja" | El navegador carga Componente Vue |
| 2 | Frontend | Dispara petición `GET /form-context` al BFF | El spinner de carga se visualiza una única vez |
| 3 | Backend | Empaqueta Zod Schema, Layout Vue y `prefillData` histórico en un solo DTO | Responde HTTP 200 en ms |
| 4 | Frontend | Dibuja dinámicamente inputs en pantalla según Layout | — |
| 5 | Frontend | Rellena la data histórica inyectando los atributos de Solo Lectura (CA-33) | Inputs en gris `#F5F5F5`, ícono candado 🔒 y Tooltip "Solo lectura" |
**Criterio de aceptación:** Ausencia de parpadeos o "Cascading Fetches" (Carga en bloque única).

### CU-US029-02: Navegación Estructural Wizard y Auto-Scroll
**CA Mapeado:** CA-22, CA-25, CA-02
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Abre un iForm Maestro dividido en 3 Fases | Ve Barra de progreso arriba (① Datos → ② Verificación) |
| 2 | Operario | Pulsa el botón inferior [Siguiente ▶] sin haber llenado la Cédula | — |
| 3 | Frontend | Ejecuta `.safeParse()` Zod en memoria | Falla validación |
| 4 | Frontend | Bloquea el salto a Fase 2 | — |
| 5 | Frontend | Realiza Scroll suave (auto-scroll) hacia el input Cédula y pone el Foco visual | Borde parpadeante Rojo + Icono Alerta (CA-25) |
**Criterio de aceptación:** Asistencia cinética forzosa; el operario jamás tiene que adivinar qué esqueleto le impide avanzar a la siguiente pestaña.

### CU-US029-03: Tolerancia a la Interrupción Continua (Auto-Guardado Local)
**CA Mapeado:** CA-03, CA-11, CA-26, CA-31
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Teclea 5 campos pero decide irse a otra pestaña | La UI muestra "💾 Solo en este navegador" transitoriamente |
| 2 | Frontend | Espera 10 segundos de inactividad (Debounce) | Realiza criptografía local AES a campos PII |
| 3 | Frontend | Ejecuta Petición Asíncrona silente `PUT /draft` | UI cambia a status "☁️ Sincronizado" animado |
| 4 | Operario | Abandona el caso por 3 días | Abre otra sesión luego en la UI de Tareas |
| 5 | Frontend | Verifica caché. Despliega Advertencia | "⚠️ Tu borrador se eliminará en 24 horas" (CA-26) |
**Criterio de aceptación:** Cero pérdidas de datos de digitación humana pesada combinando resiliencia `LocalStorage` con Backup Servidor.

### CU-US029-04: Gestión Documental Separada con Aduana Completa (Upload-First)
**CA Mapeado:** CA-09, CA-28, CA-29
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Arrastra un "Acta_Aprobada.pdf" de 8 MB al Dropzone | Inicia subida aislada (Upload-first) |
| 2 | Frontend | Muestra una barra de progreso viva con porcentaje numérico | "Subiendo... 45%" |
| 3 | Backend | Recibe y verifica Magic Bytes (No es Renameado `.exe`) | Guarda en Bóveda SGDEA Transitoria |
| 4 | Backend | Responde `UUID-991` | Frontend marca el chip en verde: `Acta_Aprobada.pdf ✅` |
| 5 | Operario | Pulsa el botón [Enviar] Final | Envía solo metadata `{"pdf_id": "UUID-991"}` a Camunda |
**Criterio de aceptación:** El Payload central NUNCA recibe adjuntos pesados en binario ni Base64. Escudo MIME aplicado.

### CU-US029-05: Sumisión (Submit), Idempotencia UX y RYOW Definitivo
**CA Mapeado:** CA-12, CA-17, CA-20, CA-21
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Hace clic frenéticamente sobre [Enviar Formulario] 3 veces | Genera 3 peticiones Javascript |
| 2 | Frontend | Aplica Overlay opaco (CA-20), encripta el `Idempotency-Key` en Header único | El texto del botón cambia a "Enviando..." y se bloquea |
| 3 | Backend | Ejecuta proceso; Retorna OK | — |
| 4 | Frontend | Cambia Overlay a "¡Tarea completada exitosamente! ✅" | Se congela por 3 segundos de victoria lúdica |
| 5 | Frontend | Ejecuta evento RYOW (Read-Your-Own-Writes) | Destruye llave LocalStorage, mata la TK100 del Store RAM |
| 6 | Frontend | Redirige automáticamente al usuario a su Bandeja | La Tarea finalizada ES INVISIBLE inmediatamente al retornar |
**Criterio de aceptación:** Estricta consistencia final en UI que mitiga el Dedo Tembloroso e impide a los operadores visualizar la misma tarea finalizada por latencias internas.

### CU-US029-06: Conflicto Mid-Flight y Superposición Ciega de Layouts
**CA Mapeado:** CA-08, CA-27
| Paso | Actor | Acción | Resultado Esperado |
|:----:|-------|--------|-------------------|
| 1 | Operario | Digita una solicitud de 20 inputs versión V1 durante 45 mins | — |
| 2 | Arq. IT | Despliega y fuerza a producción un parche de Formulario (V2) agregando Campo Obligatorio | — |
| 3 | Operario | Presiona [Enviar] al terminan su data en base a V1 | Rechazo cruzado HTTP 409 |
| 4 | Frontend | Ataja el HTTP 409 y despliega Modal Informativo "El formulario fue actualizado..." | — |
| 5 | Frontend | Inyecta y Refresca el lienzo con V2 cargando datos Draft por debajo | — |
| 6 | User UI | Muestra el Formulario intacto excepto por 1 Input pintado en ROJO vivo exigiendo nueva data | Impide el pase hasta llenado de la amnistía funcional |
**Criterio de aceptación:** Supervivencia orgánica y sin pérdidas ni fustración cuando ocurre un release candente.

---
## Escenarios Negativos

### CU-US029-NEG-01: Intrusión y Falsificación en la validación Isomórfica (Bypass)
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Actor Malicioso captura Payload de Browser y mediante `curl` añade `{ "salario_secreto": 50000 }` a campo ofuscado |
| 2 | Backend corre nuevamente el `schema.json` Zod |
| 3 | Observa la violación sobre un "Campo Solo-Lectura" o inexistente en Layout |
| 4 | Aplica sanción `strip()` y descarta el JSON sucio o arroja Error 400 Inmediato (CA-15) |

### CU-US029-NEG-02: Colisión Multi-Pestaña de Escritura Ciega (Split Brain)
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario abre TK-100 en Edge y TK-100 en Chrome/Nueva Pestaña concurrente |
| 2 | La segunda pestaña es bloqueada y puesta en modo Standby / Read-only con Banner "Tarea abierta en otra pestaña" |
| 3 | Se previene la destrucción cruzada de LocalStorage salvando el historial del Master local (CA-30) |

### CU-US029-NEG-03: Adjuntar Bombas Digitales / Archivos Masivos
| Paso | Resultado Esperado |
|:----:|-------------------|
| 1 | Operario drag-and-drop archivo corrupto tipo `PeliculaHD_Renamed.pdf` de 4 GB |
| 2 | Frontend Zod-UI rechaza la inserción antes del Upload (Límite 25MB) CA-28 |
| 3 | Evita que el Event Loop del browser colapse o se inicie Request de red masivo |

---
## Matriz de Trazabilidad
| Escenario | US | CAs Cubiertos | Prioridad |
|-----------|:--:|:------------:|:---------:|
| CU-US029-01 | US-029 | CA-05, CA-10, CA-33 | MUST |
| CU-US029-02 | US-029 | CA-02, CA-22, CA-25 | MUST |
| CU-US029-03 | US-029 | CA-03, CA-11, CA-26, CA-31 | MUST |
| CU-US029-04 | US-029 | CA-09, CA-28, CA-29 | MUST |
| CU-US029-05 | US-029 | CA-12, CA-17, CA-20, CA-21 | MUST |
| CU-US029-06 | US-029 | CA-08, CA-27 | MUST |
| CU-US029-NEG-01 | US-029 | CA-15 | MUST |
| CU-US029-NEG-02 | US-029 | CA-30 | MUST |
| CU-US029-NEG-03 | US-029 | CA-28 | MUST |
