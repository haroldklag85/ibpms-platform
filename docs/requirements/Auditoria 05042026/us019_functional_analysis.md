# Análisis Funcional Definitivo: US-019 (Integración Catálogo CRM - Resiliencia)

## 1. Resumen del Entendimiento
La US-019 formaliza el puente técnico entre el Sistema de Récord Comercial del cliente (Salesforce/Dynamics D365/etc.) y la máquina iBPMS. Permite chupar un Diccionario Corporativo (Árbol de Servicios que se Venden/Soportan).

## 2. Objetivo Principal
Erradicar la doble digitación. Si Gerencia crea un producto "Tarjeta de Crédito Libre", iBPMS lo oferta para reclamos al día siguiente.

## 3. Alcance Funcional Definido
**Inicia:** Importación API OAuth desde el CRM Corporativo.
**Termina:** Poblado de base local `ibpms_service_catalog` blindada por modo "Resilience Caché" en caso de Caída P2P.

## 4. Lista de Funcionalidades Incluidas
- **Modo Standalone (Master Switch) (CA-1):** En ON baja del CRM. En OFF, la IA y los usuarios solo ven el sub-árbol del Low Code Platform interno.
- **Source of Truth Estricto (CA-2, CA-3):** Solo chupa "Dumb Data" (Nombres IDs). Rechaza importar Reglas de Equipo. Sobrescribe modificaciones hechas por Administradores manuales.
- **Gestión Asíncrona de Deletes (CA-4):** Si el CRM destruye el código 505 (Venta Seguro), iBPMS lo tacha lógicamente ocultándolo del UI y del RAG MLOps IA para evitar alucinar productos extintos.
- **Inmunidad In-Flight (Snapshotting) (CA-5):** Los casos vivos creados bajo un Catálogo Extinto no detienen su workflow ni se crashean (Copiaron la metadata comercial en origen).
- **Mapeo Técnico Service-to-Definition (CA-7):** Crucial. Los "Nombres Comerciales CRM" se acoplan manualmente con las `processDecisionKeys` exactas de Camunda XML (Pantalla 6).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Desincronización por Multi-Versionado XML (⚠️ CA-7):** Se obliga a Mapear un Servicio del CRM ("Apertura Crédito") hacia el BPMN Key de Camunda XML. **GAP:** Al publicar versiones de Camunda, el `Process Definition Id` muta (`credito:1` -> `credito:2`), pero la `Process Definition Key` se mantiene. Si la US-019 se encadena unívocamente solo a la KEY, correrá siempre la última versión desplegada. Si los casos comerciales exigen Ejecutar Versiones Históricas Ancladas, el mapping colapsará. Debería poder amarrarse opcionalmente no a la Key, sino al Deployment Tag por compliance (Punto ciego de retrocompatibilidad y versionado).

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Importación Bidireccional de Reglas: Roles o SLA no viajan del CRM hacia acá. RBAC manda localmente.

## 7. Observaciones de Alineación o Riesgos
**Fricción de Banner Asincrono (CA-6):** "Precaución CRM inalcanzable" modo Standalone Caché. Exige creación asíncrona permitida. Todo bien a nivel UI, pero choca con integraciones síncronas de Front-End si el CRM mandatoriamente validaba números de cliente (Customer Identification) en el inicio de un Intake manual.
