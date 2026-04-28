# Handoff Backend — ARQ-028-04 | Segregación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (Iteración 5 / sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta (`FormDefinitionEntity.java`) |
| **Dependencia** | Ejecutar DESPUÉS de que Infra/BD haya generado el changeset de Liquibase |

---

## 2. Contexto Arquitectónico
Actualmente, `FormDefinitionEntity.java` mezcla campos del diseño base (JSONB, hash) con campos de la certificación de QA (`isQaCertified`, etc.). El equipo de Infraestructura ha separado estos últimos en una nueva tabla `ibpms_form_certifications`. Tu deber es actualizar la capa JPA y de dominio.

---

## 3. Instrucciones de Implementación

### Tarea 1: Refactorizar Entidades JPA
1. Modifica `FormDefinitionEntity.java`: Elimina los campos `isQaCertified`, `certifiedSchemaHash`, `certifiedBy`, y `certifiedAt`.
2. Crea `FormCertificationEntity.java` en `com.ibpms.poc.infrastructure.jpa.entity`:
   - Mapeada a `@Table(name = "ibpms_form_certifications")`.
   - Incluye `id` (UUID), `formDefinitionId` (UUID) o un `@OneToOne` hacia `FormDefinitionEntity`.
   - Incluye los campos de certificación extraídos de la otra clase.

### Tarea 2: Actualizar Puertos y Adaptadores
La lógica de certificación en `FormCertificationService.java` (o en su Adaptador JPA asociado) ya no debe hacer `.save()` sobre `FormDefinitionEntity` modificando su flag booleano, sino que debe persistir un nuevo registro en `FormCertificationEntity` utilizando un nuevo `FormCertificationRepository`.
- Si el contrato del puerto `FormDefinitionPort` retorna/recibe flags de certificación, adapta el `JpaAdapter` asociado para que lea/escriba en la nueva tabla `ibpms_form_certifications` ensamblando el Domain Model correcto.

### Tarea 3: Mantener el Contrato API Intacto
Asegúrate de que los objetos DTO que viajan hacia el Frontend (por ejemplo, al solicitar detalles del formulario) sigan conteniendo el estado de certificación. Esto significa que el *Mapping* (Mappers) deberá unificar los datos de ambas tablas antes de enviarlos, para **NO ROMPER AL FRONTEND**.

---

## 4. Criterios de Aceptación
- [ ] `FormDefinitionEntity` no contiene campos de QA.
- [ ] Existe `FormCertificationEntity`.
- [ ] `mvn clean test` pasa exitosamente.
- [ ] El API REST (`FormCertificationController`) devuelve la misma estructura JSON de siempre.

> ⚠️ Notifica finalización a QA para certificación.
