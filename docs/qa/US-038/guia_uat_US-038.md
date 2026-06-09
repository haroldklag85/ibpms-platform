# 🧪 Guía de Pruebas UAT Humanas — US-038: Asignación Multi-Rol y Sincronización EntraID

> **Generado por:** 🧪 UAT-GUIDE LEAD (Agente de Diseño de Pruebas)
> **Fecha de generación:** 2026-05-20T19:58:29-05:00
> **Épica de origen:** epic_E_seguridad_identidad_config.md
> **Rama de referencia:** main (o rama de pruebas actual)
> **Versión del guion:** 1.0

---

## 📋 Resumen de Cobertura

| CA | Título | Estado Coverage | ¿Incluido en este guion? |
|----|--------|:---------------:|:------------------------:|
| CA-01 | Tolerancia a Fallos del Kill-Switch (Redis Fail-Open) | ✅ | SÍ |
| CA-02 | Filtro de la Mochila Pesada (Anti-Token Bloat) | ✅ | SÍ |
| CA-03 | Aprovisionamiento JIT con Guardrail Claims Mínimos | ✅ | SÍ |
| CA-04 | Protocolo Break-Glass con Cierre de Ciclo | ✅ | SÍ |
| CA-05 | Resolución Aditiva de Permisos (RBAC Simple) | ✅ | SÍ |
| CA-06 | Detección y Contención SoD (Juez y Parte) | ✅ | SÍ |
| CA-07 | Proxy Temporal de Autoridad y Exorcismo de Tareas | ✅ | SÍ |
| CA-08 | El Exorcismo de Tareas por Despido | ✅ | SÍ |
| CA-09 | Trazabilidad Quirúrgica (Distributed Tracing V2) | ⏸️ | NO (Diferido a V2) |
| CA-10 | Consolidación Transversal e Insignia de Procedencia | ✅ | SÍ |
| CA-11 | Indicador Tipográfico de Dominio en Cabecera | ✅ | SÍ |
| CA-12 | Tablero de Resolución de Anomalías de Seguridad | ✅ | SÍ |
| CA-13 | Postergación de Reset de Password para V2 | ⏸️ | NO (Diferido a V2) |

**Total CAs de la US:** 13
**CAs con prueba en este guion:** 11
**CAs excluidos:** 2 (CA-09 y CA-13 diferidos a V2 según directrices de arquitectura).

---

## 🔑 Credenciales de Prueba

| Rol | Email | Contraseña |
|-----|-------|:----------:|
| Súper Administrador | `root@ibpms.local` | `Root#Temp4Sys` |
| Administrador | `admin@ibpms.local` | `admin123` |
| Analista N1 (Ejecutor) | `analista1@ibpms.local` | `admin123` |
| Analista N2 (Revisor) | `analista2@ibpms.local` | `admin123` |

*(Estas son las credenciales para la prueba local sin Microsoft EntraID conectado, usando el Fallback local o modo Híbrido).*

---

## 🧪 PRUEBAS

---

### 🧪 Prueba 1: Tolerancia a Fallos del Kill-Switch (Redis Fail-Open Policy)
> **@Traceability:** US-038, CA-01
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Administrador de Sistemas (Humano con acceso a Docker)

#### Precondiciones
- El usuario `root@ibpms.local` debe estar autenticado en el sistema.
- Estar posicionado en el Workdesk (Pantalla 1 - Bandeja de Tareas).
- Acceso a la terminal de Docker del entorno.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | En tu terminal de Docker, detén el contenedor de Redis para simular una caída. | Terminal del host | `docker stop redis` (o equivalente) | El contenedor de Redis se detiene. |
| 2 | En el navegador, actualiza la página o navega entre las diferentes colas del Workdesk (Peticiones GET). | Navegador / Workdesk | N/A | La página carga correctamente mostrando los datos. El sistema opera en modo "Fail-Open Degradado". |
| 3 | Intenta realizar una acción de escritura (ej. Completar una tarea, o Crear un usuario en Pantalla 14). | Botón de [Completar Tarea] o [Guardar] | N/A | El sistema rechaza la acción ("Fail-Closed") exigiendo Sudo-Mode o mostrando un error de degradación segura impidiendo mutar el estado. |
| 4 | Restaura el contenedor de Redis. | Terminal del host | `docker start redis` | El contenedor de Redis vuelve a estar activo. |

#### Criterio de Éxito
- [ ] La plataforma permite consultas (GET) sin Redis pero bloquea acciones de impacto (POST/PUT/DELETE) mientras el Kill-Switch está inoperativo.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 2: Filtro de la Mochila Pesada (Anti-Token Bloat)
> **@Traceability:** US-038, CA-02
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Analista N1

#### Precondiciones
- Iniciar sesión utilizando un entorno con integración SSO EntraID (si aplica) o inspeccionar la red con el Inspector del navegador (F12).

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Iniciar sesión en el sistema. | Pantalla de Login | `analista1@ibpms.local` / `admin123` | Se ingresa correctamente al sistema. |
| 2 | Abrir las herramientas de desarrollo del navegador (F12) y buscar el JWT almacenado (en Session Storage, Local Storage, o Cookie). | DevTools (F12) > Application > Storage | N/A | El token se encuentra disponible. |
| 3 | Decodificar el JWT (puedes usar jwt.io o mirarlo en red). | jwt.io | Pegar el JWT | En la lista de `roles` o `groups` del token, **solo** deben existir roles que comiencen con el prefijo oficial (ej. `ibpms_rol_`), descartando automáticamente grupos masivos de EntraID irrelevantes. |

#### Criterio de Éxito
- [ ] El payload del Token está optimizado y no incluye grupos genéricos de la corporación ajenos al sistema.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 3: Aprovisionamiento JIT con Guardrail Claims Mínimos
> **@Traceability:** US-038, CA-03
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Nuevo Usuario (Simulación JIT)

#### Precondiciones
- Configurar un usuario nuevo en EntraID (si está conectado) que intencionalmente no tenga el claim de `Sucursal_ID` o `Manager_ID`, o invocar el endpoint de `/auth/sync` forzando un payload incompleto.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Iniciar sesión por primera vez con el usuario incompleto (o disparar simulación). | Pantalla de Login | Usuario sin Claims completos | El sistema no redirige directamente al Workdesk. |
| 2 | Observar la pantalla resultante. | Interfaz Principal | N/A | El Frontend intercepta el acceso y renderiza un Modal bloqueante con el texto "Completar Perfil Local" solicitando los datos faltantes. |
| 3 | Completar la información requerida en el Modal y hacer clic en Guardar. | Modal "Completar Perfil Local" | `Sucursal X` (ejemplo) | El perfil se completa y el usuario es redirigido con éxito a la plataforma con rol "Ciudadano_Interno". |

#### Criterio de Éxito
- [ ] El motor JIT frena usuarios sin claims mínimos vitales y fuerza la recolección de los mismos antes de operar.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 4: Protocolo Break-Glass con Cierre de Ciclo
> **@Traceability:** US-038, CA-04
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Estar en la pantalla de Login y **sin** sesión iniciada.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | En la pantalla de login, buscar el enlace o botón oculto/pequeño para "Login de Emergencia" / "Break-Glass" (suele ser un link subrayado en gris/rojo). Haz clic en él. | Pantalla de Login (zona inferior) | N/A | Se despliega el formulario especializado `BreakGlassLogin`. |
| 2 | Ingresar las credenciales locales de la cuenta Break-Glass (Súper Administrador) e incluir OBLIGATORIAMENTE la justificación del incidente. | Formulario Break-Glass | Usuario: `root@ibpms.local` | El sistema permite el acceso al Workdesk y dispara alertas de severidad al backend. |
| 3 | Una vez dentro, verificar que se haya generado una alerta sobre el uso de la cuenta Break-Glass (ver Prueba 12). | Menú Superior / Pantalla 14 | N/A | (Validaremos el cierre en la prueba 12). |

#### Criterio de Éxito
- [ ] El componente `BreakGlassLogin.vue` permite acceso de emergencia documentado con justificación obligatoria (HTML5 required bloquea el envío si está vacío).

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 5: Resolución Aditiva de Permisos (RBAC Simple)
> **@Traceability:** US-038, CA-05
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador / Analista Multi-rol

#### Precondiciones
- Haber iniciado sesión como Súper Administrador (`root@ibpms.local`).
- Estar en la Pantalla 14 (Administración de Seguridad / Identity Governance).

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Navegar a la gestión de usuarios y seleccionar a `analista1@ibpms.local`. | Pantalla 14 > Usuarios | Clic en el usuario | Se abre la ficha de edición del usuario. |
| 2 | En la sección de roles (asignación múltiple), asignar un Rol A (Solo Lectura) y un Rol B (Lectura y Escritura para ciertos procesos). Guardar cambios. | Modal de Edición | Marcar 2 roles | Cambios guardados correctamente. |
| 3 | Cerrar sesión e ingresar como `analista1@ibpms.local`. | Pantalla Login | `analista1@ibpms.local` | Acceso exitoso. |
| 4 | Navegar por el sistema intentando realizar las acciones de Escritura del Rol B. | Workdesk u otra Pantalla | Clic en Acciones | El sistema permite la acción. Se aplica un modelo aditivo (Allow-Overrides). |

#### Criterio de Éxito
- [ ] Un usuario con múltiples roles hereda el super-conjunto de permisos sin colisiones o denegaciones erróneas.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 6: Detección y Contención SoD (Juez y Parte)
> **@Traceability:** US-038, CA-06
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Analista (Usuario con permisos de Crear y Aprobar)

#### Precondiciones
- Tener un rol configurado que permita tanto crear instancias de un proceso como aprobarlas.
- Haber creado un caso/instancia (Ej. una solicitud) utilizando el usuario actual.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Buscar en el Workdesk la tarea de "Aprobación" del caso que acabas de crear tú mismo. | Workdesk (Bandeja) | N/A | Localizas la tarea. |
| 2 | Intentar abrir la tarea, o en su defecto, intentar completarla (Ej. Clic en botón Aprobar). | Botón de [Aprobar] o [Reclamar] | N/A | El Backend BLOQUEA la transacción. Muestra un mensaje/alerta indicando un conflicto de Segregación de Funciones (Juez y Parte) (`Creator_ID != Approver_ID`). |
| 3 | Intentar aprobar una tarea generada por OTRA persona. | Tarea de otro usuario | N/A | La transacción es exitosa. |

#### Criterio de Éxito
- [ ] El sistema rechaza estructuralmente que el usuario origen apruebe su propio caso. Registra la anomalía.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 7: Proxy Temporal de Autoridad y Exorcismo de Tareas
> **@Traceability:** US-038, CA-07
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Administrador / Analista N2 (El delegante)

#### Precondiciones
- Haber iniciado sesión como un usuario que posee tareas asignadas en su bandeja y tiene permisos para delegar.
- Tener un segundo usuario válido (Ej. Analista N1) para usarlo como Suplente.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Ir a tu Perfil de Usuario o a la Pantalla de Delegación Autónoma. | Menú de Perfil > Autogestión / Delegación | N/A | Se muestra el panel de delegación temporal. |
| 2 | Ceder el rol/bandeja a otro usuario especificando explícitamente un Rango de Fechas. | Formulario Delegación | Seleccionar `analista1@ibpms.local` y rango de fechas (Inicio: Hoy, Fin: Mañana) | El sistema registra la delegación (Se invoca endpoint `/api/v1/security/delegations`). |
| 3 | (Si el Frontend lo refleja), observar un indicador de que las tareas están siendo delegadas temporalmente. | Notificación o Bandeja | N/A | Se procesa la delegación y un evento asíncrono en RabbitMQ orquesta el movimiento de tareas en Camunda. |

#### Criterio de Éxito
- [ ] La interfaz permite delegar poder exigiendo obligatoriamente Rango de Fechas.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 8: El Exorcismo de Tareas por Despido
> **@Traceability:** US-038, CA-08
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Identificar a un usuario (Ej. `analista2@ibpms.local`) que actualmente tenga 1 o más tareas asignadas explícitamente a él en su Workdesk.
- Haber iniciado sesión como Súper Administrador.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Navegar a la administración de usuarios. | Pantalla 14 > Usuarios | N/A | Se visualiza la grilla de usuarios. |
| 2 | Buscar al usuario `analista2@ibpms.local` y cambiar su estado usando el "Toggle Switch" a "Inactivo" (Desactivar usuario). | Fila del usuario > Switch Activo/Inactivo | N/A | El sistema inactiva al usuario y dispara el evento asíncrono `security.user.deactivated`. |
| 3 | Buscar en la "Bandeja de Equipo" o cola pública las tareas que estaban asignadas a `analista2@ibpms.local`. | Workdesk > Tareas de Grupo | N/A | Las tareas que el usuario tenía "Reclamadas" sufren un "Unclaim Masivo" y vuelven a estar disponibles en la cola de grupo. |

#### Criterio de Éxito
- [ ] La inactivación del usuario dispara el despojo forzoso de sus tareas en Camunda para evitar estancamiento de SLAs (Zombies).

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 9: Consolidación Transversal e Insignia de Procedencia
> **@Traceability:** US-038, CA-10
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Analista N2 (Con roles combinados)

#### Precondiciones
- El usuario `analista2@ibpms.local` debe tener asignados al menos dos roles distintos que le otorgan visibilidad de tareas en el Workdesk.
- Haber iniciado sesión.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Navegar al Workdesk (Bandeja Unificada). | Barra lateral > Workdesk | N/A | Aparecen múltiples tareas provenientes de diferentes procesos y roles. |
| 2 | Inspeccionar visualmente las filas de las tareas. | Grilla de tareas | N/A | Cada fila presenta un "Badge" o "Insignia visual" indicando el `targetRole` o procedencia de la tarea (ej. "Aprobador Nivel 2"), aclarando por qué se asignó a este usuario. |

#### Criterio de Éxito
- [ ] Se consolida la información y se inyecta un Badge visual indicando el rol de procedencia por fila.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 10: Indicador Tipográfico de Dominio en Cabecera
> **@Traceability:** US-038, CA-11
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Analista Multi-rol

#### Precondiciones
- Haber iniciado sesión.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Visualizar la barra superior principal (Master Header). | Cabecera de la aplicación | N/A | Se debe visualizar un micro-texto o chip de "Sombreros Principales" (ej. el icono "verified_user" o un texto resumido de los roles actuales de la sesión). |

#### Criterio de Éxito
- [ ] El Header renderiza reactivamente el Chip resumiendo visualmente los roles validados.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 11: Tablero de Resolución de Anomalías de Seguridad
> **@Traceability:** US-038, CA-12
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador / CISO

#### Precondiciones
- Haber ejecutado previamente la Prueba 4 (Break-Glass) o la Prueba 6 (Juez y Parte) para que exista una anomalía registrada en el sistema.
- Iniciar sesión como Súper Administrador.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Navegar a la Pantalla de Configuración de Seguridad. | Pantalla 14 (Identity Governance) | N/A | Carga la vista de administración. |
| 2 | Seleccionar la pestaña o sección denominada "Tablero de Anomalías". | Pestaña "Tablero de Anomalías" | N/A | Se listan en color Rojo/Alerta las incidencias de seguridad vivas (Ej. Uso de Break-Glass Account o Violación SoD). |
| 3 | Revisar un caso y hacer clic en el botón de subsanación (Ej. `[ ✅ Marcar como Subsanado ]`). | Fila de la anomalía | Clic en el botón | La alerta se limpia o marca como resuelta en el sistema (PUT `/resolve` ejecutado). |

#### Criterio de Éxito
- [ ] El tablero de anomalías lista incidentes graves (SoD, BreakGlass) y obliga al CISO a subsanarlos manualmente.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

## 📊 Resumen de Resultados (Completar por el Humano)

| # | Prueba | CA | Veredicto | Observaciones |
|:-:|--------|:--:|:---------:|---------------|
| 1 | Tolerancia a Fallos Kill-Switch | CA-01 | `___` | |
| 2 | Filtro de la Mochila Pesada | CA-02 | `___` | |
| 3 | Aprovisionamiento JIT (Guardrail) | CA-03 | `___` | |
| 4 | Protocolo Break-Glass | CA-04 | `___` | |
| 5 | Resolución Aditiva de Permisos | CA-05 | `___` | |
| 6 | Detección SoD (Juez y Parte) | CA-06 | `___` | |
| 7 | Proxy Temporal y Exorcismo | CA-07 | `___` | |
| 8 | Exorcismo por Despido | CA-08 | `___` | |
| 9 | Insignia de Procedencia (Bandeja) | CA-10 | `___` | |
| 10| Indicador en Cabecera | CA-11 | `___` | |
| 11| Tablero de Anomalías | CA-12 | `___` | |

### Firma de Certificación
- **Tester:** ________________________
- **Fecha de ejecución:** ________________________
- **Veredicto general:** `PASS` / `PASS CON OBSERVACIONES` / `FAIL`
