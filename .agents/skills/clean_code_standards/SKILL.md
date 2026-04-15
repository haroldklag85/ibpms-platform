---
name: Clean Code Standards (Java 17+ / TypeScript / Vue 3)
description: Estándares de calidad de código obligatorios para todos los agentes desarrolladores del iBPMS. Cubre naming, estructura, error handling, logging, y anti-slop patterns. Adaptado de davila7/clean-code (19K+ installs, Skills Directory 2026) y customizado para el stack Java/Spring + Vue 3/Pinia.
version: 1.0.0
triggers:
  - "Escribe código"
  - "Implementa"
  - "Refactoriza"
  - "Crea el endpoint"
  - "Agrega la funcionalidad"
---

# ✨ Estándares de Código Limpio (iBPMS)

## 📌 Propósito
Este skill establece las convenciones de calidad de código que TODO agente desarrollador DEBE seguir al escribir código para el iBPMS. El objetivo es prevenir "slop code" (código generado sin criterio que funciona pero es inmantenible).

---

## 🏗️ Sección 1: Naming (el 50% de la legibilidad)

### Backend (Java)
| Elemento | Convención | ✅ Correcto | ❌ Incorrecto |
|----------|-----------|-------------|---------------|
| Clases | PascalCase, sustantivo | `TaskAssignmentService` | `TaskHelper`, `Utils` |
| Interfaces (Ports) | PascalCase, verbo+sustantivo | `TaskAssignmentPort` | `ITaskService` |
| Métodos | camelCase, verbo primero | `assignNextTask()` | `task()`, `processData()` |
| DTOs | PascalCase + sufijo | `TaskSummaryDto` | `TaskData`, `TaskObj` |
| Constantes | SCREAMING_SNAKE | `MAX_RETRY_COUNT` | `maxRetry`, `MAXRETRY` |
| Paquetes | lowercase, singular | `com.ibpms.poc.domain.model` | `com.ibpms.poc.Domain.Models` |

### Frontend (TypeScript/Vue)
| Elemento | Convención | ✅ Correcto | ❌ Incorrecto |
|----------|-----------|-------------|---------------|
| Componentes | PascalCase + `.vue` | `WorkdeskTable.vue` | `table.vue`, `workdesk-table.vue` |
| Composables | camelCase, `use` prefix | `useWorkdeskFilters` | `workdeskFilters`, `filters` |
| Stores | camelCase, `use...Store` | `useWorkdeskStore` | `workdeskStore`, `store` |
| Types/Interfaces | PascalCase | `TaskAssignment` | `ITaskAssignment`, `task_assignment` |
| Variables reactivas | camelCase | `const isLoading = ref(false)` | `const loading = ref(false)` |
| Event emitters | camelCase, `on` prefix | `onTaskCompleted` | `taskDone`, `handleTask` |

---

## 🧱 Sección 2: Estructura de Funciones/Métodos

### Regla de las 3 R: Readable, Reusable, Responsible

1. **Máximo 30 líneas por método/función.** Si excede, extraer sub-funciones.
2. **Máximo 3 parámetros.** Si necesitas más, usa un DTO/objeto de configuración.
3. **Single Responsibility.** Un método hace UNA cosa. Si el nombre tiene "y" (`fetchAndTransform`), dividir.
4. **Early Return.** Validar condiciones al inicio y retornar temprano:

```java
// ✅ CORRECTO — Early return
public Optional<TaskAssignment> assignNext(UUID agentId, UUID tenantId) {
    if (agentId == null) return Optional.empty();
    if (!agentRepository.existsActive(agentId, tenantId)) return Optional.empty();
    
    return taskRepository.findNextPending(tenantId)
        .map(task -> assignToAgent(task, agentId));
}

// ❌ INCORRECTO — Nesting hell
public Optional<TaskAssignment> assignNext(UUID agentId, UUID tenantId) {
    if (agentId != null) {
        if (agentRepository.existsActive(agentId, tenantId)) {
            var task = taskRepository.findNextPending(tenantId);
            if (task.isPresent()) {
                return Optional.of(assignToAgent(task.get(), agentId));
            }
        }
    }
    return Optional.empty();
}
```

---

## 🛡️ Sección 3: Error Handling

### Backend (Java/Spring)
1. **Nunca `catch (Exception e)` genérico.** Atrapar excepciones específicas.
2. **Nunca swallow exceptions.** Si atrapas, logea o relanza.
3. **Usar excepciones de dominio.** Crear `TaskNotFoundException`, `TenantAccessDeniedException`, etc.
4. **Los Controllers devuelven `ResponseEntity` con códigos HTTP correctos:**
   - 200: Éxito con body
   - 201: Creación exitosa
   - 204: Éxito sin body
   - 400: Input inválido del cliente
   - 403: Sin permisos (tenant aislamiento)
   - 404: Recurso no encontrado
   - 409: Conflicto (concurrencia)
   - 500: NUNCA intencional — solo por bugs no manejados

### Frontend (TypeScript/Vue)
1. **Todo `async/await` con `try/catch`** que muestre feedback al usuario.
2. **Nunca `console.log` en producción.** Usar el logger configurado.
3. **Los errores de API se manejan en el Store**, no en el componente.
4. **Mensajes de error para el usuario:** En español, claros, con acción sugerida.

---

## 📝 Sección 4: Comentarios y Documentación

### Regla de Oro: El código se explica solo. Los comentarios explican el POR QUÉ.

```java
// ❌ INCORRECTO — Comenta el QUÉ (obvio del código)
// Asignar la tarea al agente
task.setAssignedTo(agentId);

// ✅ CORRECTO — Comenta el POR QUÉ (no obvio)
// FOR UPDATE SKIP LOCKED previene asignación duplicada bajo concurrencia (NFR-PERF-01)
@Query("SELECT t FROM TaskEntity t WHERE t.status = 'PENDING' ORDER BY t.createdAt FOR UPDATE SKIP LOCKED")
Optional<TaskEntity> findNextPendingForUpdate(UUID tenantId);
```

### Prohibiciones:
- ❌ Comentarios de TODOs sin ticket asociado
- ❌ Código comentado ("por si acaso")
- ❌ Javadoc vacíos (`/** */`)
- ❌ Comentarios de copyright genéricos generados por AI

---

## 🔒 Sección 5: Seguridad (Anti-Patrones Comunes)

1. **Siempre filtrar por `tenantId`** en queries SQL. Sin excepciones.
2. **Nunca concatenar strings en queries SQL.** Usar parámetros preparados.
3. **Nunca exponer entidades JPA en endpoints REST.** Siempre DTO.
4. **Nunca hardcodear secrets** en código o properties. Usar variables de entorno.
5. **Nunca confiar en input del frontend.** Validar TODO en el backend (Bean Validation).

---

## 📊 Checklist de Auto-Revisión Antes de Commit

Antes de hacer `git commit`, el agente DEBE verificar:

- [ ] ¿Los nombres de clases/métodos/variables son descriptivos y siguen la convención?
- [ ] ¿Todos los métodos tienen < 30 líneas?
- [ ] ¿Todos los métodos tienen ≤ 3 parámetros?
- [ ] ¿Hay early returns en vez de nesting profundo?
- [ ] ¿Los errores se manejan con excepciones específicas?
- [ ] ¿Los tests tienen nombres descriptivos con patrón `should_X_when_Y`?
- [ ] ¿No hay código comentado ni TODOs sin ticket?
- [ ] ¿Todas las queries filtran por `tenantId`?
- [ ] ¿No hay secrets hardcodeados?
- [ ] ¿Los DTOs están separados de las entidades JPA?

---

## ⚖️ DIRECTIVAS DE COMPORTAMIENTO

1. **Código limpio > código cleverness.** Prefiere legibilidad sobre ingeniosidad.
2. **Si dudas del nombre, es que el nombre está mal.** Renómbralo.
3. **Cada archivo debe poder leerse de arriba a abajo** como una historia coherente.
4. **El código es para humanos.** La máquina lo ejecuta, los humanos lo mantienen.

## 🎯 Gatillo de Ejecución
Este skill se aplica AUTOMÁTICAMENTE cada vez que un agente escribe código Java o TypeScript/Vue para el iBPMS. No requiere invocación explícita.
