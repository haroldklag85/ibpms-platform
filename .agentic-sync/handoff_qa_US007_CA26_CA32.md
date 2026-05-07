---
name: "Handoff QA - US-007 (Modo Manual DMN) CA-26 a CA-32"
role: "QA"
---

# 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración/Sprint:** Sprint-6
- **Rama de trabajo:** sprint-6
- **User Story:** US-007 (Generador Cognitivo de DMN)
- **Criterios de Aceptación (CAs) a validar:** CA-26, CA-27, CA-28, CA-29, CA-30, CA-31, CA-32
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Backend -> Frontend -> QA

# 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:**
  - `adr_010_testing_pyramid_governance.md`: Se exige validar la integración end-to-end de las vistas DMN en Playwright. Debe ser Zero-Mock contra la base de datos real o ambiente local corriendo en el puerto 8080 (Backend) y 5173 (Frontend).
- **Lineamientos Transversales:** Todo test E2E debe garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Validar explícitamente la presencia de la fila Catch-All inmutable y el tope SRE de 100 filas.

# 3. Rutas Exactas y Contexto Preexistente
- **Pruebas E2E (Playwright):**
  - Ubicación: `frontend/e2e/dmn-manual.spec.ts` (Archivo a crear o actualizar).
- **Vistas a probar:** Pantalla 4 (DMN Editor/Catalog).

# 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**1. CA-26 (Coexistencia UI):**
El test debe abrir el editor DMN y verificar que existen ambos elementos simultáneamente en la pantalla:
`expect(page.locator('.chat-nlp-panel')).toBeVisible();`
`expect(page.locator('.dmn-grid-panel')).toBeVisible();`

**2. CA-28 (Validación FEEL en vivo):**
El test interactúa con una celda y digita sintaxis errónea (ej. texto sin comillas) para verificar que el borde se pone rojo y el botón de Guardar/Publicar se bloquea.
Luego ingresa texto correcto (ej. `> 100`) y verifica que el rojo desaparece y el botón se habilita.

**3. CA-29 (Fila Catch-All):**
`const catchAllRow = page.locator('tr.catch-all');`
`await expect(catchAllRow).toBeVisible();`
`await expect(catchAllRow).toContainText('Revisión Humana');`
`await expect(catchAllRow.locator('.delete-btn')).not.toBeVisible();`

**4. CA-31 (Límite 100 filas):**
Se puede inyectar 99 filas o simular la adición hasta el límite.
Verificar que cuando hay 100 filas:
`await expect(page.locator('button.add-row-btn')).toBeDisabled();`
`await expect(page.locator('text=Límite SRE alcanzado')).toBeVisible();`

**5. CA-32 (Trazabilidad):**
El test finaliza guardando la tabla modificada.
Navega al Catálogo DMN (`/dmn-catalog` o equivalente) y verifica que el badge existe:
`await expect(page.locator('text=Modificada Manualmente')).toBeVisible();`

# 5. Matriz de QA y Testing Atómico
*Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.*

| Test Name | CA Evaluado | Aserción Esperada |
| --- | --- | --- |
| `Simultaneous display of Chat and Manual Grid` | CA-26 | Ambas clases/componentes son visibles en el DOM al mismo tiempo. |
| `Validates FEEL syntax correctly` | CA-28 | Error visual al meter texto sin comillas, éxito al corregirlo. |
| `Catch-all row is permanently locked` | CA-29 | Fila "Revisión Humana" sin botón de eliminación. |
| `Max 100 rows SRE limit enforced` | CA-31 | Botón agregar se deshabilita a los 100 items. |
| `Tags manual modification correctly` | CA-32 | Aparece el texto "Modificada Manualmente" en el catálogo post-edición. |

# 6. Mensaje de Despacho (Comunicación al Agente Especialista)

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_QA.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_QA.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
