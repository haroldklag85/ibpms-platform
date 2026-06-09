# ✅ QA Handoff — Backend Sprint 6.2 (Corregido)

Las correcciones obligatorias han sido ejecutadas. El backend está listo para las pruebas Sprint 6.2 J-04.

Por favor, indica al agente QA lo siguiente:

```markdown
# 🧪 Instrucciones para QA - Sprint 6.2 (Journey J-04)

El backend ha sido certificado por Arquitectura y las 3 desviaciones críticas detectadas en auditoría han sido subsanadas. Ahora puedes proceder con la ejecución de los tests E2E y UAT bajo las siguientes premisas:

1. **Datos Semilla Listos (B1):** El archivo `seed-e2e.sql` contiene los usuarios con hash BCrypt validado, SLAs operativos y delegaciones base.
2. **Delegación Validada (B2):** El endpoint de inbox está protegido contra IDOR (HTTP 403) gracias a `TaskDelegationService`. Valida QA CU-J04-NEG-04.
3. **Skipeo de Tareas (B3):** El payload de `SkipReason` ahora requiere estrictamente uno de estos 4: `CLIENT_NO_RESPONSE`, `REQUIRES_DOCUMENTATION`, `OUT_OF_AREA`, `OTHER`. Ejecutar las PRUEBAS C-J04-25 a 28 considerando estos motivos. 
4. **BFF Dinámico (B4):** El payload hacia Camunda está optimizado mediante `FormBffCoreService`.
5. **Máquina de Estados Kanban (B5):**
    - Se agregó el endpoint **`GET /api/v1/kanban/board`** reparando el bloqueo F7.
    - El Frontend ya puede pintar las columnas (por `tenant_id`).
    - Las transiciones prohibidas (Ej: DONE -> IN_PROGRESS o TODO -> BLOCKED) ahora arrojan HTTP 500 legal según el framework de control. Valida explícitamente el resguardo inmutable de DONE (CU-J04-NEG-06).
6. **Feature Toggle (B6):** Modificable por tenant.

**Ejecución:** Puedes lanzar la suite en el pipeline E2E Playwright. Prohibido usar `page.route` para simular respuestas HTTP, este sprint certifica la integración viva con la base de datos de auditoría.
```
