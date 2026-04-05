# Análisis Funcional Definitivo: US-013 (Identificación automática IA y CRM ONS)

## 1. Resumen del Entendimiento
La US-013 es el "Motor Semántico y de Routing Inbound". Su labor silenciosa es destriapar los correos recibidos y vomitar un JSON puro estructurado (NER - Named Entity Recognition + Sentiment Analysis + CRM Sync) que sirve de alimento vital para la UI Docketing (US-011) y la generación de borradores (US-012).

## 2. Objetivo Principal
Evitar que el usuario humano escudriñe quién manda el correo, de qué contrato habla, y si está furioso. El LLM actúa como Portero Omni-Analista clasificando (Tagging) antes de que un humano lo vea.

## 3. Alcance Funcional Definido
**Inicia:** Webhook O365 recibe el correo crudo (invisible).
**Termina:** Cuando graba el registro JSON estructurado en `ibpms_metadata_index`.

## 4. Lista de Funcionalidades Incluidas
- **Match CRM Dominio (CA-1):** Consulta cliente ONS externo.
- **Flag Unknown (CA-2):** Etiquetas visuales de prospecto y creación de tarjeta.
- **Extracción NLP JSON (CA-3):** Genera `sentiment_score`, `predicted_service` y categortiza adjuntos sin abrirlos.
- **Fallback Catálogo Local (CA-4):** Si ONS no existe, cruza vs Local Catalog en base datos Postgres.
- **Enrutamiento por Cuerpo (CA-5):** El caso `@amazon.com` cruza el texto NLP con manual de descripciones de proyecto para adivinar The Right Scope.
- **Blacklist Hotmail/Gmail (CA-6):** Obliga en base a Regex a apagar Routing por dominio e impulsar al LLM a buscar RUTs/IDs dentro del payload string.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Ataque de Pánico MLOps (Ticket Fantasma) (⚠️ CA-3):** Se ordena emitir un JSON paralelo con Metadata estandarizada para alimentar DB relacional en Postgres. **GAP:** Al depender de la estocasticidad de un LLM masivo y no imponer validación por "Zod Strict Output Schemas", el LLM puede arrojar claves erróneas (Ej: emite `{ "clienteFurioso": true }` en vez de `"sentiment_score": 9`). Si esto pasa, el Webhook receptor crasheará al intertar insertar en Postgres (SQLException). El correo electrónico jamás se le mostrará a los humanos en US-011 porque su Metadata falló en inserción, resultando en demandas legales por negligencia (Ghost Tickets o Tickets Huérfanos). Exige implementar un DLQ y un ZOD Validation Layer antes de BD.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Inserción de respuesta Push-to-CRM (No crea tickets manuales en Salesforce o Hubspot, se crea interno iBPMS).

## 7. Observaciones de Alineación o Riesgos
**Performance:** Depender de una API externa CRM ONS "para cada correo que entra de forma síncrona" (CA-1) creará un cuello de botella fatal si entra SPAM. Se precisa usar cachés en base de redis locales (TTL de 24Hr por match) para aguantar picos en el buzón PQR.
