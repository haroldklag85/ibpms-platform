# Análisis Funcional US-007: Generador Cognitivo de DMN (NLP a Tablas de Decisión)

## Resumen del Entendimiento
La US-007 define un módulo inteligente que permite a Arquitectos de Procesos y Usuarios de Negocio describir políticas lógicas en lenguaje natural (NLP) para que el iBPMS genere automáticamente una Tabla de Decisión DMN (Decision Model and Notation). Esta generación es mediada por un modelo fundacional (LLM) externo. Dada la sensibilidad de las reglas de negocio, la historia impone restricciones severas a nivel de seguridad (Zero-Trust, ofuscación PII), resiliencia (Server-Sent Events, cachés criptográficos), e integridad matemática (Hit Policy FIRST, Catch-All mandatorio). Además, define el ciclo de vida del DMN: creación, simulador integrado, aprobación, versionamiento estricto y catalogación.

## Objetivo Principal
Proporcionar una interfaz conversacional (NLP) que traduzca reglas de negocio verbales en artefactos ejecutables DMN sin introducir vulnerabilidades de seguridad, proteger los datos sensibles (PII) durante la interacción con LLMs, y garantizar la eficiencia computacional (evitando "Denial of Wallet" y latencia extrema) mediante caché y minificación XML, asegurando siempre que la decisión generada sea lógicamente completa (Hit Policy: FIRST y Catch-All).

## Alcance Funcional
**Incluye:**
*   Interfaz de Chat NLP y visor en tiempo real de la tabla DMN con Virtual Scrolling.
*   Traducción asíncrona de Prompt a DMN mediante Server-Sent Events (SSE).
*   Ofuscación de PII previa al envío del prompt y validación estricta (Sandbox) del output DMN.
*   Caché de prompts en Redis y persistencia de borradores en PostgreSQL.
*   Editor DMN visual, simulador de decisiones (pruebas en blanco), versionamiento y rollback explícito.
*   Catalogación (DMN Library Dashboard) para buscar y administrar reglas.

**Termina donde comienza:**
*   La ejecución en Producción de estas DMNs. Esto es delegado a Camunda Engine a través del BPMN desplegado en la US-005.
*   La modificación del esquema Zod subyacente. Los campos provienen del Formulario (US-003).

## Lista de Funcionalidades Incluidas
1.  **Generación Asíncrona SSE:** Evita timeouts mediante streaming de filas en tiempo real.
2.  **Caché Criptográfica y Rate Limiting:** Evita doble facturación LLM comparando hash de Prompt+Diccionario.
3.  **Gestión de Borradores:** Guarda iteraciones en PostgreSQL y LocalStorage, y limpia borradores huérfanos.
4.  **Minificación XML:** Comprime espacios y formatea el DMN previo al Commit en base de datos.
5.  **Seudonimización PII:** Oculta identificadores en las variables antes del request al LLM.
6.  **Catch-All y Hit Policy FIRST:** Garantiza evaluación determinista inyectando fila final obligatoria.
7.  **Limitación Lógica:** Restringe a una salida (Output) principal y máximo 50 filas por generación.
8.  **XAI y Simulador:** Traduce código FEEL a lenguaje humano y permite evaluar reglas "en seco" con resaltado verde.
9.  **Library Dashboard:** Catálogo con paginación server-side, búsqueda, y gestión de versiones.

## Lista de Brechas, Gaps o Ambigüedades
1.  **Vulnerabilidad Crítica de IDOR (Tenant_ID Hardcodeado):** Actualmente en el controlador `DmnGeneratorController` el identificador de inquilino (`tenantId`) está forzado en duro (`"tenant-alpha"`). Esto rompe el aislamiento multitenant y el principio Zero-Trust (AppSec), abriendo un vector IDOR donde cualquier usuario genera carga al inquilino alpha. **Requiere remediación inmediata**.
2.  **Sincronización Transaccional en Sello:** Aunque se establece que el rollback vuelve a V1, hay falta de detalle sobre cómo Camunda recibe en caliente (Hot-Deploy) este rollback sin romper instancias en vuelo (se asume comportamiento Late Binding del BPMN).

## Lista de Exclusiones
*   **Date-Math en IA:** El modelo LLM no hará operaciones de fecha; se exige que los campos enteros lleguen pre-calculados.
*   **Reglas Complejas (Any, Collect):** Solo se soporta `Hit Policy: FIRST`. Cualquier intento de usar otra política en modo XML se rechaza (HTTP 422).
*   **Dot-Notation (Estructuras profundas):** Las variables en la regla solo pueden usar tipos primitivos de primer nivel del esquema Zod.
*   **Ejecución Operativa:** La US no abarca la ejecución de la regla dentro de una instancia, solo la creación y prueba del modelo DMN.
*   **Persistencia de Test Cases:** Las pruebas del simulador son efímeras. No se persisten escenarios de prueba para V1.

## Observaciones de Alineación o Riesgos
*   **Clasificación MoSCoW:** Generación IA (Must-Have), Simulador (Must-Have), Virtual Scrolling (Should-Have).
*   **Resumen de Dependencias:** Depende directamente de la **US-003** para el diccionario de datos Zod, de la **US-005** para enlazar las DMN creadas a las tareas del BPMN, y de la **US-036** (RBAC) para la verificación del rol `ROLE_PROCESS_ARCHITECT`.
*   **Dependencia Bloqueante:** Ningún DMN puede referenciar variables que no existan previamente en el diccionario del Formulario Zod vinculado a la rama del proceso. El Bug/Gap de `tenant-alpha` en el `DmnGeneratorController` debe solucionarse usando `SecurityContextUtils.getTenantId()`.
