# Handoff Backend — Remediación OBS-1 (Auditoría I-72-DEV)
## US-039 | Rama: `sprint-3/informe_auditoriaSprint1y2`

---

## Contexto (Orden del Arquitecto Líder)

La auditoría integral de la Iteración 72-DEV para US-039 (CA-4 al CA-8) fue **APROBADA CON OBSERVACIONES**. Se detectó el hallazgo **OBS-1** que requiere tu intervención quirúrgica.

> **Referencia de Auditoría:** `auditoria_integral_us039_iteracion72DEV.md`, sección "Consolidado de Hallazgos".

---

## Hallazgo Asignado

### OBS-1 — Validación Condicional de `panicJustification` (Severidad: 🟡 Menor)
**CA afectado:** CA-8
**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/rest/dto/GenericFormSubmitRequest.java`
**Línea afectada:** L26 (`@Size(min = 20)`)

**Problema:**
La anotación `@Size(min = 20)` en el campo `panicJustification` se ejecuta por Bean Validation **siempre que el campo no sea null**, incluso cuando `panicAction` es `null`. Esto significa que si un usuario envía `panicJustification: "nota corta"` sin `panicAction`, la validación rechazaría la petición con HTTP 400 innecesariamente.

Actualmente la validación del Service en `GenericFormService.java` L100-103 actúa como doble gate y mitiga el riesgo funcional, pero la anotación Bean Validation en el DTO es técnicamente incorrecta para este caso condicional.

**Solución Exacta:**
Reemplazar `@Size(min = 20)` por una **validación condicional** usando un grupo de validación personalizado o, más pragmáticamente, eliminando la anotación `@Size` del DTO y delegando esa regla 100% al Service layer (donde ya se valida correctamente de forma condicional).

**Opción A (Pragmática — Recomendada):**
```java
// GenericFormSubmitRequest.java — ANTES:
@Size(min = 20, message = "panicJustification must be at least 20 characters if provided")
private String panicJustification;

// GenericFormSubmitRequest.java — DESPUÉS:
@Schema(description = "Justification for Panic Action (validated conditionally in service layer)", nullable = true)
private String panicJustification;
```

**Opción B (Custom Validator — Solo si el equipo prefiere pureza Bean Validation):**
Crear `@ValidPanicJustification` como constraint custom que valide los 20 chars solo cuando `panicAction != null`.

**Decisión del Arquitecto:** Implementar **Opción A** (eliminar `@Size`, documentar en `@Schema` que la validación es condicional en el service).

---

## Verificación Obligatoria
1. Compilar exitosamente: `mvn clean compile -pl ibpms-core`
2. Ejecutar tests existentes: `mvn test -pl ibpms-core -Dtest=GenericFormIntegrationTest`
3. Verificar que el test `testCa8_CancelledRequiresJustification` sigue pasando (la validación del service protege).

---

## Restricciones Arquitectónicas
1. **Cambio mínimo**: Solo modificar 1 línea en `GenericFormSubmitRequest.java`.
2. **No tocar** `GenericFormService.java` — la validación condicional en L100-103 es correcta y DEBE permanecer.
3. **No usar** `git stash`. Solo `git commit` + `git push`.
4. **Convención de commit:** `fix(US-039): OBS-1 remove unconditional @Size from panicJustification`

---

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Este cambio es de **ejecución directa** — NO requiere fase PLANNING dado que es una corrección de 1 línea con instrucciones exactas.
> 2. Ejecuta el cambio, compila, verifica tests, y haz `git commit` + `git push`.
> 3. Al finalizar, graba tu confirmación de cierre en `.agentic-sync/approval_request_backend.md` indicando: `OBS-1 REMEDIADA — commit: <hash>`.
> 4. Dile al Humano: *"Humano, he remediado OBS-1 y registrado el cierre en `.agentic-sync/approval_request_backend.md`. Entrégale este mensaje al Arquitecto Líder."*
