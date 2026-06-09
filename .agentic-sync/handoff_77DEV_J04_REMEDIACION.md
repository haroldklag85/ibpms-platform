# Handoff Arquitectónico: Remediación Integral UAT J-04 (Zero-Hallucination)

## 1. Metadatos y SSOT
- **Iteración:** Sprint 6.2 (Sub-rama: `sprint-6/uat-certification`)
- **Tópico:** Certificación Journey J-04 (Amnesia Cero, Resiliencia Backend, Kanban)
- **SSOT Ref:** `docs/uat/casos_uso_uat_j04.md`
- **Flujo de Trabajo:** Backend -> Frontend -> QA (Playwright)

## 2. Alineación Arquitectónica y ADRs
Este handoff se apega estrictamente al **ADR-001 (Hexagonal)** y **ADR-007 (CMMN vs Kanban)**. 
Para resolver el falso positivo E2E causado por los mocks del Frontend, se han instaurado los cimientos relacionales para el tablero Kanban (desacoplado de Camunda) y se ha extendido la tabla de seguridad para admitir Delegación Jerárquica (`manager_id`). El backend proveerá la infraestructura STOMP/WebSocket requerida para cumplir el requerimiento de resiliencia "Ghost Deletion" mediante Event-Driven Architecture.

---

## 3. Despacho Técnico I: Agente Backend 🔧

**Contexto Preexistente:** 
He generado previamente los scripts `32-add-user-hierarchy.sql` y `33-create-kanban-schema.sql` y los inserté en Liquibase. He mapeado `manager` en la entidad `UserEntity`.
Tu deber es levantar la capa de control y comunicación STOMP para el Workdesk interactivo.

### 3.1 Snippets Prescriptivos (El "Qué" y el "Cómo")

**A) Configuración WebSocket STOMP**
*Ruta Esperada:* `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/web/config/WebSocketConfig.java`
```java
package com.ibpms.poc.infrastructure.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/workdesk")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**B) Camunda Task Listener (Ghost Deletion Event Publisher)**
*Ruta Esperada:* `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/camunda/listener/WorkdeskTaskListener.java`
```java
package com.ibpms.poc.infrastructure.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WorkdeskTaskListener implements TaskListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WorkdeskTaskListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        // Ignora eventos que no son de asignación
        if (!TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) return;
        
        Map<String, Object> payload = Map.of(
            "taskId", delegateTask.getId(),
            "assignee", delegateTask.getAssignee(),
            "status", "CLAIMED"
        );
        // Publicar evento STOMP al Frontend
        messagingTemplate.convertAndSend("/topic/workdesk/ghost-deletes", payload);
    }
}
```

---

## 4. Despacho Técnico II: Agente Frontend 💻

**Contexto Preexistente:** 
He neutralizado tu `mockAdapter.ts` eliminando las respuestas falsas de `/workdesk` y `/kanban`. El sistema está ahora expuesto a fallos 404/500 intencionalmente (Fail-Fast). Necesitas suscribirte a la conexión STOMP que el backend expondrá.

### 4.1 Snippets Prescriptivos (El "Qué" y el "Cómo")

**A) Suscripción STOMP en Pinia Store (Ghost Deletion)**
Añadir a tu store de Workdesk:
```typescript
import { Client } from '@stomp/stompjs';

export const useWorkdeskStore = defineStore('workdesk', {
  actions: {
    initWebSocket() {
      const stompClient = new Client({
        brokerURL: 'ws://localhost:8080/ws/workdesk/websocket', // Usar env var en prod
        onConnect: () => {
          stompClient.subscribe('/topic/workdesk/ghost-deletes', (message) => {
            const event = JSON.parse(message.body);
            // Si la tarea fue asignada a otro (Ghost Deletion)
            if (event.status === 'CLAIMED' && event.assignee !== this.currentUser) {
               this.removeTaskWithGhostAnimation(event.taskId);
            }
          });
        }
      });
      stompClient.activate();
    },
    removeTaskWithGhostAnimation(taskId: string) {
       // Logica para añadir clase .is-ghost y purgar del state (CA-13)
    }
  }
});
```

---

## 5. Matriz de QA (Agente de Pruebas / Playwright y JUnit)

| Test Name (Path tentativo) | CA Evaluado | Aserción Esperada |
| :--- | :--- | :--- |
| `GhostDeletionSTOMPTest.java` | CU-J04-15 | `SimpMessagingTemplate` es invocado con payload `CLAIMED` al hacer `claim()` vía API |
| `UserHierarchyJpaTest.java` | PRE-04 | Persistir usuario con `manager_id`, buscar por ID, asegurar que `user.getManager()` no es null. |
| `us002-atomic-claim-concurrency.spec.ts` | CU-J04-15 | Ejecutar claims cruzados entre 2 navegadores. Al menos 1 recibe `409 Conflict`. |
| `kanban-schema.spec.ts` | ADR-007 | El endpoint `/kanban/board` responde 200 apuntando a PostgreSQL puro, sin tocar `rest/engine/`. |

---

## 6. Mensajes de Despacho (Copia y Pega)

**Para delegar al Agente Backend:**
> Asume el rol de Arquitecto Backend. Lee en profundidad `.agentic-sync/handoff_77DEV_J04_REMEDIACION.md`.
> Tu objetivo es materializar la Capa de Comunicación STOMP (Paso 3.A) y el Listener de Camunda (Paso 3.B). 
> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B. No modifiques las entidades DB que ya generé.

**Para delegar al Agente Frontend:**
> Asume el rol de Líder Frontend. Lee en profundidad `.agentic-sync/handoff_77DEV_J04_REMEDIACION.md`.
> Tu objetivo es neutralizar el "Fail-Fast" de la red al implementar el cliente web STOMP dictaminado en el Paso 4.A y asegurar la persistencia Draft (Amnesia Cero). 
> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
