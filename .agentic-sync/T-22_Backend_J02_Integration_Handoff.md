# 🧠→⚙️ Handoff: Arquitecto Líder → Backend
# T-22: Integración Backend J-02 (Low-Code Ecosystem)

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** ⚙️ Backend
**Fecha:** 2026-05-13
**Sprint:** 7 — Iteración 7.2
**Prioridad:** 🟡 Media (Ensamblaje Funcional)
**Dependencia:** Ninguna (Ejecución en paralelo a T-20.5).

---

## 1. Resumen Ejecutivo (El "Por Qué")
Iniciamos la Iteración 7.2. El objetivo es ensamblar el Journey J-02: El ecosistema Low-Code (IDE de Formularios US-003, BPMN Designer US-005, DMN Intelligence US-007). Estos módulos ya existen de manera aislada con coberturas superiores al 94%.
Tu misión en Backend es **asegurar la conectividad (plumbing)**. Debes verificar, exponer y conectar los endpoints REST necesarios para que desde un entorno de Base de Datos VACÍA, el usuario pueda guardar un Formulario, desplegar un XML BPMN y guardar reglas DMN. No se requiere desarrollar lógica de negocio compleja, solo garantizar que los endpoints de guardado/despliegue cruzado existan, estén securizados y no arrojen errores CORS o de mapeo DTO.

---

## 2. Definición de Hecho (DoD - AP-02)
- ✅ Los endpoints de guardado de US-003 (Formularios), US-005 (BPMN) y US-007 (DMN) están expuestos, probados y accesibles.
- ✅ No existen *mocks* en los controladores (100% Zero-Mock V2). La persistencia ataca directamente la base de datos a través de JPA/Hibernate o el Engine de Camunda.
- ✅ Los controladores respetan la arquitectura Hexagonal (Inbound Adapters invocando Application Services).
- ✅ Ley Global 3 (Trazabilidad): `// @Traceability: Integración Backend J-02 (T-22)`.
- ✅ Compilación exitosa (`mvn clean compile`).

---

## 3. Contexto Mandatorio (ADRs y Leyes Globales)
*   **Zero-Mock V2 (ADR-010):** Está PROHIBIDO retornar JSONs quemados o quemar datos en el controlador. La base de datos estará vacía; el sistema debe soportar la creación desde cero.
*   **Ley Global 3:** Comentar todos los ajustes de integración.

---

## 4. Dependencias Técnicas Previas
- Liquidbase configurado para instanciar las tablas vacías de Formularios, DMN y Modelos BPMN.

---

## 5. Plan de Acción (Action Plan)
1.  **Auditoría de Controladores:** Revisa los controladores correspondientes al IDE de formularios, el BPMN Model y el DMN Engine.
2.  **Validación de Payload:** Asegúrate de que los DTOs que reciben el JSON del formulario y el XML del BPMN/DMN estén correctamente formateados.
3.  **Remediación de Plumbing:** Si falta algún endpoint para el guardado cruzado o el listado de recursos creados, impleméntalo siguiendo la arquitectura Hexagonal.
4.  **CORS y Seguridad:** Verifica que `@PreAuthorize` permita el guardado a los roles de Administrador/Modelador.

---

## 6. Estrategia de Testing (Validación)
- Unit Testing / MockMvc (sin apagar seguridad perimetral) para verificar que enviando un JSON válido, el endpoint devuelve HTTP 201 Created o 200 OK y persiste el dato.

---

## 7. Agnostic Handoff Payload (Copia y Pega esto al Agente Backend)
```text
Asume el rol de ⚙️ Agente Backend.

ANTES DE HACER CUALQUIER COSA, lee obligatoriamente estos archivos en este orden exacto:

1. cat .cursorrules
2. cat ibpms-platform/.agents/skills/backend_java_spring/SKILL.md
3. cat ibpms-platform/.agentic-sync/T-22_Backend_J02_Integration_Handoff.md

TU MISIÓN:

1. Inicia el ensamblaje del ecosistema Low-Code (J-02). Debes exponer y asegurar la conectividad de los endpoints de guardado y despliegue para US-003 (Formularios), US-005 (BPMN) y US-007 (DMN).
2. Política Zero-Mock V2 Estricta: La base de datos estará vacía (creación desde cero). Está PROHIBIDO devolver mocks o "stub objects" en los controladores. Conecta los Inbound REST con los Services/Repositories reales.
3. Tu enfoque es "plumbing": asegurar que el Frontend pueda hacer POST/PUT de los artefactos visuales y el Backend los persista sin errores de DTO, CORS o RBAC.
4. Inyecta trazabilidad: `// @Traceability: Integración Backend J-02 (T-22)`.
5. Valida compilación y realiza commit: `feat(core): conexion de endpoints de guardado para ecosistema lowcode j-02 [T-22]`.
```
