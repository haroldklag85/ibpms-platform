---
description: Política Estricta de Gestión Documental y Uso del Monorepositorio para Agentes de IA
---

# 🤖 DIRECTRIZ OBLIGATORIA PARA AGENTES DE IA: Uso del Monorepositorio Documental

**Atención a todos los Agentes de IA operando en el entorno `ibpms-platform`:**

Este proyecto opera bajo una **Arquitectura de Monorepositorio Estricta**, diseñada para garantizar el orden estructural, la trazabilidad del código y mantener una Única Fuente de Verdad. Queda terminantemente prohibido dispersar información, "re-inventar" requerimientos o almacenar dependencias/análisis fuera de su ruta designada.

Antes de ejecutar cualquier lectura, refactorización funcional o creación de nuevo código/documentación, usted (el Agente) **DEBE ACATAR** las siguientes reglas inviolables:

## 1. Regla de "Leer Antes de Escribir" (Prevención de Duplicados)
- **Verificación Obligatoria:** Antes de generar un nuevo artefacto de análisis funcional, crear un contrato de API (YAML) o proponer componentes de arquitectura, **USTED DEBE inspeccionar** activamente las carpetas del repositorio para confirmar si ya existe un documento vigente que aborde ese tema.
- Use la herramienta `find_by_name` o `grep_search` sobre la carpeta `docs/` o `scaffolding/`.
- **Actualizar, No Duplicar:** Si identifica un material previo (ej. un plan de implementación antiguo o un esquema C4), **reutilice y actualice** el archivo existente usando `replace_file_content`. No cree archivos "v2", "v3" ni sobrescriba la historia creando documentos paralelos.  

## 2. Prohibición de Ubicaciones Alternativas
- **La Organización es Ley:** No almacene documentación técnica, minutas de reuniones, prompts, scripts provisionales ni código funcional en carpetas temporales como `/scratch`, `Desktop`, ni en el directorio raíz suelto.
- El proyecto posee una Jerarquía Oficial:
  - Todo documento de Arquitectura y Requerimientos debe crearse o modificarse exclusivamente dentro de `ibpms-platform/docs/...`
  - Toda la logística del Agente (checklist interno, prompts iterativos) pertenece a `ibpms-platform/scaffolding/...`
  - Todo el código backend pertenece a la estricta separación de Arquitectura Hexagonal instaurada en `ibpms-platform/backend/...`

## 3. Respeto por la Única Fuente de la Verdad y Sincronización Estricta
- Existe un único contrato comercial/funcional y un único plano arquitectónico. 
- **SINCRONIZACIÓN OBLIGATORIA (Regla C4 - Implementation Plan):** Si el usuario solicita un cambio arquitectónico y usted modifica los modelos arquitectónicos (`docs/architecture/c4-model.md` o `docs/architecture/c4-model-v2.md`), **ES OBLIGATORIO** actualizar inmediatamente la sección de "Solution-Architecture View" dentro del archivo `docs/architecture/implementation_plan.md` para evitar inconsistencias de diagramas Mermaid entre ambos documentos.
- Discrepancias entre el código (Backend Java) y los requerimientos (`docs/`) deben ser reportadas al usuario de inmediato señalando la incongruencia.

## 4. Protocolo de Comunicación de Agentes (Agentic Handoff Protocol)
Dado que el Squad de Desarrollo de IA funciona de forma asíncrona, toda la comunicación técnica, resolución de dependencias entre capas (Backend/Frontend) y reportes de QA debe estar escrita y versionada. Quedan prohibidas las asunciones silenciosas a espaldas del equipo.

- **Directorio de Sincronización:** El equipo utilizará una carpeta raíz oculta llamada `.agentic-sync/`.
- **Canalización por Archivos:** Cuando un agente finaliza un macro-componente (ej. *El API de Expedientes ya funciona y devuelve 200 OK*), debe redactar un "Handoff Report".
  - Ejemplos de Handoffs: `.agentic-sync/backend_to_frontend_handoff.md`, o `.agentic-sync/squad_to_lead_architect_request.md`.
- **Contenido del Handoff:** Cada nota de traspaso debe incluir: (1) Qué se completó, (2) Qué contrato (OpenAPI) responde, (3) Cómo el Agente receptor debe probarlo, (4) Bloqueantes detectados.

## 5. Lineamientos para Documentación en Código Generado
- La documentación en el código generado debe estar firmemente asociada a declaraciones relevantes (clases, métodos, campos o miembros públicos).
- Los comentarios deben ser claros y concisos, y su objetivo principal debe ser explicar el **propósito o la razón** (el porqué) detrás del código, no lo que el código hace.
- Deben usarse con moderación. Si el propio código ya es suficientemente expresivo y auto-documentable siguiendo las normas de Clean Code, omita la inserción de comentarios redundantes.

> **Objetivo Final:** 
> Usted debe actuar como un Custodio Digital implacable. Su meta es mantener el repositorio libre de basura digital, asegurando un orden clínico, eficiencia máxima en el trabajo y una trazabilidad impecable para cualquier otro Agente o Humano que acceda al proyecto.

-- Fin de la Directriz --
