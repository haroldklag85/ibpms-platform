# Handoff Backend — US-005, CA-05

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-05 (Obligatoriedad de Nomenclatura de Instancia - ID Único)
> **Estado:** Delegado para Implementación

---

## 1. Alineación Arquitectónica e Infraestructura

- **Alineación:** ADR-001 (Hexagonal Architecture). La lógica de validación reside en el adaptador de infraestructura de Camunda (`CamundaBpmnValidationAdapter`), que implementa el puerto `BpmnValidationPort`.
- **Riesgo:** Evitar romper validaciones colaterales en el validador BPMN.

---

## 2. Requerimientos Técnicos

### R1. Modificar el mensaje de validación de nomenclatura
En el archivo `backend/ibpms-core/src/main/java/com/ibpms/poc/infrastructure/adapters/CamundaBpmnValidationAdapter.java` (aproximadamente en la línea 206), modifique el mensaje de error de validación cuando falta la propiedad `ReglaNomenclatura`.
- **Actual:** `"Debe definir cómo se llamarán los casos de este proceso (Propiedad: ReglaNomenclatura)."`
- **Esperado:** `"Debe definir cómo se llamarán los casos de este proceso."`

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir o mantener el comentario de trazabilidad en la línea correspondiente o en el bloque de código modificado:
`// @Traceability: US-005, CA-05`

---

## 3. Directivas de Validación y Calidad

- **Clean Code:** Use nombres descriptivos y mantenga la simplicidad del código.
- **TDD:** Verifique que el test de integración `DeployNomenclatureGovernanceCA05Test` pase en verde tras la modificación.

---

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
