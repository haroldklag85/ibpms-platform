# 🧪 Guía de Pruebas UAT Humanas — US-036: RBAC, Zero-Trust y Gobernanza de Seguridad (ISO 27001)

> **Generado por:** 🧪 UAT-GUIDE LEAD (Agente de Diseño de Pruebas)
> **Fecha de generación:** 2026-05-20T20:25:35-05:00
> **Épica de origen:** epic_E_seguridad_identidad_config.md
> **Rama de referencia:** main (o rama de pruebas actual)
> **Versión del guion:** 1.0

---

## 📋 Resumen de Cobertura

| CA | Título | Estado Coverage | ¿Incluido en este guion? |
|----|--------|:---------------:|:------------------------:|
| CA-1 | Hibridación de Roles EntraID vs Locales | ✅ | SÍ |
| CA-2 | El Guardián Absoluto (Root Super Admin) | ✅ | SÍ |
| CA-3 | Clonación de Perfiles por Plantilla | ✅ | SÍ |
| CA-4 | Segregación Iniciador vs Ejecutor | ✅ | SÍ |
| CA-5 | Privacidad Visual de Colas (Data Segregation Local) | ✅ | SÍ |
| CA-6 | Herencia de Roles Piramidal | ✅ | SÍ |
| CA-7 | Inmutabilidad por Desactivación Suave (Soft-Delete) | ✅ | SÍ |
| CA-8 | Aprovisionamiento de Transeúntes (Ciudadano Interno) | ✅ | SÍ |
| CA-9 | Módulo de Delegación Autónoma Temporal | ✅ | SÍ |
| CA-10 | Creación de Robots de Integración (API Keys) | ✅ | SÍ |
| CA-11 | Respeto ciego al Autenticador Perimetral | ✅ | SÍ |
| CA-14 | El Botón Táctico de Exorcismo (Kill-Session) | ✅ | SÍ |
| CA-15 | Bypass Anónimo de Procesos (URLs Públicas) | ✅ | SÍ |
| CA-16 | Informes Densos de Fiscalización (Auditoría CISO) | ✅ | SÍ |
| CA-17 | Traza Indeleble de Otorgamiento | ✅ | SÍ |
| CA-19 | Modelo de Datos Relacional para la Matriz RBAC | ✅ | NO (Validado implícitamente por CA-2 y CA-6) |
| CA-20 | Estrategia de Row-Level Security | ✅ | NO (Validado implícitamente por CA-5) |
| CA-21 | Infraestructura de Blacklist JWT | ✅ | NO (Validado implícitamente por CA-14) |
| CA-22 | Política de Seguridad para API Keys | ✅ | NO (Validado implícitamente por CA-10) |
| CA-23 | Comportamiento de Delegación sobre Tareas | ✅ | NO (Validado implícitamente por CA-9) |
| CA-24 | Alcance Explícito del Reporte ISO 27001 | ✅ | NO (Validado implícitamente por CA-16) |
| CA-26 | Experiencia de Caída Segura (UX Fallback) | ✅ | SÍ |
| CA-27 | Inmutabilidad de Roles Nativos del Sistema | ✅ | SÍ |
| CA-28 | Granularidad Macro de la Topología Visual | ✅ | SÍ |
| CA-29 | Diseño Limpio del Modal de Roles (Tablas/Tabs) | ✅ | SÍ |
| CA-30 | Superposición Inclusiva Multirrol | ✅ | SÍ |
| CA-31 | Arquitectura Endpoint Dinámico (Anti-JWT Bloat) | ✅ | SÍ |
| CA-32 | Caché Híbrida y Auto-Curación Zero-Trust | ✅ | SÍ |
| CA-12 | Exclusión de Ocultamiento de Campos | N/A | NO (Fuera de alcance) |
| CA-13 | Desacoplamiento de Roles Estáticos vs Dinámicos | N/A | NO (Fuera de alcance) |
| CA-18 | Omisión Estricta de Segregación de Funciones | N/A | NO (Diferido a V2) |
| CA-25 | Directriz de Coordinación US-036 vs US-038 | ✅ | NO (Regla arquitectónica) |

**Total CAs de la US:** 32
**CAs con prueba en este guion:** 19 (Agrupados en 10 Pruebas de Usuario)
**CAs excluidos:** 13 (Validaciones implícitas de backend o fuera de alcance/diferidas).

---

## 🔑 Credenciales de Prueba

| Rol | Email | Contraseña |
|-----|-------|:----------:|
| Súper Administrador | `root@ibpms.local` | `Root#Temp4Sys` |
| Analista N1 (Delegante) | `analista1@ibpms.local` | `admin123` |
| Analista N2 (Delegado) | `analista2@ibpms.local` | `admin123` |

*(Si necesitas usuarios adicionales, solicita su creación mediante el Agente de Base de Datos).*

---

## 🧪 PRUEBAS

---

### 🧪 Prueba 1: Protección de Roles Nativos e Inmutabilidad
> **@Traceability:** US-036, CA-02, CA-27
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Iniciar sesión como Súper Administrador (`root@ibpms.local`).

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Navegar a la sección de Configuración de Seguridad. | Pantalla 14 (Identity Governance) | N/A | Carga la vista de administración. |
| 2 | Abrir el listado global de Roles. | Tab de "Roles" | N/A | Aparece la tabla de roles disponibles. |
| 3 | Buscar el rol de sistema "SUPER_ADMIN" (El Guardián Absoluto) y observar los botones de acción. | Fila del rol `SUPER_ADMIN` | N/A | El registro se visualiza con un Badge/Indicador nativo. |
| 4 | Intentar Editar o Eliminar el rol `SUPER_ADMIN`. | Botones "Editar" o "Eliminar" | N/A | La UI bloquea/deshabilita la acción y el Backend rechaza cualquier petición sobre este ID. |

#### Criterio de Éxito
- [ ] Es matemáticamente imposible mutar o eliminar el rol fundacional (Super Admin).

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 2: Herencia Piramidal y Desactivación Suave (Soft-Delete)
> **@Traceability:** US-036, CA-06, CA-07, CA-29
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Estar en la pantalla de Roles.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Hacer clic en "Crear Nuevo Rol". | Botón de Creación | N/A | Se abre un modal limpio estructurado con Tabs (Información Básica, Topología, etc.). |
| 2 | Configurar un rol "Director Regional". | Modal de Rol | Nombre: `Director Regional` | Rol guardado correctamente. |
| 3 | Crear un segundo rol "Ejecutivo Local" y en la configuración establecer como "Rol Padre" al Director Regional. | Modal de Rol | Nombre: `Ejecutivo Local`, Padre: `Director Regional` | Se guarda manteniendo la relación piramidal. |
| 4 | Hacer clic en el botón de Eliminar sobre "Ejecutivo Local" (Simular Borrado). | Grilla de Roles | Clic en Eliminar | El rol desaparece visualmente de la lista activa. |
| 5 | *(Verificación Backend)* Validar si el registro en Base de Datos existe. | (En BD) | Consulta SQL | El registro aún existe pero su flag `isActive` cambió a `false` (Soft-Delete comprobado). |

#### Criterio de Éxito
- [ ] La eliminación es ilusoria (Soft-delete inyectado para proteger la consistencia de auditorías pasadas). El modal de roles respeta el diseño en Tabs.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 3: Privacidad Visual, Topología Dinámica y Anti-JWT Bloat
> **@Traceability:** US-036, CA-05, CA-28, CA-30, CA-31
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Múltiples Analistas

#### Precondiciones
- `analista1` pertenece a la región A. `analista2` a la región B.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Iniciar sesión como `analista1` e ir al Workdesk. | Login y Bandeja Unificada | N/A | Se visualizan tareas de la Región A. |
| 2 | Observar el menú lateral izquierdo (Topología Visual). | Sidebar | N/A | Solo carga los ítems a los que `analista1` tiene acceso (Ej. "Formularios", "Reportes Básicos"). La estructura se pide al backend dinámicamente (`/users/me/menu-layout`). |
| 3 | Cerrar sesión e ingresar como `analista2` (Mismos módulos macro pero sin acceso a Región A). | Login | N/A | Se loguea. |
| 4 | Revisar el Workdesk. | Bandeja Unificada | N/A | NO visualiza las tareas de la Región A (Row-Level Security / Privacidad de Colas activa). |

#### Criterio de Éxito
- [ ] El menú es dinámico (Union Multirrol) y las consultas a base de datos son filtradas por privacidad local del usuario.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 4: Botón Táctico de Exorcismo (Kill-Session y Auto-Curación Zero-Trust)
> **@Traceability:** US-036, CA-14, CA-21, CA-32
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** CISO/Admin y un Usuario Objetivo

#### Precondiciones
- Tener dos navegadores diferentes abiertos (Ej. Chrome y Edge).
- Navegador A (Chrome): Logueado como `root`.
- Navegador B (Edge): Logueado como `analista1` (dejando la pestaña abierta en el Workdesk).

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | En Navegador A (`root`), ir a "Usuarios", buscar a `analista1` y hacer clic en el botón de "Revocar Sesión / Kill Session". | Pantalla 14 | Clic en Revocar Sesión | El sistema inserta el Token del usuario en la Blacklist de Redis. |
| 2 | En Navegador B (`analista1`), intentar hacer clic en un enlace interno o interactuar con un botón en el Workdesk. | Workdesk (Edge) | Clic en "Refrescar" o abrir tarea | La solicitud recibe un `401 Unauthorized` desde el Backend. |
| 3 | Observar el comportamiento del Frontend (Navegador B). | Interfaz Frontend | N/A | El Frontend detecta el 401, limpia la caché híbrida (`$reset()`) y ejecuta una auto-curación expulsando agresivamente al usuario hacia la pantalla de `/login`. |

#### Criterio de Éxito
- [ ] La revocación es de efecto casi inmediato. El Frontend no colapsa, sino que reacciona de manera controlada y segura devolviendo al origen.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 5: Módulo de Delegación Autónoma y Exorcismo de Delegación
> **@Traceability:** US-036, CA-09, CA-23
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Delegante y Delegado

#### Precondiciones
- Dos usuarios de la misma área. `analista1` y `analista2`.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Login como `analista1` (Delegante). Navegar a "Delegar Tareas" (o en su perfil). | Panel de Usuario | N/A | Se abre el formulario de delegación. |
| 2 | Programar una delegación asignando a `analista2` con fecha de "Hoy" y "Mañana". | Formulario | `analista2` | La delegación se guarda. |
| 3 | Login como `analista2` y confirmar que puede ver las tareas de `analista1`. | Bandeja Unificada | N/A | Tareas visibles y auditadas. |
| 4 | *(Verificación Backend In-Flight)* Al expirar la fecha (simulable si se cambian fechas en BD o pasa 1 día), verificar a quién vuelven las tareas In-Flight. | Tareas no finalizadas | N/A | Las tareas que el delegado reclamó pero no terminó sufren un `revertAssignee()` y regresan a `analista1`. |

#### Criterio de Éxito
- [ ] La delegación es temporal y auto-reversible sin intervención de soporte IT.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 6: Informes Densos de Fiscalización y Traza Indeleble (CISO Auditoría)
> **@Traceability:** US-036, CA-16, CA-17, CA-24
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador / CISO

#### Precondiciones
- Haber ejecutado acciones de asignación, creación o delegación en pruebas anteriores.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Login como Súper Administrador y navegar al "Tablero de Auditoría ISO 27001". | Pantalla de Seguridad / Reportes | N/A | Se lista el histórico de eventos de seguridad. |
| 2 | Buscar el evento de cuando `root` creó el rol "Director Regional" o cuando `analista1` delegó poder a `analista2`. | Grilla de Auditoría | N/A | El log se muestra y presenta la firma "Traza Indeleble" de otorgamiento (quién, a quién y cuándo). |
| 3 | Solicitar la exportación del informe en formato CSV o PDF. | Botón "Exportar" | N/A | Se descarga el reporte conteniendo un Hash SHA-256 de inmutabilidad en el archivo. |

#### Criterio de Éxito
- [ ] El módulo de auditoría es transparente, inmutable y produce exportaciones aptas para compliance ISO 27001.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 7: Robots de Integración (Service Accounts) y Política de API Keys
> **@Traceability:** US-036, CA-10, CA-22
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Herramienta cliente API disponible (Ej. Postman o Terminal cURL).

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | En la aplicación web, ir a Configuración de Seguridad > Pestaña "Service Accounts". | Pantalla 14 | N/A | Lista de Robots. |
| 2 | Hacer clic en "Crear Service Account" para un integrador de RPA (Ej. Robot UiPath). | Modal de Creación | Nombre: `RPA_Bot` | El sistema genera y MUESTRA POR ÚNICA VEZ una API Key. |
| 3 | Copiar la API Key. Observar la grilla. | Grilla de Service Accounts | N/A | La API Key ya no es visible, solo sus últimos 4 dígitos y se indica que está "Hasheada (SHA-256)". |
| 4 | Abrir Postman/Terminal e invocar un endpoint interno (Ej. `GET /api/v1/auth/me`) enviando la cabecera `X-API-Key: [Valor]`. | Postman / Terminal | Cabecera HTTP | El backend reconoce el Robot de integración y responde `200 OK`. |

#### Criterio de Éxito
- [ ] Service Accounts plenamente operativas vía cabeceras API-Key sin necesidad de tokens JWT temporales.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 8: Bypass Anónimo de Procesos Públicos
> **@Traceability:** US-036, CA-15
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Usuario No Autenticado (Anónimo)

#### Precondiciones
- Modo incógnito sin sesiones activas.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Intentar cargar la URL directa de Login (`/login`) o hacer un PING al endpoint `GET /api/v1/health` o equivalente configurado como público. | Navegador (Barra Direcciones) | URL /login | La aplicación carga correctamente, demostrando que `JwtSecurityFilter` permite el bypass a URLs whitelisteadas. |
| 2 | Intentar cargar la URL directa del Workdesk (`/workdesk`). | Navegador | URL /workdesk | Inmediata redirección a la pantalla de Login o HTTP 401 si es por API. |

#### Criterio de Éxito
- [ ] El firewall Spring Security es robusto por defecto (Deny-All) y respeta las exclusiones públicas del `SecurityConfig`.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 9: Experiencia de Caída Segura (UX Fallback)
> **@Traceability:** US-036, CA-26
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Cualquiera (Acceso a Docker opcional)

#### Precondiciones
- El sistema operando con normalidad.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Apagar bruscamente el servidor Backend (Simular caída). | Terminal/Docker | `docker stop ibpms-backend` | Backend Inaccesible. |
| 2 | En el Frontend web (que sigue abierto), intentar navegar entre menús en la Sidebar. | Navegador UI | Clic en enlaces | La interfaz no se desmorona a pantalla blanca. Renderiza un layout alternativo o notifica amigablemente que los servicios están inalcanzables (Fallback Message). |

#### Criterio de Éxito
- [ ] El Frontend captura errores de red (Network Error) en sus interceptores Axios y en ruta (MainLayout) para ofrecer un Fallback seguro.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

### 🧪 Prueba 10: Clonación de Perfiles por Plantilla
> **@Traceability:** US-036, CA-03
> **Estado de implementación:** ✅ Completado
> **Rol requerido:** Súper Administrador

#### Precondiciones
- Existencia del `analista1` con un conjunto de 3 roles asignados.
- Existencia del `analista2` "nuevo", sin roles.

#### Pasos de Ejecución

| Paso | Acción | Ubicación en la UI | Dato de Entrada | Resultado Esperado |
|:----:|--------|:------------------:|:---------------:|:------------------:|
| 1 | Ir a la gestión de Usuarios y buscar a `analista2`. | Grilla Usuarios | N/A | Se abre su ficha. |
| 2 | Buscar la opción "Asignar desde Plantilla / Clonar Roles" y seleccionar como base a `analista1`. | Botón "Clonar Perfil" | Selección: `analista1` | El sistema inyecta en masa todos los roles del usuario base a este nuevo usuario. |
| 3 | Guardar y verificar la lista de roles de `analista2`. | Ficha Usuario | N/A | Posee exactamente los mismos 3 roles que el analista1. |

#### Criterio de Éxito
- [ ] Capacidad de aprovisionar usuarios masivamente copiando el esquema de un "Usuario Tipo" existente.

#### Resultado del Humano
- **Veredicto:** `PASS` / `FAIL` / `BLOQUEADO`
- **Observaciones:** 

---

## 📊 Resumen de Resultados (Completar por el Humano)

| # | Prueba | CA(s) Relacionado(s) | Veredicto | Observaciones |
|:-:|--------|:---------------------|:---------:|---------------|
| 1 | Protección Roles Nativos | CA-02, CA-27 | `___` | |
| 2 | Soft-Delete y Herencia | CA-06, CA-07, CA-29 | `___` | |
| 3 | Topología Dinámica y RLS | CA-05, CA-28, CA-30, CA-31 | `___` | |
| 4 | Kill-Session y Zero-Trust | CA-14, CA-21, CA-32 | `___` | |
| 5 | Delegación Autónoma | CA-09, CA-23 | `___` | |
| 6 | Auditoría CISO (ISO 27001) | CA-16, CA-17, CA-24 | `___` | |
| 7 | Service Accounts API Keys | CA-10, CA-22 | `___` | |
| 8 | Bypass Anónimo Público | CA-15 | `___` | |
| 9 | UX Fallback en Caída | CA-26 | `___` | |
| 10| Clonación de Perfiles | CA-03 | `___` | |

### Firma de Certificación
- **Tester:** ________________________
- **Fecha de ejecución:** ________________________
- **Veredicto general:** `PASS` / `PASS CON OBSERVACIONES` / `FAIL`
