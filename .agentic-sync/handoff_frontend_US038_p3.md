# Handoff Frontend - Iteración 46 (US-038: Fase Final CA-11 a CA-13)

## Propósito
Otorgar validación visual de los roles adquiridos vía Microsoft EntraID en la cabecera principal y construir la interfaz reactiva para el cumplimiento del CISO (Tablero de Anomalías).

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-11 (Indicador Tipográfico de Dominio en Cabecera):**
    - **Target:** `MasterHeader.vue`, `TopBar.vue` o componente global equivalente.
    - Leer los "Claims" (Roles) del usuario autenticado.
    - Renderizar un micro-texto o chip discreto junto al avatar/nombre del usuario, resumiendo sus 2 o 3 roles principales de más alta jerarquía (Ej: `Director Comercial | Aprobador VIP`), demostrando que la ingesta del SSO fue exitosa.
* **CA-12 (Tablero de Anomalías de Seguridad):**
    - **Target:** Nueva pestaña dentro de `Pantalla 14` (Admin V1 / Seguridad RBAC).
    - Renderizar una tabla consumiendo `GET /api/v1/security/anomalies`.
    - Pintar de color ROJO las anomalías abiertas (Ej: Intento SoD, Login Break-Glass).
    - Añadir botón de acción `[ ✅ Marcar como Subsanado ]` para invocar el `PUT` de resolución y desaparecer el rojo de la grilla.
* **CA-13 (Postergación de Reset de Password para V2):**
    - Asegurar que la pantalla de `Login.vue` **NO contenga** ningún enlace de "Olvidó su contraseña" que intente detonar flujos de recuperación por correo automático. 
    - En caso de existir, mutarlo temporalmente a un texto pasivo: *"¿Olvidó su clave? Comuníquese con el Administrador IT"*. (Regla de Arquitectura, Cero Código de Lógica).

## Tareas Vue (Prioridad 2 - Puede Iniciar en Paralelo)
1. Modificar el Master Header para exhibir el resumen tipográfico de Múltiples Roles (CA-11).
2. Construir la Grilla Roja de Anomalías y conectarla al endpoint transaccional del CISO (CA-12).
3. Purgar botones de recuperación de contraseña orgánica (CA-13).
