# ADR-013: Estrategia RAG Dual — Segregación Arquitectónica entre Motor de Memoria Conversacional y Base de Conocimiento Documental

**Estado:** Aceptado
**Fecha:** 2026-04-10
**Autores:** Equipo de Arquitectura IA | iBPMS Platform
**Stakeholders:** Arquitecto de IA, Equipo Backend, FinOps
**Relacionado con:** ADR-001 (Hexagonal Architecture), ADR-009 (pgvector), US-054, US-056, US-057

---

## 1. Contexto y Problema

El iBPMS necesita dos capacidades RAG (Retrieval-Augmented Generation) fundamentalmente diferentes:

1. **Memoria Conversacional (US-056):** Persistir y recuperar el historial de conversaciones con los LLMs en procesos BPMN de larga duración. Los datos son efímeros, asociados a una sesión o instancia de proceso, y tienen un ciclo de vida ligado al pipeline de Dreaming (Light → Deep → REM → Expiración).

2. **Base de Conocimiento (US-057):** Indexar y recuperar documentos empresariales (normativas, manuales, políticas, artefactos de proceso) que son persistentes, organizados en Espacios de Conocimiento (Knowledge Spaces), y asignados a agentes IA especializados o roles funcionales.

**Problema:** ¿Deben compartir la misma tabla vectorial, el mismo puerto hexagonal y las mismas políticas de ciclo de vida? ¿O deben segregarse?

---

## 2. Decisión

**Se adopta una Estrategia RAG Dual con segregación completa a nivel de puertos, tablas y políticas de gobernanza, pero con infraestructura compartida a nivel de embeddings y base de datos.**

---

## 3. Diseño Detallado

### 3.1 Puertos Hexagonales Segregados

```
┌─────────────────────────────────────────────────────────┐
│                    CAPA DE DOMINIO                       │
│                                                          │
│  ┌──────────────────────┐  ┌───────────────────────────┐│
│  │ CognitiveMemoryPort  │  │   KnowledgeBasePort       ││
│  │ (US-056)             │  │   (US-057)                ││
│  │                      │  │                           ││
│  │ • ingest()           │  │ • indexDocument()         ││
│  │ • recall()           │  │ • recall()                ││
│  │ • dream()            │  │ • syncKnowledgeSpace()    ││
│  │ • consolidate()      │  │ • removeDocument()        ││
│  │ • expire()           │  │ • status()                ││
│  └──────────┬───────────┘  └───────────┬───────────────┘│
│             │                          │                 │
└─────────────┼──────────────────────────┼─────────────────┘
              │                          │
              │  ┌───────────────────┐   │
              └──┤EmbeddingProvider  ├───┘
                 │Port (US-054)      │
                 │ COMPARTIDO        │
                 └─────────┬─────────┘
                           │
              ┌────────────┴────────────┐
              │    pgvector (ADR-009)   │
              │  ┌────────┐ ┌────────┐  │
              │  │memory_ │ │knowl_  │  │
              │  │vectors │ │vectors │  │
              │  │(US-056)│ │(US-057)│  │
              │  └────────┘ └────────┘  │
              └─────────────────────────┘
```

**Justificación:** Cada pilar tiene responsabilidades, ciclos de vida y políticas de acceso radicalmente diferentes. Compartir un puerto único crearía un acoplamiento semántico que violaría el Principio de Responsabilidad Única (SRP).

### 3.2 Modelo de Datos Segregado

| Aspecto | `ibpms_memory_vectors` (US-056) | `ibpms_knowledge_vectors` (US-057) |
|---------|--------------------------------|-------------------------------------|
| **Scope Key** | `session_id` / `process_instance_id` | `knowledge_space_id` / `agent_id` |
| **Ciclo de Vida** | Efímero → Dreaming → Expiración TTL | Persistente → Sincronización incremental |
| **Fuente de Datos** | Transcripciones conversacionales | Documentos empresariales (PDF, DOCX, etc.) |
| **Acceso** | Restringido a la sesión/proceso que generó los datos | Compartido por múltiples agentes con acceso autorizado |
| **Política de GC** | Dreaming (REM → compactación) | Versioning + TTL explícito |
| **Billing Source** | `CONVERSATION_MEMORY` | `KNOWLEDGE_INDEXING` |

**PROHIBIDO:** Insertar chunks documentales en `ibpms_memory_vectors` o chunks conversacionales en `ibpms_knowledge_vectors`. La mezcla de concerns viola la integridad arquitectónica.

### 3.3 Infraestructura Compartida

Los siguientes componentes son **compartidos** entre ambos pilares para evitar duplicación:

| Componente | Responsabilidad |
|-----------|----------------|
| `EmbeddingProviderPort` (US-054) | Generación de embeddings vectoriales. Ambos pilares usan el mismo proveedor y modelo configurado por el Tenant. |
| `TextChunkingService` | Chunking de texto con overlap. US-057 extiende con capacidades header-aware y page-aware. |
| `QueryExpansionService` | Expansión semántica de queries para mejorar recall. |
| Instancia PostgreSQL + pgvector | Misma base de datos, diferentes tablas. |

### 3.4 Flujo de Ensamblaje Dual

Cuando un agente IA necesita contexto para una inferencia LLM, el `AssemblerService` combina resultados de ambos pilares:

```
Prompt Final al LLM:
┌─────────────────────────────────────────────────┐
│ 1. System Prompt (inmutable - US-054 CA-04)     │
├─────────────────────────────────────────────────┤
│ 2. [CONTEXTO DOCUMENTAL]                        │   ← KnowledgeBasePort.recall()
│    - Chunk A (score: 0.92, Manual Técnico p.3)  │      PRIORIDAD ALTA
│    - Chunk B (score: 0.88, Normativa Art. 12)   │
├─────────────────────────────────────────────────┤
│ 3. [CONTEXTO CONVERSACIONAL]                    │   ← CognitiveMemoryPort.recall()
│    - Chunk X (score: 0.85, sesión 2 días atrás) │      PRIORIDAD MEDIA
│    - Chunk Y (score: 0.79, sesión 1 semana)     │
├─────────────────────────────────────────────────┤
│ 4. Memoria Activa (últimos N turnos)            │   ← Sesión actual
│    - Turn[i-3] → Turn[i-1]                      │      PRIORIDAD BAJA (siempre incluida)
├─────────────────────────────────────────────────┤
│ 5. User Prompt (turno actual)                   │
└─────────────────────────────────────────────────┘
```

**Regla de Presupuesto:** Si el `tokenBudget` no alcanza para ambos contextos, el conocimiento documental (Sección 2) tiene **PRIORIDAD** sobre el historial conversacional (Sección 3). El LLM necesita "saber" (normativa, manual) antes de "recordar" (qué se habló antes).

---

## 4. Consecuencias

### Positivas
- **Separación de concerns clara:** Cada pilar evoluciona independientemente.
- **Auditoría granular:** Los billing sources separados permiten FinOps preciso.
- **Escalabilidad independiente:** La base de conocimiento puede crecer sin afectar la memoria conversacional y viceversa.
- **Seguridad granular:** Los Knowledge Spaces tienen su propio modelo de acceso (por agente/rol), independiente del acceso por sesión/proceso.

### Negativas
- **Complejidad moderada:** El `AssemblerService` debe orquestar dos fuentes RAG con ranking unificado.
- **Overhead de mantenimiento:** Dos tablas vectoriales, dos event logs, dos GC jobs.
- **Riesgo de drift:** Si los modelos de embedding divergen entre tablas, la comparación cross-pilar pierde significado (mitigado por la restricción de consistencia en US-057 CA-05).

### Riesgos Mitigados
- **Token overflow:** El presupuesto de tokens con priorización documental evita desbordamientos.
- **Data leakage entre tenants:** RLS aplicado en ambas tablas de forma independiente.
- **Inconsistencia de embeddings:** Restricción de modelo único por Knowledge Space.

---

## 5. Alternativas Consideradas

### 5.1 Tabla Vectorial Unificada (Rechazada)
- **Pro:** Simplifica la infraestructura.
- **Contra:** Mezcla datos efímeros con persistentes, complica el GC, imposibilita políticas de acceso diferenciadas. Un proceso BPMN podría accidentalmente ver chunks documentales de otro agente si los filtros no son perfectos.

### 5.2 Puerto Hexagonal Único (Rechazada)
- **Pro:** Una sola interfaz para todo RAG.
- **Contra:** Viola SRP. Las operaciones de "dreaming" no tienen sentido para documentos. Las operaciones de "sync" no tienen sentido para conversaciones. El puerto se convierte en un God Interface.

### 5.3 Bases de Datos Vectoriales Separadas (Rechazada)
- **Pro:** Aislamiento total.
- **Contra:** Duplica la infraestructura de bd y operaciones. pgvector ya soporta tablas separadas con índices HNSW independientes, que ofrecen el aislamiento necesario sin duplicar la instancia PostgreSQL.

---

## 6. Referencias

- [ADR-001: Arquitectura Hexagonal](./adr_001_hexagonal_architecture.md)
- [ADR-009: Selección de pgvector para Almacenamiento Vectorial](./adr_009_pgvector.md)
- US-054: LLM Plugin Engine (v1_user_stories.md)
- US-056: Memory Core Engine & RAG Conversacional (v1_user_stories.md)
- US-057: Knowledge Base Engine & RAG Documental (v1_user_stories.md)
- OpenClaw Reference: `src/memory-host-sdk/` (código fuente de referencia)
