# Análisis Funcional Definitivo: US-023 (Correlación Continua del Hilo)

## 1. Resumen del Entendimiento
La US-023 rige el motor unificador del "Thread de Conversación". Opera para que si existen 5 correos entre el SAC y el Usuario antes de instanciar formalmente un reclamo, todos sean empaquetados juntos para que las Pantallas y la Máquina mantengan contexto.

## 2. Objetivo Principal
Asegurar que todas las respuestas al correo de originación Inbound sean succionadas y unificadas como Contexto Pre-SD (Previo al Service Delivery definitivo).

## 3. Alcance Funcional Definido
**Inicia:** Un cliente oprime Responder al Confirm-to-Create pre-SD.
**Termina:** El webhook vincula los Message-ID y, cuando el Admin por fin despacha The Master Process, le adhiere el paquete completo.

## 4. Lista de Funcionalidades Incluidas
- **Threading Recursivo (CA-2):** El Webhook Microsoft O365 / IMAP detecta la caída de un mail de respuesta, extrae el hilo padre, une por UUID y encola.
- **Herencia Masiva a Camunda (CA-2):** Traspasa este Contexto previo como un objeto contínuo atado al Expediente Madre originario.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Crash de Payload en Inserción de Motor BPM (⚠️ CA-2):** Establece como finalización obligatoria: "*vincula todo ese hilo previo de correos (Pre-SD Context) a la instancia madre del BPMN (SD)*". **GAP:** Si vinculan el hilo de 15 respuestas (Textos HTML enormes, docenas de kB de adjuntos en B64) pasándolas como "Variables Activas de Arranque" al Webhook de `/engine-rest/process-definition/key/start`, Camunda 8/7 enloquecerá y PostgreSQL bloqueará la serialización del Object Value. Camunda penaliza duramente cargar basura estática enorme en su historial transaccional de estados. Toda esta comunicación DEBE heredarse en el Metadata Index Relacional (`ibpms_metadata_index`), y al BPMN Madre SOLO enviarle un minúsculo "Thread UUID". Una correlación mal interpretada tumba toda la Arquitectura de Microservicios.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Respuestas salientes manuales de la IA interactiva no están cubiertas aquí.

## 7. Observaciones de Alineación o Riesgos
**Ruptura Forense RAG:** Toda esa cadena unificada ("Hilo Previo Contexto") es la fuente alimentadora del LLM (RAG). Si la separación técnica "Headers de O365" vs "Body crudo" no se filtra eficientemente, los Modelos LLM consumirán toneladas de tokens asimilando las basuras MIME incrustadas perdiendo eficacia, arrojando falsos positivos en el SAC.
