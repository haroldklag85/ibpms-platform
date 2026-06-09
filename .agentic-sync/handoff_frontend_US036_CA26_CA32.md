# 🏗️ Handoff Técnico Frontend: US-036 (CA-26 al CA-32)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **US:** US-036 (Identity Governance)
- **CAs:** CA-26, CA-27, CA-28, CA-29, CA-30, CA-31, CA-32
- **Exclusiones:** Funcionalidades V2, sub-menús de granularidad fina.
- **SSOT:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md`
- **Flujo de Trabajo:** 2️⃣ Frontend (Debes esperar a que el Backend publique su endpoint).

## 2. Alineación Arquitectónica y ADRs
- **ADR-002 (Vue3 Microfrontends):** Todo el estado global debe gestionarse en Pinia (`useMenuStore`). Prohibido usar `localStorage` para almacenar la topología (Riesgo XSS).
- **CA-31 (Anti-JWT Bloat):** Prohibido leer el layout visual desde el JWT. Debes consumir `GET /api/v1/users/me/menu-layout` post-login.
- **CA-32 (Auto-Curación Zero-Trust):** Un interceptor global de Axios debe escuchar códigos `403 Forbidden`. Si ocurre, debe vaciar el store de menús y recargar el layout.
- **CA-29 (Diseño Limpio Modal de Roles):** Refactorizar la Pantalla 14 (Gestión de Roles) usando pestañas (Tabs) para separar metadatos del role vs. asignación de layout.
- **CA-26 (UX Fallback):** Si el array de menús llega vacío `[]`, mostrar una "Página de Bienvenida" o layout neutral, NUNCA bloquear la UI.

## 3. Rutas Exactas y Contexto Preexistente
- `ibpms-platform/frontend/src/stores/useMenuStore.ts`: Almacenará la matriz dinámica en memoria.
- `ibpms-platform/frontend/src/plugins/axios.ts`: Donde se inyectará la lógica de auto-curación (interceptor 403).
- `ibpms-platform/frontend/src/views/admin/RolesView.vue` (o Pantalla 14 equivalente): Se debe migrar a pestañas (PrimeVue Tabs).

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**Interceptor Axios (CA-32):**
```typescript
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      const menuStore = useMenuStore();
      menuStore.purgeTopology(); // CA-32: Auto-curación
      // Opcional: toast informativo "Permisos revocados"
    }
    return Promise.reject(error);
  }
);
```

**Estructura Tabs Modal de Roles (CA-29):**
```html
<TabView>
    <TabPanel header="Detalles del Rol">
        <!-- Formulario básico -->
    </TabPanel>
    <TabPanel header="Topología Visual">
        <!-- Switches para los 7 menús macro (CA-28) -->
    </TabPanel>
</TabView>
```

## 5. Matriz de QA y Testing Atómico (Vitest)
Debes crear/actualizar los tests unitarios en Vitest para `useMenuStore.spec.ts` y `axiosInterceptor.spec.ts`.
- **Test 1:** Validar que al recibir un error 403 simulado, el array de `useMenuStore.topology` pase a ser vacío (CA-32).
- **Test 2:** Validar que si `topology.length === 0`, el componente de menú renderice el fallback visual (CA-26).

---

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar `git stash`.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
