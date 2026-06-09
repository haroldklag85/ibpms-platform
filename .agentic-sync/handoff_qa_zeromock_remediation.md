# 🏗️ Handoff Consolidado QA — Validación Zero-Mock (Regresión + E2E)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **Objetivo:** Validar que las 15 remediaciones Zero-Mock no generan regresiones y que el scanner pasa limpio.
- **Flujo:** 3️⃣ QA (después de que Backend y Frontend hayan hecho push)

## 2. Protocolo de Ejecución
- **Modo:** Zero-Mock E2E (backend vivo, frontend vivo, sin mocks)
- **Framework:** Playwright
- **Archivo de suite:** `frontend/e2e/zero-mock-regression.spec.ts` [NUEVO]

---

## 3. Escenarios de Prueba Requeridos (10 Tests)

### GRUPO A — Verificación del Scanner

#### TEST-ZM-001: Anti-Mock Scanner Verde
```gherkin
DADO que el agente Frontend completó la remediación
CUANDO ejecuto `node scripts/anti-mock-scanner.js`
ENTONCES el scanner reporta "✅ Anti-Mock scan passed. No violations found."
Y el exit code es 0
```

---

### GRUPO B — Regresión Módulo Modeler

#### TEST-ZM-002: BpmnDesigner carga sin mockRole
```gherkin
DADO que navego al Diseñador BPMN `/admin/modeler/bpmn`
CUANDO el componente monta
ENTONCES los paneles de visibilidad (CA-21) se muestran según el rol real del usuario autenticado
Y NO existe una constante `mockRole` en el DOM reactivo
```

#### TEST-ZM-003: BpmnDesigner catch blocks muestran Toast on error
```gherkin
DADO que el backend de DMN/Forms/Connectors no responde (simular con network interception)
CUANDO el BpmnDesigner intenta cargar los dropdowns
ENTONCES se muestra un Toast con mensaje "Error cargando..." para cada endpoint
Y los arrays de opciones están vacíos (no datos fantasma)
```

#### TEST-ZM-004: InstancesManager consulta al backend real
```gherkin
DADO que navego al Gestor de Instancias con un processId válido
CUANDO el componente monta
ENTONCES se realiza un `GET /api/v1/design/processes/{processId}/instances`
Y las instancias mostradas son las reales del motor Camunda
```

---

### GRUPO C — Regresión Módulo RBAC

#### TEST-ZM-005: GlobalRolesTable sin UUIDs mock
```gherkin
DADO que abro el modal de Asignación Masiva
CUANDO selecciono un rol plantilla
ENTONCES el modal muestra un componente de selección de usuarios reales (UserPicker)
Y NO envía UUIDs hardcodeados '00000000-0000-...'
```

#### TEST-ZM-006: IdentityGovernance conecta Kill Switch al backend
```gherkin
DADO que presiono el botón Kill Switch para un usuario
CUANDO la solicitud se envía
ENTONCES se realiza un `POST /api/v1/admin/users/{id}/kill-session`
Y NO se muestra el toast "Fallback local: Kill Switch emulado"
```

#### TEST-ZM-007: IdentityGovernance exporta CSV real
```gherkin
DADO que presiono "Exportar Matriz CISO"
CUANDO el backend procesa la solicitud
ENTONCES se descarga un archivo CSV con datos reales del backend (no MOCK_CISO_Access_Matrix.csv)
Y el archivo contiene cabeceras reales (PROCESS, ROLE, INITIATE, EXECUTE, etc.)
```

---

### GRUPO D — Verificaciones de Renombramiento

#### TEST-ZM-008: Archivos con placeholders no bloquean el scanner
```gherkin
DADO que los archivos del GRUPO C (IntakeManual, PublicIntake, etc.) usan `placeholder*` en vez de `mock*`
CUANDO ejecuto `node scripts/anti-mock-scanner.js`
ENTONCES el scanner NO detecta violaciones en estos archivos
```

---

### GRUPO E — WorkdeskMockup eliminado

#### TEST-ZM-009: WorkdeskMockup no existe en views
```gherkin
DADO que busco el archivo `src/views/WorkdeskMockup.vue`
CUANDO verifico su existencia
ENTONCES NO existe en `src/views/`
Y SÍ existe en `src/tests/fixtures/WorkdeskMockup.vue`
```

#### TEST-ZM-010: Router no tiene ruta a WorkdeskMockup
```gherkin
DADO que inspecciono `src/router/index.ts`
CUANDO busco referencias a `WorkdeskMockup`
ENTONCES no existe ninguna ruta apuntando a ese componente
```

---

## 4. INSTRUCCIONES OPERATIVAS

1. Inicia en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md`.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.**
3. Guarda tu solicitud de revisión en `.agentic-sync/approval_request_qa.md`.
4. Al grabar el archivo, detente y dile al Humano: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_qa.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera el veredicto. Si aprobado, pasa a `EXECUTION`.

📚 **SKILLS OBLIGATORIOS:**
- Aplica TDD: `.agents/skills/tdd_first/SKILL.md`
- Ejecuta todo contra backend vivo (Zero-Mock): `.agents/skills/zero_mock_e2e/SKILL.md`

> **Commit y Push** obligatorio en `sprint-6/uat-certification`. Queda prohibido usar `git stash`.
