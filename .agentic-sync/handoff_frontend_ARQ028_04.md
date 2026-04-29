# Handoff Frontend — ARQ-028-04 | Segregación de Cohesión Mixta

---

## 1. Metadatos y SSOT

| Campo | Valor |
|-------|-------|
| **Iteración** | Deuda Técnica y Refactorización (sprint-6) |
| **Rama Git** | `sprint-6` |
| **Deuda a Cerrar** | **ARQ-028-04:** Entidad con cohesión mixta |
| **Estado** | 🟢 **SIN ACCIONES REQUERIDAS** |

---

## 2. Alineación Arquitectónica

La refactorización **ARQ-028-04** corresponde a una normalización de base de datos y refactorización de entidades JPA exclusivamente en el Backend (separando `ibpms_form_definitions` de `ibpms_form_certifications`).

El Agente Backend ha recibido instrucciones estrictas de **mantener intactos los contratos API (DTOs)** mediante unificación en el Mapper antes de enviar al Controller. Por lo tanto, los componentes Vue que dependen del estado de certificación (como `FormDesignerQACert.vue` o similares) **no sufrirán roturas** ni cambios en sus propiedades reactivas.

---

## 3. Acción

**Instrucción:** Archivar este ticket. No hay código que modificar, compilar ni testear en el Frontend para esta deuda técnica.
