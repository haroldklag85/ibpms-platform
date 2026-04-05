# Análisis Funcional Definitivo: US-012 (Borrador de Respuesta Entrante LLM)

## 1. Resumen del Entendimiento
La US-012 define al "Fantasma en la Máquina" (El Agent Worker - LLM). Produce un pre-cálculo asincrónico redactando dos textos: Un auto-reply (Acuse) y una respuesta sustancial (Fondo). Impone barandas duras de seguridad LLM (Anti-alucinación forzando Placeholders vacíos) y limitación de Token-Window.

## 2. Objetivo Principal
Reducir el Time-To-Resolution (TTR) al mínimo (Ahorro de horas hombre tecleando "Hola, adjunto lo solicitado"). El humano pasa de ser "Redactor" a "Revisor y Aprobador" de la IA.

## 3. Alcance Funcional Definido
**Inicia:** Detonado asíncronamente cuando ingresa un Mail al Exchange/Webhook.
**Termina:** Cuando en la UI (Pantalla 2C), el humano visualiza el TextArea inyectado con el borrador y le da "Enviar".

## 4. Lista de Funcionalidades Incluidas
- **Dual Pipeline (CA-1):** Borrador Acuse / Borrador Respuesta Técnica.
- **Anti-Alucinaciones Financieras (CA-2):** Forzar tags `[INGRESAR_MONTO]` con validación UI (Bloqueo Botón Enviar).
- **Traductor Selectivo Bilingüe (CA-3):** Ignora outputs generativos en lenguajes raros, mostrando traducción para el operador humano.
- **Override MLOps (CA-4):** Retroalimentación de Edición Humana -> Entrenamiento Continuo (US-015).
- **Sliding Window Context (CA-5):** Solo los últimos 5 emails inyectados al Prompt para proteger contexto.
- **Tone Matching (CA-6):** Inyecta "Pido Respeto / Disculpas Institucionales" en prompts según Sentiment Analysis (US-013).
- **Prohibición de Verbos Transaccionales (CA-7):** La IA no puede prometer acciones, solo excusarse y revisar.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **La Ceguera Transaccional Estúpida (⚠️ CA-7 vs Inteligencia RAG):** El CA-7 es tan paranoicamente restrictivo que decreta que la IA de borrado NO puede certificar ningún estatus dictaminando "El caso está en revisión" obligatoriamente. **GAP:** Esto hace que la inversión en IA sea inútil si a nivel sistémico el iBPMS sí sabe que el pago "YA fue pagado exitosamente ayer". Si le vendamos los ojos al LLM para no leer la Base de Datos, producirá borradores burocráticos y torpes que el humano tendrá que destruir para re-escribir con data real. Se necesita autorizar "Lectura Autorizada con Confianza (RAG GET)" pero prohibir "Compromiso de Mutación (Promesas POST)".
- **Fricción Operativa (CA-2 "Cierres condicionales"):** Si el modelo asume variables mediante placeholders, inhabilitar el Botón "Aprobar y Enviar" del Front requiere un escaneo local de Regex pre-render inmaculado, asumiendo que el LLM respetará siempre la sintaxis "[VARIABLE]". Si el LLM usa "((variable))", el frontend dejará habilitado el botón y se enviarán correos repletos de etiquetas de modelo a clientes finales.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Agente Autónomo (Fire and Forget) - TODO correo requiere Revisión Humana P2C (Manual Override mandatory mode).

## 7. Observaciones de Alineación o Riesgos
**Fricción Comercial:** Mandar Acuses de Recibo (CA-1) creados por IA requiere pre-avisar al Cliente que es un bot el que acusa. De lo contrario se enojan asumiendo que el humano ignoró su dolencia principal respondiendo con simplezas instantáneas.
