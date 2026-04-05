# Análisis Funcional Definitivo: US-040 (Embudo Inteligente de Intake y Cuarentena)

## 1. Resumen del Entendimiento
La US-040 es la interfaz de contención obligatoria paramétrica que filtra la Inteligencia Artificial Extractiva. Todas las "Ideas" (Action Cards) de la IA antes de arrancar los procesos en Camunda, aterrizan aquí para validación humana, a menos que un toggle de confianza las escale.

## 2. Objetivo Principal
Proveber a los administradores un Workdesk de Cuarentena (State Machine de 5 actos) con controles duros transaccionales (Concurrencia, Deshacer, Papeleras y Auto-routing).

## 3. Alcance Funcional Definido
**Inicia:** El Webhook ingesta correo, IA evalúa y sicta `Action_Card=Pending`.
**Termina:** El botón Aprueba (Inicia BPMN) o Descarta (Muerte temporal y feedback a MLOps).

## 4. Lista de Funcionalidades Incluidas
- **State Machine Restrictiva (CA-0):** Transición unidireccional irrompible. 
- **Concurrencia y Saneamiento:** Lock optimista (`@Version`) entre 2 admins (CA-3226). Ventana Toast Deshacer de 10s. Borradores con reloj activo SLA. Papelera de recilaje Soft-Delete TTL 48hr.
- **Rutas de Escalado:** Asignación humana a Pool o Asignado directo. Auto-instanciación IA (>98% confianza).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Bombardeo MLOps Estructural (Riesgo Auto-Aprobación) (⚠️ CA-3301):** Condiciona a que la "Feature Toggle de Auto IA" se ejecute si la tarjeta de pre-triaje supera el `98% de Confidence` de la Red Neuronal, saltándose la ventana humana al embudo. **GAP:** El *Confidence Score* de un LLM refleja la certidumbre semántica del tipo de trámite ("Estoy seguro que es Facturación"), pero *NO REFLEJA LA COMPLETITUD ESTRUCTURAL MATEMÁTICA* de los datos tabulares subyacentes. Si el LLM extrajo 99% seguro que es trámite Alpha, pero falló en Regex al extraer el "Nit", Camunda 8 arrojará Excepción de Datos y estrellará. Para disparar The Auto-Gate, la política debe exigir `Confidence > 98% AND ValidationSchema.isZodCompliant() == TRUE`.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Las tarjetas IA de "Aporte a caso Vivo (Inyección)" para reabrir casos en vuelo están relegadas al MVP v2 (Previene pesadilla Data Inconsistency concurrente en Camunda histórico).

## 7. Observaciones de Alineación o Riesgos
Excelente modelado de la "Muerte Aparente" / Tiempo de gracia. Retrasar por 10s el Dispatcher de eventos REST para evitar cancelar tokens de Camunda es una genialidad asíncrona de Frontend (Delay Queue) que ahorra millones de ciclos inútiles al orquestador Java por clics impulsivos.
