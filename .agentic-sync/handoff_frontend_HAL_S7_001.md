# 🏗️ Handoff: Frontend - Resolución de HAL-S7-001 (Fuzzer UUID)

## 1. Metadatos y SSOT
- **Iteración/Sprint:** sprint-7/bugfix-uat
- **User Story:** US-003
- **Hallazgo:** HAL-S7-001 (Campo file/signature exige UUID en validación Zod pero Fuzzer envía "Dummy Data")
- **Path del SSOT:** `docs/requirements/epics/epic_B_formularios_bpmn.md`
- **Flujo de Trabajo:** Frontend -> QA

## 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs:** Cumplimiento con ADR-006 (Esquema de UI) y validación estructural Zod.
- **Lineamientos Transversales:** Se debe garantizar la fricción nula en QA automatizado y manual. La generación automática del Fuzzer ("Autocompletar Happy") debe generar un Payload sintácticamente compatible con el esquema Zod de salida sin depender de interacciones humanas. Dado que el esquema requiere `.uuid()` para `file` y `signature`, el generador de Mock debe producir un UUID por defecto.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo a Modificar:** `frontend/src/stores/useFormDesignerStore.ts`
- **Contexto:** Ubicar las funciones `generateMockPath` y `generateVitestSpec`. En ambas, existe una estructura condicional `if (f.type === '...')` que asigna valores por defecto. Cuando es cadena de texto, actualmente cae en el `else` y asigna `'Dummy Data'`.

## 4. Snippets Prescriptivos
Debes añadir la condición específica para `file` y `signature` en ambas funciones.

**Modificación en `generateMockPath`:**
```typescript
            if(f.type === 'number' || f.type === 'timer') mock[key] = 42;
            else if(f.type === 'checkbox') mock[key] = true;
            else if(f.type === 'email') mock[key] = 'test@example.com';
            else if(f.type === 'url') mock[key] = 'https://example.com';
            else if(f.isMultiple) mock[key] = ['Option1'];
            else if(f.type === 'file' || f.type === 'signature') mock[key] = '550e8400-e29b-41d4-a716-446655440000';
            else mock[key] = 'Dummy Data';
```

**Modificación en `generateVitestSpec`:**
```typescript
            if(f.type === 'number' || f.type === 'timer') specStr += `      ${key}: 42,\n`;
            else if(f.type === 'checkbox') specStr += `      ${key}: true,\n`;
            else if(f.type === 'email') specStr += `      ${key}: 'test@test.com',\n`;
            else if(f.type === 'url') specStr += `      ${key}: 'https://test.com',\n`;
            else if(f.isMultiple) specStr += `      ${key}: ['Option1'],\n`;
            else if(f.type === 'file' || f.type === 'signature') specStr += `      ${key}: '550e8400-e29b-41d4-a716-446655440000',\n`;
            else specStr += `      ${key}: 'Dummy Data',\n`;
```

## 5. Matriz de QA y Testing Atómico
No es necesario un nuevo archivo de test, simplemente el `npm run build` confirmará que TypeScript asimila la lógica correctamente, y el Fuzzer funcionará adecuadamente al ser utilizado por QA.

## 6. Mensaje de Despacho
Humano, por favor copia y pega el siguiente mensaje para el Agente Frontend:

> **Agente Frontend**, inicia la implementación de este handoff ubicado en `.agentic-sync/handoff_frontend_HAL_S7_001.md` para resolver el hallazgo HAL-S7-001. Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
