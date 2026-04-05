# Análisis Funcional Definitivo: US-007 (Generador Cognitivo de DMN - NLP a Tablas de Decisión)

## 1. Resumen del Entendimiento
La historia aborda la transcripción verbal de Reglas de Negocio empíricas a una tabla DMN estrictamente estructurada que evalúa matrices en Policy "FIRST". Integra el motor FEEL y lo hace seguro contra ejecuciones remotas (Sandboxing Java) al mismo tiempo que maneja la UX del usuario final que es "Alergico" a los números.

## 2. Objetivo Principal
Traducir la complejidad jurídica o corporativa (Ej: Matrices de Riesgo) en tablas de decisiones procesables mecánicamente sin requerir un Data Scientist. La IA evalúa la oración, expulsa un XML estático inmutable, y Camunda lo evalúa con certeza del 100%.

## 3. Alcance Funcional Definido
**Inicia:** En la caja de chat NLP (Generación) o en la Carga de un Archivo.
**Termina:** Con la Tabla renderizada localmente, aprobada por el humano y minificada/commit mediante un Hash criptográfico para caché.

## 4. Lista de Funcionalidades Incluidas
- **Streaming Grid SSE:** Renderizado fila por fila en vivo.
- **Data Deduplication (Redis Caché):** Guardar Hash del prompt para ahorrar plata no re-solicitando a OpenAI algoritmos ya ejecutados globalmente.
- **Garbage Collection Efímero:** Borradores en LocalStorage purgables automáticamente a las 24 hr.
- **FEEL Engine Sandboxed:** Cero posibilidad de que la DMN lance código de Kernel/Bash en el container Java.
- **DOMPurify / XSS Prevención:** Celdas estériles a inyección maliciosa (Tags `<script>`).
- **Seudonimización Front-End:** Nombres de variables oscurecidos a la IA.
- **Inmutabilidad PUT:** Ediciones mutan a Version N+1 forzosamente (como US-005).
- **Hard Mathematics:** Cero date math asíncrono. Limitado a evaluar variables planas (`CustomerAge >= 18`). Máximo 50 reglas/filas.
- **Catch-All (Candado):** La fila última obligatoria devolverá "Revisión Humana" si nada cuadra orgánicamente.
- **UX XAI y Simulator:** Convierte el código oscuro FEEL en "Español/Business", y permite testear en RAM pura si funciona.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Arquitectónica CQS (⚠️ CRÍTICO CA-03):** Reclama que los XML "NO sellados" vivirán en *LocalStorage*. En la misma oración, exige explícitamente "y serán purgados físicamente de PostgreSQL a las 24h mediante un Job". Esto viola conceptualmente Arquitectura; el Backend (PostgreSQL) no purga lo que solo vive en memoria de front (LocalStorage). O viaja el borrador al backend REST y muere ahí, o no interviene la DB.
- **Deadlock Transaccional en Catch-All (⚠️ CA-07):** Impone obligatoriamente colocar una última fila `Output = Revisión Humana`. El motor de Camunda en modo Hit Policy FIRST siempre resolverá un Token. Si el diagrama BPMN de la US-005 instanciado *NO POSEE un Gateway* capaz de enrutar la rama hacia un analista cuando diga "Revisión Humana", la Business Rule Task acabará su vida, y el sistema creerá erradamente que la IA/DMN aprobó/resolvió, avanzando al siguiente paso sin control (Incident envenenador). La US-007 es ciega al diseño BPMN en V1.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógica generativa in-time sobre variables profundas (Dot Notation) como `Response.Payload.Age`.
- Compilador de Lógica Rápida ("Fast-Feel-Engine" del lado del cliente); la UX de test va contra Camunda RAM en motor Sandbox.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Crítico Relacional:** El comportamiento de la Fila de Catch-All exige obligatoriamente un rediseño de las responsabilidades BPMN (La US-005 debe forzar a validar el Path "Revision Humana", o el Pre-Flight crasheará el diseño). La contradicción de LocalStorage VS Postgres debe resolverse editando el Criterio 03 para eliminar "PostgreSQL".
