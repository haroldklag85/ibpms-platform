# Handoff Técnico - Frontend - US-036 (CA-12 al CA-16)

## 1. Contexto y Objetivos
Finalizar la interfaz de la Pantalla 14 (Identity Governance) con las funciones de fiscalización, revocación forzosa y configuración de trámites públicos.

**Rama de trabajo:** `DevDavid`
**Iteración:** `05-DEV-DAVID`

## 2. Alineación Arquitectónica
- **ADR-002 (Vue 3):** Uso de componentes SFC, Pinia para estado de reportes y Axios para descarga de binarios.
- **CA-12 Policy:** Los controles de acceso son por formulario/proceso completo. No implementar selectores de campos individuales en Screen 14.

## 3. Especificaciones UI/UX (Criterios de Aceptación)

### CA-14: Botón de Exorcismo (Kill-Session)
- **Ubicación:** Pestaña "Usuarios" -> Ficha de Empleado (Detalle).
- **Componente:** Botón de acción destacado (Rojo/Peligro) `[Revocar Todo y Matar Sesión]`.
- **Acción:** Invocar `POST /api/v1/admin/security/users/{userId}/revoke-session`.
- **Feedback:** Mostrar modal de confirmación "Esta acción expulsará al usuario inmediatamente de todos sus dispositivos. ¿Continuar?".

### CA-15: Switch Trámite Público
- **Ubicación:** Pestaña "Procesos" o dentro de la configuración de cada proceso en la matriz.
- **UI:** Switch/Toggle `[Permitir Trámite Público]`.
- **Lógica:** Al activar, el sistema debe indicar visualmente que el proceso ahora es accesible vía URL anónima.

### CA-16: Reportes CISO (ISO 27001)
- **Ubicación:** Nueva Pestaña "Reportes" en Pantalla 14.
- **Acción:** Botón `[Generar Reporte Matrizal ISO 27001]`.
- **Lógica:** 
    - Llamar al endpoint de descarga.
    - Manejar el stream de descarga para CSV/Excel.
    - Mostrar historial de reportes generados (opcional si el backend lo provee).

### CA-12: Scope Limit
- Asegurar que la matriz de permisos solo permita habilitar/deshabilitar procesos completos, evitando la complejidad de campos individuales (delegado al Pro-Code Builder).

## 4. Estado Global (Pinia)
- Actualizar `rbacStore.js` para manejar la descarga de reportes y el estado de revocación.
- Integrar el manejo de errores 401/403 post-revocación para limpiar el estado local si el admin se auto-revoca (escenario de prueba).

## 5. Pruebas y Validación
- **Vitest:** Mockear la llamada de descarga de archivos y verificar que el Blob se crea correctamente.
- **UX:** Validar que el botón Kill-Session requiere doble confirmación.

## 6. Instrucciones Operativas
Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo en `implementation_plan.md`.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
> 3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_frontend.md`.
> 4. Detente y notifica al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder..."*
> 5. Aplica **Clean Code** (`.agents/skills/clean_code_standards/SKILL.md`).
