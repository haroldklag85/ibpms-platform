# 🏗️ Handoff Técnico QA: US-036 (CA-26 al CA-32)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **US:** US-036 (Identity Governance)
- **CAs:** CA-26, CA-27, CA-28, CA-29, CA-30, CA-31, CA-32
- **Exclusiones:** Funcionalidades V2.
- **SSOT:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md`
- **Flujo de Trabajo:** 3️⃣ QA (Solo inicias cuando Backend y Frontend hayan integrado y hecho push de su trabajo).

## 2. Alineación Arquitectónica y ADRs
- **ADR-010 (Testing Pyramid Governance):** Se prohíbe el uso exclusivo de pruebas E2E. Debes validar que el Backend haya creado pruebas unitarias/integración y el Frontend sus pruebas en Vitest. Tu rol es certificar el flujo completo E2E en Playwright "Zero-Mock" (backend vivo).

## 3. Rutas Exactas y Contexto Preexistente
- `ibpms-platform/frontend/e2e/identity-governance.spec.ts` (Archivo E2E nuevo o a extender).

## 4. Matriz de QA y Testing Atómico (Playwright E2E)
Asegura la Ley de Correspondencia Gherkin (1 test por CA mínimo):

| Test Name (Playwright / JUnit / Vitest) | CA Evaluado | Aserción Esperada |
|-----------------------------------------|-------------|-------------------|
| `shouldFallbackToWelcomePageOnEmptyMenu` | CA-26 | El layout no se rompe; renderiza la página de bienvenida para usuario sin menús. |
| `shouldPreventNativeRoleModification` | CA-27 | El botón de editar para `SUPER_ADMIN` está deshabilitado / El API retorna 400 o 403. |
| `shouldRenderRolesModalWithTabs` | CA-29 | El modal expone la clase `.p-tabview` y permite alternar entre Metadatos y Layout visual. |
| `shouldMergeRolesInclusively` | CA-30 | Usuario con rol A (Dashboard) y rol B (Tasks) ve AMBOS menús. (Backend + E2E) |
| `shouldAutoPurgeMenuOn403` | CA-32 | Tras revocar permiso por BD, al hacer click en otra página el interceptor lanza 403 y el menú se borra de la UI. |

## 5. Snippets Prescriptivos (El "Qué" y el "Cómo")
Debes inyectar tu prueba E2E (Playwright) levantando el entorno backend completo o conectándote a `http://localhost:5173` con el API Spring Boot corriendo en el puerto 8080. 
Ejemplo de aserción (CA-30):
```typescript
test('Union inclusiva de roles', async ({ page }) => {
  // Login con usuario de doble rol (Ej. "multi-role-user")
  await page.goto('/login');
  await page.fill('#username', 'multi-role-user');
  await page.fill('#password', 'ZeroTrust123!');
  await page.click('button[type="submit"]');

  // Validar que se muestren ambos menús
  await expect(page.locator('text="Dashboard"')).toBeVisible();
  await expect(page.locator('text="Bandeja de Tareas"')).toBeVisible();
});
```

---

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar `git stash`.

📚 **SKILLS OBLIGATORIOS:**
- Aplica el skill `.agents/skills/qa_e2e_validation_audit/SKILL.md` para garantizar la Ley de Correspondencia Gherkin (Test vs User Story). Todo CA sin test correspondiente debe reportarse como Cobertura Faltante.
