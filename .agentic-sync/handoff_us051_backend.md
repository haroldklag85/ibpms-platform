# Handoff US-051: Agente Backend (Sprint 6 — Cierre de Gaps)

**Emitido por:** Arquitecto Líder
**Fecha:** 2026-05-01
**Prioridad:** SHOULD (soporte a Frontend)
**Fuente SSOT:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md` (líneas 562-695)

---

## Tareas:

### 1. CA-34 — Evento [ROLES_UPDATED] en SSE
**Archivo:** La infraestructura SSE que emite eventos de seguridad (endpoint `/api/v1/security/stream`)
**Acción:** Cuando un admin modifica roles parcialmente (sin revocar todo), emitir el evento `[ROLES_UPDATED]` en lugar de `[ROLE_REVOKED]`. El `[ROLE_REVOKED]` debe reservarse exclusivamente para el Kill-Session (desactivación total del usuario).

### 2. CA-36 — Reconciliación de Contrato MenuItemDTO
**Archivo:** `application/dto/ui/MenuItemDTO.java`
**Discrepancia detectada:** El Frontend espera `items` pero el DTO envía `children`. El Frontend espera `label` en items hijos, pero el DTO envía `title`.

**Contrato TypeScript esperado por el Frontend:**
```typescript
interface MenuItem {
    path: string;
    icon: string;
    label: string;   // Backend envía "title"
    roles?: string[];
}
interface MenuGroup {
    title: string;
    roles?: string[];
    items: MenuItem[];  // Backend envía "children"
}
```

**Opciones:**
- **A (Recomendada):** Usar `@JsonProperty` en el DTO para serializar con los nombres esperados sin cambiar la estructura interna.
- **B:** Renombrar los campos directamente en el DTO.
