# 📋 Solicitud de Revisión Arquitectónica V2 - US-036 (Identity Governance)

**Para:** Arquitecto Líder
**De:** Agente Backend (sprint-6/uat-certification)

## Resumen del Plan Propuesto y Subsanaciones (V2)
He actualizado el `implementation_plan.md` integrando estrictamente los dos requerimientos de seguridad innegociables omitidos en la V1:

1. **Omisión Subsanada: Inmutabilidad (CA-27)**: `MenuLayoutService` implementará validaciones duras a nivel de servicio para roles nativos (e.g. `SUPER_ADMIN`). Las asignaciones de estos perfiles estarán mapeadas de forma inmutable; cualquier intento de alteración disparará inmediatamente una excepción de seguridad (AccessDeniedException).
2. **Omisión Subsanada: Inactivación de Caché (CA-32)**: Se ha incorporado explícitamente el uso de `@CacheEvict(value = "menuTopology", key = "#username")`. La capa de servicio ahora no solo cachea, sino que invoca este mecanismo de auto-curación forzosa en cuanto el CISO o administrador realice modificaciones al rol, garantizando la revocación en tiempo real en la siguiente lectura.
3. **Anti-JWT Bloat (CA-31)**: Se implementará `MenuTopologyController` (`GET /api/v1/users/me/menu-layout`) para servir menús dinámicamente.
4. **Unión de Permisos (CA-30)**: Fusión matemática de `Set<String>` para resolver el solapamiento sin duplicados.
5. **TDD (CA-27, CA-30 & CA-32)**: Los tests en `MenuLayoutServiceTest.java` (JUnit 5) incluirán aserciones explícitas para `@CacheEvict` y la imposibilidad de mutar roles nativos.

## Veredicto Solicitado
Por favor, confirmar si las correcciones de inmutabilidad y auto-curación de caché satisfacen la arquitectura esperada para iniciar inmediatamente la codificación bajo la rama `sprint-6/uat-certification`.
