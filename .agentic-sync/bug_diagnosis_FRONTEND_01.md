# 🔬 Diagnóstico Forense - FRONTEND_01

## Descripción del Bug
El servidor de desarrollo de Vite del Frontend no podía levantar debido a un error de análisis de importación (`[plugin:vite:import-analysis] Failed to resolve import`). Específicamente, `MainLayout.vue` intentaba importar el componente `CQRSConnectionToast.vue` desde `src/components/common/`, pero el archivo no existía bajo ese nombre.

## Capa Afectada
- **Capa Probable**: Frontend (🎨 FRONTEND)

## Archivos Sospechosos
- `src/layouts/MainLayout.vue` (líneas 10 y 277)

## Causa Raíz Hipotética
Una renombramiento de archivo o un error tipográfico en el desarrollo previo. El componente real se llama `ConnectionToast.vue`. El Layout principal no fue actualizado para reflejar este cambio en el nombre de la importación y la declaración del tag HTML.
