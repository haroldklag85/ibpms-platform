---
name: Architect Handoff Generation Protocol
description: Instrucciones estrictas para la creación de documentos de Handoff técnicos destinados a Agentes Especialistas (Backend, Frontend, QA) para evitar abstracciones y fallos sistémicos.
version: 1.0.0
---

# 🏗️ Protocolo de Handoff Arquitectónico (Zero-Hallucination)

## 📌 Propósito
Este skill obliga al Arquitecto Líder (o a cualquier agente planificador) a redactar documentos de delegación (Handoffs) que sean **técnicamente prescriptivos, deterministas y libres de ambigüedad**. El objetivo es mitigar "alucinaciones", donde el agente asume erróneamente que las instrucciones abstractas son suficientes, resultando en despliegues incompletos, pérdida de tiempo en ciclos de desarrollo y violaciones de las métricas de QA.

---

## 🚫 Anti-Patrones Estrictamente Prohibidos
1. **Instrucciones Abstractas:** Prohibido decir "agrega un campo de estado" o "actualiza la UI". Se debe indicar el tipo de dato y la capa visual exacta.
2. **Ignorar el Contexto Preexistente:** Prohibido ordenar cambios sin especificar la ruta del archivo exacta ni referenciar los métodos o tablas afectadas.
3. **Comandos de Fallback Ciegos (Compilación Simplificada):** Tienes ESTRICTAMENTE PROHIBIDO indicar a los agentes que compilen utilizando atajos como `mvn clean install` o `npm run build`. **SIEMPRE** se deben invocar oficialmente los protocolos Zero-Trust por referencia.

---

## ✅ Estructura Obligatoria del Documento de Handoff

Todo Handoff DEBE generarse en la carpeta `.agentic-sync/` con un nombre de archivo descriptivo (ej. `handoff_77DEV_US001_CA01_CA03.md`) y DEBE contener rigurosamente las siguientes 6 secciones:

### 1. Metadatos y SSOT (Single Source of Truth)
- Iteración/Sprint y Rama de trabajo (`sprint-3/...`).
- User Story y Criterios de Aceptación (CAs) exactos a desarrollar.
- URL o path local de la fuente de verdad histórica (ej. `v1_user_stories.md`).
- Flujo de Trabajo (Orden secuencial requerido, e.g. Back -> Front -> QA).

### 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
Antes de prescribir y delegar el código, el documento DEBE declarar y asegurar el cumplimiento de las restricciones sistémicas:
- **Validación de ADRs:** Identificar y listar qué ADRs impactan o rigen esta iteración (ej. `adr_postgres_pgvector.md`, convenciones SQL locales, dependencias técnicas aprobadas).
- **Lineamientos Transversales:** Confirmar explícitamente cómo el diseño de este sprint respeta los pilares innegociables del proyecto, como el *Tenant Isolation*, resiliencia en arquitecturas *CQRS*, trazabilidad *ISO*, mitigación de alucinaciones (RAG) o seguridad *Zero-Trust*.
- **Trazabilidad de la Solución:** Demostrar o explicar (en 1-2 párrafos) por qué las interfaces y lógicas propuestas (snippets) cumplen al 100% con estas leyes de arquitectura pre-establecidas.

### 3. Rutas Exactas y Contexto Preexistente
Por cada tarea estipulada, proveer:
- **Paths Absolutos/Relativos:** El nombre del archivo exacto que el agente debe leer o modificar (ej. `backend/.../entity/MiEntidad.java`).
- **Estado Actual:** Resumen del componente preexistente (columnas actuales de la base de datos, hooks de Vue actuales), para orientar temporalmente al agente que toma la tarea.

### 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
No pidas la construcción de lógica desde cero; dicta la base de la lógica.
- **Base de Datos:** DDL completos en SQL estructurados para changelogs de Liquibase.
- **Backend:** Snippets obligatorios de firmas de métodos, variables de los DTOs y lógica crítica (ej. el manejo de `try/catch` para estrategias de degradación).
- **Frontend:** Estructura del framework preferido (Tablas, selectores o Slots Vue) e integración de store. **Prohibido dejar el diseño y UX abierto a la interpretación.**

### 5. Matriz de QA y Testing Atómico
Sección obligatoria dirigida al Agente de QA (TDD):
- Nombre y ruta del script de pruebas a crear (Vitest/JUnit).
- Una tabla de validación cruzada garantizando que cada Criterio de Aceptación se corresponda 1 a 1 con al menos un caso de prueba.
  > Formato: Test Name | CA Evaluado | Aserción Esperada (Ej. "ProgressBar renderiza style width=60% calculando 60").

### 6. Mensaje de Despacho (Comunicación al Agente Especialista)
En la parte final del archivo, agrega los mensajes que el Agente Arquitecto enviará al humano para copiar/pegar y delegar. Se DEBEN aplicar estas plantillas absolutas:

**Para Agentes Backend:**
> "Compilación obligatoria: Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B."

**Para Agentes Frontend:**
> "Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`."

---

## 🎯 Gatillo de Ejecución
Siempre que el usuario solicite: "Crea el handoff", "Prepara el documento de delegación", "Planifica el desarrollo" o "Inicia la iteración X", el Agente involucrado **DEBE LEER Y APLICAR este SKILL** imperativamente antes de generar resultados o escribir markdown en el repositorio.
