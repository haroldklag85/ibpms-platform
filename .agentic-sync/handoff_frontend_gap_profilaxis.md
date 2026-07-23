# Handoff: Frontend — Profilaxis Estructural GAPs (Sprint 1 / Code Freeze)

## 1. Metadatos y SSOT
- Sprint: S1 (Code Freeze — solo profilaxis de UI, cero componentes nuevos)
- Pantallas afectadas: Pantalla 11 (Hub Integraciones CRM), Pantalla 15-A (Restricciones PMO)
- Propuesta aprobada: B — Sidebar Fencing
- SSOT de referencia: `scaffolding/workflows/v1_master_layout_policies.md` (Regla 1: Z-Index, Regla 14: FABs)

## 2. Alineación Arquitectónica
- Master Layout Policy Regla 14: Entradas de navegación sin destino funcional
  activo están prohibidas en interfaces B2B.
- ADR-002 (Vue 3): Ningún componente .vue se elimina — solo se condiciona la visibilidad.

## 3. Rutas Exactas y Contexto

### Archivo objetivo — Sidebar
Localizar la entrada de navegación en el sidebar. Buscar el archivo de layout principal:
`frontend/src/components/` o `frontend/src/layouts/` — el componente que renderiza
el menú lateral (probablemente `AppSidebar.vue`, `MainLayout.vue` o similar).

### RESTRICCIÓN ABSOLUTA
- ❌ NO modificar `frontend/src/router/index.ts` — las rutas deben permanecer registradas
- ❌ NO eliminar archivos `.vue` de Pantalla 11 ni 15-A
- ✅ SOLO ocultar la entrada del menú de navegación en el sidebar

## 4. Snippet Prescriptivo — Ocultamiento con HTML Comments

El mecanismo ÚNICO aprobado es comentar el bloque con un comentario explicativo.
NO usar `display:none`, NO eliminar:

```vue
<!-- GAP-4 [US-045] / GAP-6 [US-021]: Oculto hasta Sprint de refinamiento -->
<!-- <SidebarItem icon="..." label="Hub Integraciones" :to="{ name: 'integrations-hub' }" /> -->

<!-- GAP-4 [US-045]: Oculto hasta Sprint de refinamiento -->
<!-- <SidebarItem icon="..." label="Restricciones PMO" :to="{ name: 'domain-restrictions' }" /> -->
```
Comentar el elemento JSX/template para que sea reversible con un solo uncomment en el sprint correspondiente.

## 5. Matriz de Verificación
| Tarea | Verificación | Criterio de Éxito |
|---|---|---|
| Sidebar oculto P11 | Inspección visual sidebar en localhost:5173 | Entrada "Hub Integraciones" no visible |
| Sidebar oculto P15-A | Inspección visual sidebar | Entrada "Restricciones PMO" no visible |
| Router intacto | grep -r "integrations-hub\|domain-restrictions" src/router/ | Rutas presentes |
| Vistas intactas | find src/views -name "*.vue" \| grep -i "integration\|domain" | Archivos intactos |
| Build limpio | npm run build | "✓ built in Xs" sin errores TypeScript |

## 6. Mensaje de Despacho
Ejecutar profilaxis, luego obligatoriamente:
Frontend Build Audit SKILL:
`npm run build` → validar "✓ built in Xs" sin errores
`npm run lint` → warnings OK, errores NO
Solo entonces: `git commit -m "chore(profilaxis): Sidebar Fencing GAPs S1"`
