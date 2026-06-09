# 🔵 Handoff Backend — US-025 Fase 2: Corrección UUID MenuTopologyJpaAdapter

> **Emitido por:** Arquitecto Líder  
> **Fecha:** 2026-05-03  
> **Agente Destino:** Backend  
> **Prioridad:** 🟡 P1 — Corregir antes de Fase 3B  
> **Pre-requisito:** Fase 1B completada ✅  
> **Gate de Salida:** `mvn compile` sin errores

---

## Defecto DEF-F1B-01

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/ui/MenuTopologyJpaAdapter.java`  
**Líneas:** 43-44

### Problema
La tabla `ibpms_menu_topology` define `id UUID` y `parent_id UUID`.
El adaptador usa `rs.getLong("id")` y `rs.getLong("parent_id")`, lo cual causa
`ClassCastException` en runtime con el driver JDBC de PostgreSQL porque UUID ≠ Long.

### Código Actual (Defectuoso):
```java
record.id = rs.getLong("id");                                    // L43
record.parentId = rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null;  // L44
```

Y la clase `MenuRecord` usa `Long`:
```java
private static class MenuRecord {
    Long id;        // ← INCORRECTO: la tabla usa UUID
    Long parentId;  // ← INCORRECTO
    ...
}
```

### Código Correcto:
```java
record.id = rs.getObject("id", UUID.class);                      // L43
record.parentId = rs.getObject("parent_id", UUID.class);         // L44
```

Y la clase `MenuRecord` debe usar `UUID`:
```java
private static class MenuRecord {
    UUID id;
    UUID parentId;
    ...
}
```

También actualizar `nodeMap` de `Map<Long, MenuItemDTO>` a `Map<UUID, MenuItemDTO>`.

### Resumen de Cambios
1. `MenuRecord.id` y `MenuRecord.parentId` → tipo `UUID`
2. `rs.getLong()` → `rs.getObject("id", UUID.class)` 
3. `Map<Long, MenuItemDTO>` → `Map<UUID, MenuItemDTO>`
4. Agregar `import java.util.UUID;` si no está

### Gate
```bash
mvn compile  # Sin errores
```

Después de esta corrección, espera aprobación del Arquitecto para Fase 3B.
