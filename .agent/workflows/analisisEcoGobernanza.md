---
description: Analiza el ecosistema de gobernanza (rules, workflows, skills), detecta contradicciones/reglas rotas, y actualiza el documento de análisis centralizado.
---

# Título
Análisis resumido del ecosistema de gobernanza: rules, workflows, skills, validación de contradicciones o reglas rotas y actualización de documento en `governance_ecosystem_analysis.md`

## Contexto
Existe un ecosistema de gobernanza compuesto por archivos y estructuras que controlan el comportamiento operativo del sistema y de sus agentes. Se requiere entender de forma resumida qué función cumple cada artefacto dentro del modelo de gobernanza.

Las rutas a considerar para este análisis son:
- `.cursorrules`
- `platform\scaffolding\workflows\`
- `ibpms-platform\.agents\skills\`
- `ibpms-platform\.cursorignore`

La salida debe ser resumida, pero suficientemente clara para identificar propósito, alcance, cobertura, agentes impactados, exclusiones, oportunidades de mejora y posibles contradicciones o reglas rotas dentro del ecosistema.

Adicionalmente, cuando se ejecute este prompt, el resultado debe quedar **actualizado o guardado** en el archivo:
- `ibpms-platform/docs/governance_ecosystem_analysis.md`

## Objetivo
Explicar de forma resumida cada archivo o grupo de archivos del ecosistema de gobernanza, detallando qué hace cada **rule**, **workflow** o **skill**, indicando su alcance, objetivo, cubrimiento, agentes a los que aplica, qué no contempla, incluyendo recomendaciones de mejora y un análisis para determinar si existen contradicciones o reglas rotas, y dejar el resultado persistido en `ibpms-platform/docs/governance_ecosystem_analysis.md`.

## Instrucciones para el agente
1. Revisa los artefactos ubicados en las siguientes rutas:
   - `.cursorrules`
   - `platform\scaffolding\workflows\`
   - `ibpms-platform\.agents\skills\`
   - `ibpms-platform\.cursorignore`
2. Identifica cuáles corresponden a:
   - **rules**
   - **workflows**
   - **skills**
   - y archivos auxiliares de gobernanza
3. Para cada archivo o artefacto relevante, entrega una explicación resumida con esta estructura:
   - Nombre del archivo
   - Tipo de artefacto
   - Objetivo
   - Alcance
   - Nivel de cubrimiento
   - Agentes sobre los que aplica
   - Qué controla, activa o condiciona
   - Qué no contempla
   - Recomendación de mejora
4. Si hay varios archivos similares dentro de una carpeta, puedes agruparlos cuando tenga sentido, siempre que no se pierda claridad.
5. Explica la información en lenguaje claro, operativo y fácil de entender.
6. Si detectas relaciones entre artefactos, indícalas brevemente.
7. Realiza un análisis transversal para determinar si existen:
   - contradicciones entre rules, workflows o skills,
   - solapamientos conflictivos,
   - vacíos de cobertura,
   - reglas rotas,
   - dependencias inconsistentes,
   - o “leyes” del sistema que se contradicen entre sí.
8. Si identificas contradicciones o reglas rotas, repórtalas como hallazgos resumidos, indicando:
   - dónde ocurre el conflicto,
   - qué impacto puede generar,
   - y qué recomendación aplica.
9. No inventes comportamiento no sustentado en el contenido real de los archivos.
10. Si algún archivo no tiene suficiente contexto para interpretarse con certeza, indícalo explícitamente.
11. Incluye recomendaciones de mejora al final del análisis o por cada artefacto, según convenga.
12. Genera el resultado final en formato Markdown estructurado y **actualízalo o guárdalo** en:
   - `ibpms-platform/docs/governance_ecosystem_analysis.md`
13. Si el archivo ya existe, actualízalo preservando una estructura clara y evitando duplicidades innecesarias.
14. Si el archivo no existe, créalo con una estructura ordenada, trazable y lista para consulta futura.
15. Asegúrate de que el documento final sirva tanto como referencia explicativa como insumo de gobierno técnico.

## Entregables esperados
- Inventario resumido de artefactos de gobernanza.
- Clasificación por tipo: rule, workflow, skill o archivo auxiliar.
- Explicación resumida por cada archivo o grupo de archivos.
- Identificación de agentes impactados por cada artefacto.
- Hallazgos sobre vacíos, solapamientos, contradicciones o reglas rotas.
- Recomendaciones de mejora.
- Documento actualizado o creado en `ibpms-platform/docs/governance_ecosystem_analysis.md`.

## Criterios de calidad
- La salida debe ser resumida pero útil.
- Debe quedar claro qué hace cada artefacto y hasta dónde llega.
- Debe identificarse sobre qué agentes aplica cada elemento.
- Deben señalarse exclusiones y limitaciones.
- Deben detectarse contradicciones o reglas rotas si existen.
- Las recomendaciones de mejora deben ser concretas y accionables.
- El documento generado debe quedar bien estructurado, sin duplicidades innecesarias y listo para reutilización.
- No debe haber alucinación ni inferencias sin sustento.

## Restricciones o consideraciones
- No convertir la salida en documentación excesivamente extensa.
- No mezclar funciones de rules, workflows y skills sin diferenciarlas.
- No omitir archivos auxiliares relevantes como `.cursorignore` si afectan gobernanza.
- Si falta contexto, declararlo explícitamente.
- No sobrescribir el documento final con una versión peor estructurada o menos completa que la existente sin justificación.

## Supuestos detectados
- Se asume que las rutas indicadas contienen el ecosistema principal de gobernanza.
- Se asume que estos artefactos impactan el comportamiento de uno o varios agentes.
- Se asume que el usuario requiere una visión resumida pero operativa.
- Se asume que además de explicar, se espera identificar oportunidades de mejora y posibles contradicciones.
- Se asume que el archivo `ibpms-platform/docs/governance_ecosystem_analysis.md` es la ubicación oficial del resultado.

## Dudas o vacíos detectados
- Si dentro de las carpetas existen subdirectorios muy amplios, puede ser necesario agrupar por categorías para mantener la salida resumida.
- No se especifica si debe priorizarse análisis funcional, técnico o mixto; por defecto se asumirá un enfoque mixto.
- No se define si debe conservarse historial de versiones del documento generado; si no existe instrucción adicional, se asumirá actualización de la versión vigente.
