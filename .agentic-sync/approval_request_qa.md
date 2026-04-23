# Solicitud de Revisión: QA E2E (US-036)

**De:** Agente QA
**Para:** Arquitecto Líder
**Rama:** `sprint-6/uat-certification`
**US:** US-036 (Identity Governance) - CA-26 al CA-32

Estimado Arquitecto Líder,

He finalizado la etapa de `PLANNING` para la implementación de las pruebas E2E exigidas en la historia US-036. Siguiendo estrictamente las directrices del ADR-010 y el handoff, he diseñado la siguiente matriz de cobertura (Ley de Correspondencia Gherkin) que será plasmada en `frontend/e2e/identity-governance.spec.ts`:

1. **CA-26:** `shouldFallbackToWelcomePageOnEmptyMenu` - Valida que un usuario sin menús asignados vea un Dashboard base neutral sin errores.
2. **CA-27:** `shouldPreventNativeRoleModification` - Valida la inmutabilidad de roles como `SUPER_ADMIN` bloqueando checkboxes en la UI.
3. **CA-29:** `shouldRenderRolesModalWithTabs` - Confirma el uso de Tabs para segmentar la creación de roles (Información Básica vs Topología).
4. **CA-30/CA-20:** `shouldMergeRolesInclusively` - Verifica la unión matemática de menús al poseer múltiples roles.
5. **CA-32:** `shouldAutoPurgeMenuOn403` - Confirma que ante un 403, el interceptor de Axios purga Pinia y emite el Toast correspondiente.

Se ejecutará todo sobre el entorno backend local levantado (Zero-Mock), acatando las reglas del ecosistema de testing actual. 

Solicito su **VEREDICTO FORMAL** (Aprobación) para transicionar a la etapa de `EXECUTION` y codificar/commitear estos escenarios en el repositorio.
