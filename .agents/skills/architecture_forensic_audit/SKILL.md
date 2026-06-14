---
name: Architecture Forensic Audit & Purge
description: Workflow completo de auditoría forense, detección de contradicciones cruzadas y saneamiento documental del ecosistema arquitectónico. Diseñado para ejecutarse periódicamente o ante cambios estructurales significativos.
version: 1.0.0
triggers:
  - "Audita la arquitectura"
  - "GAP de arquitectura" 
  - "Ejecuta la auditoría forense"
  - "Saneamiento arquitectónico"
  - "Purga documental"
  - "Verifica consistencia de ADRs"
---

# 🏛️ Workflow: Auditoría Forense de Ecosistema Arquitectónico (iBPMS)

## 📌 Propósito
Este workflow ejecuta una auditoría forense completa sobre el directorio `docs/architecture/`, certificando la integridad arquitectónica del proyecto, detectando contradicciones cruzadas entre documentos, y purgando la materia muerta documental. Genera un reporte ejecutivo con acciones correctivas priorizadas.

---

## 🧬 PERSONA
Actúa como un **Enterprise Solutions Architect, Site Reliability Engineer (SRE) y Auditor de Consistencia Documental**. Tu misión es triple:
1. Certificar la integridad arquitectónica del proyecto.
2. Detectar y denunciar **contradicciones cruzadas entre documentos**.
3. Purgar la materia muerta documental para dejar solo el Estado del Arte.

---

## 🔒 PRE-REQUISITO: Contexto Técnico Real (Ground Truth)
**ANTES de iniciar la auditoría, el agente DEBE verificar estos hechos directamente contra el código fuente:**

```
VERIFICAR contra archivos reales:
├── pom.xml         → Java version, Spring Boot version, Camunda version, starters (amqp/kafka)
├── package.json    → Vue version, Vite version, Pinia version, test framework
├── docker-compose* → Servicios reales (PostgreSQL/RabbitMQ/Kafka/Redis)
└── application*.yml → spring.datasource.url (puerto DB), spring.rabbitmq/spring.kafka
```

**Registrar como Ground Truth:**
- **Backend:** [Java X, Spring Boot X.X.X, Motor BPM X.X.X]
- **Frontend:** [Vue X.X.X, Framework CSS, Test framework]
- **Broker Real:** [RabbitMQ / Kafka / Ambos] — Verificar contra `pom.xml` starters
- **Base de Datos:** [Motor + versión + Puerto correcto]
- **Arquitectura:** [Monolith / Modulith / Microservicios] — Verificar contra estructura POM

> [!CAUTION]
> Si algún documento arquitectónico contradice el Ground Truth del código fuente, es una **contradicción crítica (🔴)**.

---

## 📋 FASE 0: Taxonomía y Clasificación de Archivos
**ANTES de analizar contenido**, clasificar cada archivo del directorio `docs/architecture/` en una de estas categorías:

| Categoría | Descripción |
|-----------|-------------|
| **ADR** | Registro de Decisión Arquitectónica (Inmutable salvo actualización formal) |
| **C4** | Diagrama de Modelo C4 (Context, Container, Component) |
| **Plan** | Plan de Implementación o Estrategia |
| **Auditoría/Gap** | Reporte de auditoría o análisis de brechas |
| **Blueprint** | Diseño de pantalla o componente UI |
| **Operacional** | Topología, CI/CD, infraestructura |
| **Propuesta** | Documento propositivo no vinculante |

**Entregable:** Tabla con todos los archivos clasificados. Marcar con ⚠️ los que parezcan redundantes entre sí.

---

## 🔥 FASE 1: Validación contra los 10 Pilares de Excelencia Arquitectónica
Evaluar ADRs, Planes y C4 contra **cada uno** de estos pilares. Entregar veredicto (✅ Cumple / ⚠️ Desviado / ❌ Roto) con evidencia documental:

### Pilares Estructurales (Cómo se organiza el código)
1. **Modular Monolith (Spring Modulith):** ¿Límites de módulos bien definidos? ¿Dependencias circulares?
2. **Hexagonal Architecture (Puertos y Adaptadores):** ¿Lógica de negocio goteando en Controllers? ¿Algún ADR contradice la pureza hexagonal de otro?
3. **DDD (Domain-Driven Design):** ¿Aggregates bien protegidos? ¿Lenguaje ubicuo consistente entre documentos?

### Pilares de Integración (Cómo se comunican los componentes)
4. **API-First (Contract-Driven):** ¿El contrato OpenAPI/Zod es fuente de verdad única?
5. **CQRS (Command Query Responsibility Segregation):** ¿Separación realmente implementada o promesa documental? Contrastar con código real.
6. **Event-Driven & Transactional Integrity:** ¿Se documenta el Patrón Transactional Outbox? ¿Las referencias a brokers son coherentes con el código?

### Pilares de Evolución (Cómo crece el sistema)
7. **Strangler Fig Strategy:** ¿Convivencia V1/V2 tiene Anti-Corruption Layer definida?

### Pilares de Operación (Cómo sobrevive el sistema)
8. **Observabilidad y Trazabilidad (SRE):** ¿IDs de correlación transversales? ¿Herramientas de monitoreo concretas?
9. **Resiliencia Adaptativa (Fault Tolerance):** ¿Circuit Breakers, Bulkheads, Retry Policies, DLQs documentados?
10. **Seguridad por Diseño (Zero-Trust & Tenant Isolation):** ¿RLS, JWT autónomo, Key Vault, TLS 1.2+?

---

## 🔍 FASE 2: Detección de Contradicciones Cruzadas (Cross-Document)
**Fase MÁS CRÍTICA.** Buscar activamente estas categorías:

### A. Contradicciones de Protocolo/Puerto
- ¿Algún diagrama C4 o plan referencia el puerto incorrecto de la BD?
- ¿Hay remanentes de otro motor de BD no purgados?

### B. Contradicciones de Tecnología
- ¿Los documentos mencionan un broker distinto al real del código?
- ¿Se menciona una versión de motor BPM que no coincide con el `pom.xml`?
- ¿Hay menciones a infra Cloud (Kubernetes/AKS) en contextos que deberían ser IaaS (VMs)?

### C. Contradicciones entre ADRs
- ¿Un ADR contradice principios establecidos en otro? (Ej: pureza hexagonal vs entidades JPA directas en dominio)
- ¿Hay saltos en la secuencia de numeración de ADRs?

### D. Gaps Cerrados vs. Gaps Abiertos
- Cruzar gaps reportados contra commits reales del repositorio
- ¿Cuáles ya fueron resueltos por código y siguen reportándose como abiertos?

---

## 🧹 FASE 3: Saneamiento de "Materia Muerta" y Redundancias

### A. Archivos a ELIMINAR (Obsoletos)
| Archivo | Motivo de Eliminación | ¿Contenido Migrable? |
|---------|----------------------|---------------------|

Criterios:
- Gaps cerrados por commits
- Blueprints que no reflejan la UI actual
- Propuestas incorporadas al `implementation_plan.md`
- Reportes cuyas recomendaciones ya fueron aplicadas

### B. Archivos REDUNDANTES (Fusionar)
¿Hay documentos cubriendo el mismo tema desde ángulos distintos?

### C. Archivos que DEBEN ACTUALIZARSE
¿Qué documentos son correctos pero parcialmente desactualizados?

### D. Archivos FUERA DE LUGAR (Reubicar)
¿Hay archivos que pertenecen a subdirectorios especializados? (ej. blueprints → `docs/blueprints/`, CI/CD → `docs/operations/`)

---

## 📊 ENTREGABLE FINAL: "Executive Architectural Audit & Purge Report"
El agente DEBE generar un artefacto `.md` con estas 5 secciones obligatorias:

### Sección 1: Tablero de Salud (10 Pilares)
| # | Pilar | Veredicto | Evidencia / Riesgo |
|---|-------|-----------|-------------------|

### Sección 2: Registro de Contradicciones Cruzadas
| # | Documento A | vs. Documento B | Contradicción | Severidad | Acción Correctiva |
|---|-------------|-----------------|---------------|-----------|-------------------|

### Sección 3: Plan de Purga Documental
| Archivo | Acción (DELETE/MERGE/UPDATE/MOVE) | Justificación |
|---------|-----------------------------------|---------------|

### Sección 4: Preguntas Abiertas de Arquitecto
Lista de decisiones que **NO pueden resolverse por auditoría documental** y requieren decisión explícita del Arquitecto Líder humano.

### Sección 5: Certificación C4 V1 → V2
¿El modelo C4-V2 es evolución lógica del V1 o contiene desviaciones que rompen la continuidad del Patrón Strangler?

---

## ⚖️ DIRECTIVAS DE COMPORTAMIENTO
1. **No seas complaciente.** Si un ADR contradice el protocolo Zero-Trust, denúncialo como riesgo de seguridad.
2. **Cita siempre el archivo y la línea.** Nunca digas "hay una inconsistencia". Di "en `implementation_plan.md` línea 238, se referencia TCP/3306 que contradice PostgreSQL documentado en ADR-009".
3. **Prioriza hallazgos por severidad:** 🔴 Crítico (rompe la arquitectura), 🟡 Medio (genera ambigüedad), 🟢 Bajo (cosmético/documental).
4. **Distingue V1 de V2:** Una mención a tecnología futura en contexto V2 no es error. Una mención a tecnología futura en contexto V1 es una contradicción con el código real.
5. **Verifica contra código fuente SIEMPRE.** Nunca asumas. El `pom.xml`, `package.json` y `docker-compose.yml` son la ley.
6. **No ejecutes correcciones sin aprobación.** Genera el reporte completo, preséntalo al humano, y espera luz verde explícita antes de modificar archivos.

---

## 🎯 Gatillo de Ejecución
Siempre que el usuario solicite: "Audita la arquitectura", "Ejecuta la auditoría forense", "Saneamiento arquitectónico", "Purga documental" o "Verifica consistencia de ADRs", el Agente involucrado **DEBE LEER Y APLICAR este SKILL** imperativamente antes de generar resultados.

## 📜 Historial de Ejecuciones
| Fecha | Agente | Archivos Antes | Archivos Después | Brechas Cerradas | Referencia |
|-------|--------|---------------|-----------------|-----------------|------------|
| 2026-04-08 | Antigravity | 26 | 20 | 9/12 | `architecture_audit_consolidated.md` |
