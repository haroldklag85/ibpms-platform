# Análisis Funcional Definitivo: US-045 (Restricciones de Dominio Ágil y Documental System Limits)

## 1. Resumen del Entendimiento
La US-045 provee los "Hardware Limits" de salvataje funcional a nivel de base de datos y UX. Previene el caos cognitivo (Ej: Muchas columnas Kanban), estandariza los Time-To-Live de los PDF externos, configura el "Deshacer" transaccional, y oficializa el Data Wallet "JSONB" para formularios ligados a tarjetas ágiles.

## 2. Objetivo Principal
Empoderar al PMO con límites matemáticos inviolables que protejan al sistema del mal uso de los usuarios operativos y de los Scrum Masters (como abrogar tableros Kanban infinitos), y establecer el patrón CQRS para tableros CMMN Cero.

## 3. Alcance Funcional Definido
**Inicia:** Edición de parámetros globales en Pantalla 15.A.
**Termina:** El límite es interceptado en vivo (Ej: Fracasa el drag-and-drop Kanban, el enlace SGDEA expira, o la UI posterga el post al Backend).

## 4. Lista de Funcionalidades Incluidas
- **Barreras Visibles Kanban (CA-4272):** Control `Kanban_Max_Columns` (UI Horizontal Scroll preventions).
- **Grace Period Configurable (CA-4277):** Ventana de "Aceptar / Deshacer" variable al crear Intake Cards.
- **URL Efímeras SGDEA (CA-4282):** TTL de "Review Mode" temporal sobre S3 pre-signed.
- **Persistencia Tabular JSONB para Formularios Ágiles (CA-4287):** Relega Camunda. Crea una tabla directa JPA/MySQL que posee una bolsa `JSONB` donde Zod escupe sus validadores sin crear un millón de One-To-Many DTOs, maximizando la agilidad de los FormBuilders locales.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **División Cognitiva del Paradigma de Tareas (GAP Arquitectónico Oculto ⚠️ CA-4287):** El CA estipula que la "arquitectura ágil rechaza a Camunda y guarda todo en JPA Relacional". Esto tiene un valor altísimo de "Speed-to-Market" para casos ligeros. Pero siembra un infierno de consolidación: Habrá Tareas Vivas en "Zeebe/Tasklist" (Vía Workflow/BPMN) y Tareas Vivas en "ibpms_kanban_tasks" (Vía Kanban Fast). Si el CISO pide cruzar "Todo lo que María está haciendo hoy", se requerirá un Aggregation Layer monumental (CQRS/Read Model). El FrontEnd Multi-Rol de la US-038 exigía una vista única, este CA hace temblar la unificación.
- **Compatibilidad JSONB de MySQL:** La mención a `JSONB` es propia del Dialecto PostgreSQL (Postgres > 9.4). MySQL soporta el data-type `JSON`, pero su parser indexado Mapea distinto en Hibernate (Requiere librería `hypersistence-utils` de Vlad Mihalcea en el contexto de Spring Boot JPA). Documentar este Risk Framework.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Límite de carga de MBytes (Upload Limits) - Faltante.

## 7. Observaciones de Alineación o Riesgos
La existencia de esta User Story corrobora al 100% que el producto no es sólo "Camunda con interfaz", sino un verdadero iBPMS Multi-Paradigma (Workflow Estricto C8 + Agile Kanban Fast CMMN Cero) con Form Builder ubicuo en ambos carriles paralelos.
