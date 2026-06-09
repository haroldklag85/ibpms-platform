# 🧠→⚙️ Handoff: Arquitecto Líder → Backend - JAVA
# US-004: Async RabbitMQ Consumer & Refactor Hexagonal (C4-L3)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ BACKEND - JAVA
**Fecha:** 2026-05-25T13:20:00-05:00
**Sprint:** 7 — Iteración 1
**Prioridad:** 🔴 Alta
**Dependencia:** Ninguna

## 📖 LECTURAS OBLIGATORIAS (Ejecutar ANTES de codificar)

```bash
# 1. Política del Enjambre (LEY GLOBAL 0, 1, 2, 3)
cat .cursorrules

# 2. Skill principal del agente receptor
cat .agents/skills/backend_sre_compilation_audit/SKILL.md

# 3. Skills transversales aplicables
cat .agents/skills/clean_code_standards/SKILL.md
cat .agents/skills/zero_mock_enforcement/SKILL.md

# 4. ADRs relevantes
cat docs/architecture/ADR-001-Hexagonal.md
```

> ⚠️ **LEY GLOBAL 3 — Trazabilidad Inversa:** TODO el código nuevo DEBE llevar
> la anotación @Traceability o comentario `// @Traceability: US-XXX, CA-XX`.
> Esto es INNEGOCIABLE.

## 🔬 Diagnóstico del Arquitecto

Existe una deuda técnica crítica en la US-004 que compromete la resiliencia de la mensajería y viola la Arquitectura Hexagonal.

| Violación/Hallazgo | Ubicación | Detalle |
|--------------------|:---------:|---------|
| Ausencia Consumidor | `infrastructure/mq/consumer` | No existe un listener para `QUEUE_INTEGRATIONS_WEBHOOK`, causando pérdida de payloads si Camunda cae. |
| Falsa Capa de Servicio | `application/service/sgdea/SharePointAdapterService.java` | Un adaptador externo de SharePoint está ubicado en la capa de servicios de dominio, violando ADR-001. |
| Cohesión Rota | `infrastructure/web/client/MsGraphWebClientAdapter.java` | Adaptador externo de MS Graph ubicado en la capa Web en lugar de `infrastructure/adapters/external`. |

## 🎯 Instrucciones Quirúrgicas

### Paso 1: Crear el Consumidor de RabbitMQ

**Archivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/mq/consumer/WebhookIntakeConsumer.java`

Implementar el consumidor usando `@RabbitListener` que consuma la cola de integraciones de webhooks.

```java
package com.ibpms.poc.infrastructure.mq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.ibpms.poc.infrastructure.mq.config.RabbitMqTopologyConfig;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@Component
@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})
public class WebhookIntakeConsumer {

    @RabbitListener(queues = RabbitMqTopologyConfig.QUEUE_INTEGRATIONS_WEBHOOK)
    public void consumeWebhookPayload(Map<String, Object> payload) {
        log.info("Procesando payload de webhook de RabbitMQ: {}", payload);
        // Lógica de ruteo hacia UseCase
    }
}
```

### Paso 2: Refactorizar Adaptadores C4-L3

**Archivo 1:** Mover y renombrar `SharePointAdapterService.java` desde `application/service/sgdea/` hacia `infrastructure/adapters/external/SharePointGraphAdapter.java`. Reemplazar la anotación `@Service` por `@Component` e implementar la interfaz del puerto correspondiente. Actualizar imports en las clases que lo consumen.

**Archivo 2:** Mover `MsGraphWebClientAdapter.java` desde `infrastructure/web/client/` hacia `infrastructure/adapters/external/MsGraphWebClientAdapter.java`. Actualizar imports.

## Criterios de Aceptación (DoD)

| # | Criterio | Evidencia |
|---|----------|-----------|
| 1 | `WebhookIntakeConsumer` anotado con `@RabbitListener` | Inspección del archivo para confirmar la clase. |
| 2 | `SharePointGraphAdapter` movido a external | Ejecutar `ls backend/.../application/service/sgdea/` no debe listar el archivo de adaptador. |
| 3 | Trazabilidad inyectada | Revisión de `WebhookIntakeConsumer.java` contiene `@Traceability(US = "US-004", CA = {"CA-6", "CA-8"})` |
| 4 | Build exitoso | `mvn clean package -DskipTests` finaliza con BUILD SUCCESS. |

## 🚦 SECUENCIA DE EJECUCIÓN

1. Mover los archivos de los adaptadores y corregir referencias e imports.
2. Crear `WebhookIntakeConsumer.java`.
3. Validar sintaxis y dependencias.
4. Ejecutar el build: `mvn clean package -DskipTests`
5. Commit: `git add . && git commit -m "refactor(intake): US-004 rabbitmq consumer and hexagonal fix" && git push`

## 📋 Instrucciones para Copiar y Pegar

```text
Asume el rol de ⚙️ BACKEND - JAVA.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat .agents/skills/backend_sre_compilation_audit/SKILL.md
3. cat .agents/skills/clean_code_standards/SKILL.md
4. cat .agents/skills/zero_mock_enforcement/SKILL.md
5. cat docs/architecture/ADR-001.md
6. cat C:\Users\HaroltAndrésGómezAgu\.gemini\antigravity\brain\70f5a9fc-0715-4dbd-b999-23a6c6833584\artifacts\handoff_backend_US004.md

TU MISIÓN:

1. Mueve el SharePointAdapterService y MsGraphWebClientAdapter a la carpeta de infraestructura/external.
2. Crea el WebhookIntakeConsumer con el listener de RabbitMQ tal cual los snippets del handoff.
3. Build/Compile: mvn clean package -DskipTests
4. Commit: git add . && git commit -m "refactor(intake): US-004 rabbitmq consumer and hexagonal fix" && git push

REGLAS INQUEBRANTABLES:
- DEBES inyectar @Traceability en la nueva clase creada.
- PROHIBIDO usar @Transactional en adaptadores o controladores.
- DEBES asegurar que el refactor Hexagonal purgue la capa application/service de los adaptadores de infraestructura mencionados.
```
