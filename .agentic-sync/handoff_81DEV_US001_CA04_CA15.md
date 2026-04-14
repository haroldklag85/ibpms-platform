# Handoff Arquitectónico - Iteración 81-DEV
**Épica:** Épica A (Motor Core)
**User Story:** US-001 (Workdesk)
**Criterios de Aceptación:** CA-04 (Delegación UI), CA-15 (Anti-IDOR Security)
**Estado:** Preparado para Ejecución (DEV)
**Fecha:** 2026-04-14 (simulada según matriz)

---

## 1. Contexto y Objetivos de la Iteración
La iteración 81-DEV dota al Workdesk de capacidades de delegación seguras. El sistema debe permitir a un usuario autorizado alternar su vista entre su propio Inbox (Me) o el Inbox de un usuario que le ha delegado tareas (Delegated). Este cambio de contexto en el Frontend (CA-04) debe estar acompañado de una validación estricta en el Backend (CA-15) que rechace peticiones maliciosas (IDOR) donde un usuario intente acceder al contexto de un tercero sin una delegación activa y válida.

## 2. Criterios de Aceptación (Gherkin Base)

### CA-04: Toggle de Delegación en UI
```gherkin
Escenario: Usuario con delegaciones activas cambia el alcance de su Workdesk
  Dado que el "Usuario A" tiene al menos una delegación activa recibida del "Usuario B"
  Y el "Usuario A" se encuentra en la pantalla Workdesk
  Cuando hace clic en el selector "Filtro de Delegación" (delegationFilter)
  Y selecciona el contexto del "Usuario B"
  Entonces el Workdesk debe recargar la grilla
  Y mostrar únicamente las tareas asignadas actualmente al "Usuario B"
  Y añadir un indicador visual en cada fila indicando "Delegado por: Usuario B"
```

### CA-15: Validación Perimetral de Seguridad (Anti-IDOR)
```gherkin
Escenario: Intento de IDOR al listar tareas de un usuario no delegado
  Dado un atacante o usuario malicioso autenticado "Usuario C"
  Cuando el "Usuario C" envía una petición HTTP al backend solicitando las tareas asignadas al "Usuario B"
  Y el "Usuario B" NO ha delegado acceso al "Usuario C"
  Entonces el servicio perimetral intercepta la petición
  Y el backend responde con HTTP 403 Forbidden
  Y registra el intento de acceso no autorizado en logs de auditoría de seguridad
```

---

## 3. Especificación Técnica - Backend

### 3.1. Protección Anti-IDOR (`RbacAuthorizationService.java` & `TaskDelegationService.java`)
- **Implementación Core:** Modificar/Extender validaciones para interceptar peticiones de lectura de tareas donde el `assigneeId` solicitado difiere del usuario en el contexto de seguridad actual (Principal).
- **Proceso de Validación:**
  1. Extraer usuario del Token JWT (Contexto de Seguridad).
  2. Extraer usuario objetivo de la petición de filtrado/búsqueda.
  3. Si `jwtUser != targetUser`, invocar `TaskDelegationService.validateActiveDelegation(jwtUser, targetUser)`.
  4. La validación perezosa (Lazy Evaluation) del `TaskDelegationService` debe garantizar que la delegación no está expirada en milisegundos reales.
  5. Lanzar excepción `AccessDeniedException` si la delegación es inválida/inexistente.

### 3.2. Endpoints y Controladores (`WorkdeskController.java`)
- Exponer/Actualizar endpoint para listar delegantes activos: `GET /api/v1/workdesk/delegators/me` (Retorna lista de usuarios de los cuales puedo ver el inbox).
- Asegurar que el endpoint existente de query del inbox acepte el parámetro opcional `delegatedOwnerId` que active el pipeline Anti-IDOR.

---

## 4. Especificación Técnica - Frontend

### 4.1. Almacenamiento de Estado (`useWorkdeskStore.ts`)
- **Action:** `fetchDelegators()` para poblar la lista de responsables que han delegado en el usuario.
- **State:** `currentDelegationContext: string | null` (almacena el ID del usuario seleccionado).
- **Modificación:** Integrar `currentDelegationContext` como parámetro o cabecera (Header/QueryParam) en la petición WebSocket o REST inicial para notificar al backend del contexto deseado.

### 4.2. Componente Principal (`Workdesk.vue`)
- Convertir el mock/placeholder de `delegationFilter` en un selector reactivo v-model enlazado a `useWorkdeskStore.currentDelegationContext`.
- Añadir manejador `@change` que dispare el limpiado de grilla y recarga de tareas invocando a `store.fetchTasks()`.

---

## 5. Políticas de Compilación y Gobernanza Obligatorias (Ref: SKILL.md)

1. **Protocolo Backend (Java 21 / Spring Boot):**
   - Comando de test: `mvn clean test` o `mvn clean verify -DfailIfNoTests=false`
   - Tolerancia de cobertura: Si hay tests funcionales mutados para Anti-IDOR, deben pasar con status verde absoluto.
2. **Protocolo Frontend (Vue 3 / TypeScript):**
   - Estricto: `npm run type-check` (cero advertencias TS en los bindings del filter de delegación).
   - Bundle check: `npm run build` debe completar sin romper la optimización de chunks.
3. **Restricción de Handoff:** Ningún código pasará a Quality Assurance si el build local detona una excepción de seguridad no manejada.

---

## 6. Siguientes Pasos
Validar este handoff con el arquitecto del sistema/usuario. Una vez aprobado, el agente pasará a fase **EXECUTE** comenzando por los tests de fallos (TDD de IDOR) en backend.
