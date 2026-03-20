# Handoff Backend - Iteración 46 (US-038: Fase Final CA-11 a CA-13)

## Propósito
Dotar a la plataforma de un repositorio centralizado de auditoría reactiva (Tablero de Anomalías) donde el Oficial de Seguridad (CISO) o el Súper Admin deban solventar manualmente incidentes críticos como las violaciones de Segregación de Funciones (SoD) y el uso de la cuenta Break-Glass.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-12 (Tablero de Resolución de Anomalías de Seguridad):**
    - Crear el CRUD o controlador pasivo para la Entidad `SecurityAnomaly` (alimentada previamente por los eventos internos de Spring en Iteraciones pasadas).
    - Exponer endpoint `GET /api/v1/security/anomalies?status=OPEN` para listar incidencias.
    - Exponer endpoint `PUT /api/v1/security/anomalies/{id}/resolve` para que el Administrador firme la solución de la anomalía, guardando su ID de solucionador y la fecha de la resolución (Cierre de Ciclo).
* **Ausencia de CA-14 y CA-15:** El alcance de la US-038 finaliza en el CA-13 (el cual es una regla de exclusión).

## Tareas Java (Prioridad 1 - Tarea Bloqueante)
1. Instanciar el `SecurityAnomalyController` y su Servicio asociado.
2. Garantizar que solo los perfiles de `[Super_Administrador]` o `[Oficial_Seguridad]` tengan permiso (Spring Security) de invocar la ruta del Tablero de Anomalías.
