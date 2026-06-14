---
description: Protocolo para mantener sincronizado el archivo v1_user_stories_registry.json con el estado real del repositorio modularizado de Épicas (docs/requirements/epics/).
version: 1.0.0
---

# 🔄 Workflow: Sincronización del Registry JSON de User Stories

> **Versión:** 1.0 | **Fecha de creación:** 2026-04-14
> **Responsable:** Agente Arquitecto Líder o Agente Product Owner
> **Artefacto objetivo:** `docs/requirements/v1_user_stories_registry.json`
> **Dependencias:** `docs/requirements/v1_user_stories_index.md`, `docs/requirements/epics/epic_X_*.md`

## Contexto

El archivo `v1_user_stories_registry.json` es el mapa programático que los agentes usan para localizar rápidamente una User Story (US) y su archivo de Épica de origen. Si este archivo se desincroniza con el contenido real de los archivos en `epics/`, los agentes seguirán rutas incorrectas o no encontrarán las US nuevas.

## Triggers (Cuándo ejecutar)

- Después de agregar, renombrar o eliminar una User Story de un archivo de Épica.
- Después de crear un nuevo archivo de Épica en `docs/requirements/epics/`.
- Después de ejecutar el workflow de Graduación de Hallazgos (`graduacionAuditoriaAlSsot.md`) si se crearon CAs nuevos.
- Cuando un agente reporte que una US no se encuentra en el registry.
- Al inicio de cada Sprint como validación preventiva.

## Roles autorizados

- **Ejecutor:** Agente Arquitecto Líder o Agente Product Owner.
- **Excluidos:** Backend, Frontend, QA — estos agentes solo consultan el registry, no lo modifican.

---

## Flujo de Trabajo (4 Fases)

### FASE 1: Inventario del Estado Real de las Épicas

**Objetivo:** Obtener la lista real de todas las User Stories que existen en los archivos de Épica.

1. Ejecutar `list_dir` sobre `docs/requirements/epics/` para obtener la lista de archivos.
2. Para cada archivo `epic_X_*.md`, ejecutar PowerShell para extraer las US declaradas:
   ```powershell
   Select-String -Path "docs\requirements\epics\epic_A_motor_core.md" -Pattern "^## US-\d+" | Select-Object LineNumber, Line
   ```
3. Consolidar en una lista: `{ US-ID, archivo_origen, línea }`.

### FASE 2: Lectura del Registry JSON Actual

**Objetivo:** Obtener el estado declarado del registry para compararlo con el real.

1. Leer `docs/requirements/v1_user_stories_registry.json` con `view_file`.
2. Parsear la estructura JSON y extraer la lista de US registradas con sus campos (`id`, `title`, `epic`, `file`).

### FASE 3: Detección de Discrepancias

**Objetivo:** Cruzar el inventario real (Fase 1) con el registry declarado (Fase 2).

Clasificar cada US en una de estas categorías:

| Categoría | Significado | Acción |
|-----------|-------------|--------|
| ✅ **Sincronizada** | US existe en el archivo de Épica Y en el registry con el path correcto | Ninguna |
| 🔴 **Faltante en Registry** | US existe en un archivo de Épica pero NO aparece en el registry | Agregar entrada al registry |
| 🟡 **Path Incorrecto** | US existe en el registry pero el campo `file` apunta a un archivo que no existe o es incorrecto | Corregir el campo `file` |
| ⚠️ **Fantasma en Registry** | US aparece en el registry pero NO existe en ningún archivo de Épica | Eliminar entrada del registry |

### FASE 4: Aplicación de Correcciones

**Objetivo:** Actualizar el registry JSON para reflejar el estado real.

1. Generar la versión corregida del JSON con las entradas nuevas, corregidas o eliminadas.
2. Mostrar al Humano un resumen de los cambios propuestos:
   ```markdown
   ## Cambios propuestos al Registry:
   - ➕ Agregar: US-050 → epics/epic_E_integraciones.md
   - 🔧 Corregir: US-003 path de epic_B → epic_A
   - ➖ Eliminar: US-099 (no existe en ninguna Épica)
   ```
3. **Esperar aprobación del Humano** antes de escribir los cambios.
4. Usar `replace_file_content` o `write_to_file` para actualizar el JSON.
5. Ejecutar `git commit -m "chore(registry): Sincronización de v1_user_stories_registry.json"` seguido de `git push`.

---

## Sincronización del Índice (Paso Complementario)

Si se detectaron discrepancias en la Fase 3, también se debe verificar que `docs/requirements/v1_user_stories_index.md` refleje las mismas Épicas y US. Si el índice también está desincronizado:

1. Actualizar la tabla del índice para que liste correctamente todas las US por Épica.
2. Hacer `git commit` del índice junto con el registry en el mismo commit.

---

## Estructura del Registry JSON (Referencia)

Cada entrada del registry debe seguir esta estructura:

```json
{
  "id": "US-001",
  "title": "Obtener Tareas Pendientes en el Workdesk",
  "epic": "A",
  "epicName": "Motor Core de Gestión de Tareas",
  "file": "epics/epic_A_motor_core.md",
  "totalCAs": 30,
  "version": "V1"
}
```

**Campos obligatorios:**
- `id`: Identificador único de la US (formato `US-XXX`).
- `title`: Título descriptivo de la US.
- `epic`: Letra identificadora de la Épica (A, B, C...).
- `epicName`: Nombre completo de la Épica.
- `file`: Path relativo al archivo de Épica desde `docs/requirements/`.
- `totalCAs`: Conteo total de Criterios de Aceptación.
- `version`: Versión del MVP (`V1`).

---

## Validaciones de Calidad

Antes de dar el workflow por completado:

- [ ] Cada US en los archivos de Épica tiene una entrada en el registry.
- [ ] Cada entrada del registry apunta a un archivo que realmente existe.
- [ ] El campo `totalCAs` refleja el conteo real de `Scenario:` en el archivo de Épica para esa US.
- [ ] El índice (`v1_user_stories_index.md`) es consistente con el registry.
- [ ] Los cambios fueron commiteados y pusheados a la rama activa.

---

## Anti-Patrones

1. **NO modificar el registry sin verificar el estado real de las Épicas.** Siempre ejecutar la Fase 1 primero.
2. **NO eliminar entradas del registry sin confirmar que la US fue realmente removida.** Podría ser un error de grep.
3. **NO actualizar el registry manualmente sin pasar por este workflow.** Todo cambio debe ser trazable.
