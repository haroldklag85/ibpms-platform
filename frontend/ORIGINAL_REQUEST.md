# Original User Request

## Initial Request — 2026-05-31T19:28:17Z

El objetivo es corregir el bypass de seguridad en la ruta del DLQ Dashboard (Hallazgo 1), asegurando que solo los roles autorizados ('ROLE_ADMIN_IT' y 'ROLE_SUPER_ADMIN') tengan acceso, guiado por TDD.

Working directory: c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend
Integrity mode: development

## Requirements

### R1. Remediación de la Ruta DlqDashboard en router/index.ts
- Modificar la definición de la ruta `DlqDashboard` en `src/router/index.ts` para usar la propiedad `roles` en lugar de `requiredRole`, permitiendo únicamente a los roles `['ROLE_ADMIN_IT', 'ROLE_SUPER_ADMIN']`.

## Acceptance Criteria

### Verificación de Pruebas y Compilación
- [ ] Ejecutar `npx vitest run src/tests/regression_hallazgo1.spec.ts` y comprobar que pase exitosamente (verde).
- [ ] Ejecutar `npm run build` en el frontend y comprobar que compile sin errores.
- [ ] Garantizar que no se hayan modificado aserciones de pruebas históricas (Ley Global 4).
