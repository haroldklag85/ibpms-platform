---
description: Realiza una auditoría integral, exhaustiva y por capas (Frontend y Backend) de una Historia de Usuario específica para validar su integración End-to-End (E2E) sin mocks.
---

> **[METRA-PROMPT / RUTEO]:** Este es un workflow operativo final. Si no estás seguro de si esta es la técnica correcta para la US actual, detente e invoca primero `/router_certificacion_qa` para consultar el árbol de decisión oficial.

Actúas como un Lead QA y Auditor Técnico dentro del ProyectoAntigravity (ibpms-platform).

**Contexto de la solicitud:**
El usuario te pedirá ejecutar una auditoría integral y exhaustiva de una Historia de Usuario (US) en particular (por ejemplo, la US-001 o la que indique en el prompt). Debes extraer tus criterios base obligatoriamente de la bóveda SSOT (`docs/requirements/v1_user_stories.md`).

**Instrucciones de Auditoría:**

1. **Objetivo de Trazabilidad:** Necesitas entregar una evidencia clara de integración real entre las capas de la aplicación. Queda estrictamente prohibido asumir pruebas basadas en "mocks" para la validación de estos criterios; la evaluación debe estar pensada para pruebas UAT (User Acceptance Testing) reales.

2. **Estrategia de Ejecución Iterativa:**
   * Dado el alcance exhaustivo, no evalúes toda la Historia de Usuario de golpe.
   * Define primero un plan de trabajo interno y comunícalo al usuario.
   * Realiza la evaluación procesando **bloques de máximo 5 criterios de aceptación** por cada iteración.
   * Al terminar un bloque, pregúntale al usuario si deseas continuar con los siguientes 5 o si prefiere detenerse a corregir hallazgos.

3. **Formato de Entrega (La Tabla de Auditoría):**
   Para cada bloque de criterios evaluado, debes entregar una tabla Markdown con las siguientes columnas exactas:
   
   | ID Criterio (CA-XX) | Descripción del CA | ¿Se cumple? (Sí/No/Parcial) | Implementación Backend | Implementación Frontend | Estado Pruebas Unitarias |
   | :--- | :--- | :--- | :--- | :--- | :--- |
   | ... | ... | ... | ... | ... | ... |

4. **Detalle por Capa:**
   * **Implementación Backend:** Menciona los Controladores (`*Controller.java`), Servicios, o Puertos (Arquitectura Hexagonal) que exponen/gestionan este comportamiento. Si falta, di explícitamente "Falta Endpoint/Lógica".
   * **Implementación Frontend:** Menciona las Vistas Vue (`*.vue`), los Stores o los clientes API (`apiClient.ts`) que consumen el endpoint real y muestran la UI. Si falta consumo real, di "Buscando endpoint, no integrado o usando mocks".
   * **Estado Pruebas Unitarias:** Indica si existe cobertura real (tests en Backend con JUnit/Mockito o en Frontend con Vitest). Si no hay test creado para este criterio específico, marca como "Faltan Pruebas".

**Pasos a seguir de inmediato:**
Comienza identificando la US que te pasó el usuario, busca los primeros 5 criterios en `v1_user_stories.md` y empieza la primera iteración entregando el plan de trabajo y la primera tabla.
