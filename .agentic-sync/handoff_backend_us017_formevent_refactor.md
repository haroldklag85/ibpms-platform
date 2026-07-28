# Handoff: Backend — Saneamiento Hexagonal US-017 FormEvent ⚙️

## 1. Metadatos y SSOT
- Sprint: S1 (Tech Debt Profilaxis — Code Freeze activo)
- US: US-017 (CQRS / Event Sourcing)
- Remediación: Violación ADR-001 — @Entity en capa domain/
- Rama: sprint-1/profilaxis-architectural-fencing
- SSOT ADR: docs/architecture/adr-001-hexagonal-architecture.md

## 2. Alineación Arquitectónica
- ADR-001 (Hexagonal Architecture): La capa domain/model/ NUNCA
  importa jakarta.persistence.*, org.hibernate.* ni ningún SDK externo.
  FormEvent viola esta ley. Esta remediación la restaura.
- Consecuencia: FormCompletionService y FormEventRepository (puerto)
  no deben cambiar — solo cambia el adaptador JPA.

## 3. Rutas Exactas y Contexto

Archivo a PURIFICAR (eliminar JPA):
  src/main/java/com/ibpms/poc/domain/model/FormEvent.java

Archivo a CREAR (nueva entidad JPA):
  src/main/java/com/ibpms/poc/infrastructure/jpa/entity/FormEventEntity.java

Archivo a MODIFICAR (agregar mapper):
  src/main/java/com/ibpms/poc/infrastructure/persistence/FormEventRepositoryJpa.java
  (No está en infrastructure/jpa/ — está en infrastructure/persistence/)

Puerto existente que NO se toca:
  src/main/java/com/ibpms/poc/domain/port/FormEventRepository.java

## 4. Snippets Prescriptivos

### Snippet A — FormEventEntity.java (CREAR en infrastructure/jpa/entity/)
```java
@Entity
@Table(name = "form_event_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "user_id")
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
```

### Snippet B — FormEvent.java POJO puro (MODIFICAR domain/model/)
```java
// SOLO Lombok permitido. Cero imports jakarta.*, hibernate.*
@Value   // inmutable por diseño CQRS
@Builder
public class FormEvent {
    UUID eventId;
    EventType eventType;
    String taskId;
    String processInstanceId;
    String userId;
    String payloadJson;
    String idempotencyKey;
    Instant createdAt;
}
```

### Snippet C — Mapper en FormEventRepositoryJpa.java (MODIFICAR)
```java
// Agregar estos métodos privados al adaptador existente:

private FormEventEntity toEntity(FormEvent domain) {
    return FormEventEntity.builder()
        .eventId(domain.getEventId())
        .eventType(domain.getEventType())
        .taskId(domain.getTaskId())
        .processInstanceId(domain.getProcessInstanceId())
        .userId(domain.getUserId())
        .payloadJson(domain.getPayloadJson())
        .idempotencyKey(domain.getIdempotencyKey() != null ? domain.getIdempotencyKey().toString() : null)
        .createdAt(domain.getCreatedAt())
        .build();
}

private FormEvent toDomain(FormEventEntity entity) {
    return FormEvent.builder()
        .eventId(entity.getEventId())
        .eventType(entity.getEventType())
        .taskId(entity.getTaskId())
        .processInstanceId(entity.getProcessInstanceId())
        .userId(entity.getUserId())
        .payloadJson(entity.getPayloadJson())
        .idempotencyKey(entity.getIdempotencyKey() != null ? UUID.fromString(entity.getIdempotencyKey()) : null)
        .createdAt(entity.getCreatedAt())
        .build();
}

// El método save() del puerto debe usar toEntity():
@Override
public FormEvent save(FormEvent domain) {
    FormEventEntity saved = springRepo.save(toEntity(domain));
    return toDomain(saved);
}
```

## 5. Matriz de Verificación
| Tarea | Verificación | Criterio de Éxito |
|---|---|---|
| T2 — Dominio puro | `grep -r "jakarta.persistence" .../domain/model/FormEvent.java` | 0 ocurrencias |
| T2 — Hibernate ausente | `grep -r "org.hibernate" .../domain/model/FormEvent.java` | 0 ocurrencias |
| T1 — Entidad creada | `ls .../infrastructure/jpa/entity/FormEventEntity.java` | Archivo existe |
| T3 — Mapper en adaptador | `grep "toEntity|toDomain" .../persistence/FormEventRepositoryJpa.java` | ≥ 2 ocurrencias |
| Saga intacta | `FormCompletionSagaTest pasa` | BUILD SUCCESS |
| Event store inmutable | `FormEventStoreImmutabilityTest pasa` | BUILD SUCCESS |
| Compilación | `mvn clean compile` | BUILD SUCCESS sin warnings de FormEvent |

## 6. Mensaje de Despacho
Ejecutar en este orden: T1 (crear FormEventEntity) → T2 (purificar
FormEvent) → T3 (agregar mapper en adaptador). Nunca al revés —
el compilador validará que domain/ no tenga JPA antes de que el
adaptador esté listo.

Compilación obligatoria — Backend SRE Compilation Audit SKILL:
`docker compose up -d --build ibpms-core`
`docker compose logs -f ibpms-core`
Validar: "Tomcat started on port(s): 8080"
Luego: `mvn clean test` → BUILD SUCCESS

`git commit -m "refactor(US-017): extract FormEventEntity to infra layer — ADR-001 compliance"`
`git push origin sprint-1/profilaxis-architectural-fencing`
