---
name: TDD-First Development Protocol
description: Protocolo de Test-Driven Development que obliga a todo agente desarrollador a escribir el test que falla ANTES de escribir el código de implementación. Aplica para Backend (JUnit/Testcontainers) y Frontend (Vitest). Adaptado de mbcoalson/test-driven-development (Skills Directory 2026).
version: 1.0.0
triggers:
  - "Implementa esta feature"
  - "Desarrolla este CA"
  - "Crea este endpoint"
  - "Agrega esta funcionalidad"
  - "Codea esto"
---

# 🧪 Protocolo TDD-First (Red → Green → Refactor)

## 📌 Propósito
Este skill invierte el flujo natural del agente. En lugar de: **código → tests** (que produce tests que "pasan" pero no prueban nada), fuerza el ciclo: **test que falla → código mínimo → refactoring**. Esto garantiza que cada línea de código existe porque un test la exige.

---

## 🚫 Anti-Patrones Estrictamente Prohibidos

1. **Test-After:** Prohibido escribir la implementación primero y los tests después. Los tests post-hoc tienden a validar "lo que hace el código" en vez de "lo que debería hacer".
2. **Tests que nunca fallan:** Si escribes un test y pasa en verde la primera vez, probablemente no está testeando nada. Revisa.
3. **Tests sin aserciones:** Prohibido escribir tests que solo ejecutan código sin `assertEquals`, `assertThrows`, `expect().toBe()`, etc.
4. **Mock Everything:** Prohibido mockear la capa que estás testeando. Los mocks son para dependencias externas, no para el sujeto bajo test.

---

## ✅ Ciclo TDD por Criterio de Aceptación

Para CADA Criterio de Aceptación (CA) delegado en el Handoff:

### FASE RED 🔴 — Escribir el Test que Falla

1. **Leer el CA** del Handoff y traducirlo a un nombre de test descriptivo:
   - ✅ `should_assign_next_task_to_agent_when_queue_has_pending_items()`
   - ❌ `test1()`, `testCA08()`, `shouldWork()`

2. **Escribir el test completo** antes de tocar el código de producción:

   **Backend (JUnit 5 + Testcontainers):**
   ```java
   @Test
   @DisplayName("CA-08: Asignar siguiente tarea al agente cuando hay items pendientes")
   void should_assign_next_task_when_queue_has_pending() {
       // GIVEN — Estado inicial
       TaskEntity pending = createPendingTask(tenantId);
       taskRepository.save(pending);
       
       // WHEN — Acción bajo test
       Optional<TaskAssignment> result = taskService.assignNext(agentId, tenantId);
       
       // THEN — Verificación del CA
       assertThat(result).isPresent();
       assertThat(result.get().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
       assertThat(result.get().getAssignedTo()).isEqualTo(agentId);
   }
   ```

   **Frontend (Vitest + Testing Library):**
   ```typescript
   describe('CA-08: Attend Next CTA', () => {
     it('should call assignNext and update store when clicked', async () => {
       // GIVEN
       const store = useWorkdeskStore();
       vi.spyOn(workdeskService, 'assignNext').mockResolvedValue(mockAssignment);
       
       // WHEN
       await store.attendNext();
       
       // THEN
       expect(workdeskService.assignNext).toHaveBeenCalledOnce();
       expect(store.currentTask).toEqual(mockAssignment);
     });
   });
   ```

3. **Ejecutar el test** → DEBE FALLAR (Red). Si pasa, el test no prueba nada nuevo.

### FASE GREEN 🟢 — Código Mínimo para Pasar

4. **Escribir el código de producción MÍNIMO** necesario para que el test pase.
   - No optimizar aún.
   - No agregar features que no exigen los tests.
   - No manejar edge cases que no tienen test.

5. **Ejecutar el test** → DEBE PASAR (Green).

6. **Ejecutar la suite completa del módulo** → 0 regresiones.

### FASE REFACTOR 🔵 — Limpiar sin Romper

7. **Refactorizar** el código de producción:
   - Extraer métodos/funciones si hay duplicación.
   - Mejorar naming.
   - Aplicar patrones del ADR correspondiente (Hexagonal, CQRS, etc).

8. **Ejecutar todos los tests de nuevo** → DEBEN SEGUIR PASANDO.

---

## 📏 Convenciones de Naming para Tests

| Capa | Framework | Convención |
|------|-----------|------------|
| Backend Domain | JUnit 5 | `should_[acción]_when_[condición]()` |
| Backend Integration | JUnit 5 + Testcontainers | `should_[resultado]_given_[estado]_when_[acción]()` |
| Frontend Store | Vitest | `it('should [acción] when [condición]')` |
| Frontend Component | Vitest + Testing Library | `it('renders [elemento] with [estado]')` |

---

## 📊 Matriz de Cobertura por CA

Al finalizar el ciclo TDD, el agente DEBE producir esta tabla en su `approval_request_[ROL].md`:

| CA | Test Name | Tipo | Aserción Principal | Estado |
|----|-----------|:----:|--------------------:|:------:|
| CA-08 | `should_assign_next_task_when_...` | Unit | `assertThat(result).isPresent()` | 🔴→🟢 |
| CA-16 | `should_skip_task_with_justification_...` | Integration | `verify(auditLog).save(...)` | 🔴→🟢 |

---

## ⚖️ DIRECTIVAS DE COMPORTAMIENTO

1. **El test es la especificación.** Si no puedes escribir un test para un CA, es que no entiendes el CA. Vuelve a leer el Handoff.
2. **Un test por comportamiento.** No testees 5 cosas en 1 test. Un test = 1 aserción principal.
3. **Nunca falsees un test para que pase.** Si el test no pasa naturalmente, la implementación está mal — no el test.
4. **Los tests de integración usan Testcontainers**, nunca H2 in-memory (violación de ADR-010).

## 🎯 Gatillo de Ejecución
Siempre que un agente reciba un Handoff de Backend o Frontend con CAs a implementar, DEBE aplicar el ciclo TDD-First (Red → Green → Refactor) para cada CA antes de declarar la implementación como completa.
