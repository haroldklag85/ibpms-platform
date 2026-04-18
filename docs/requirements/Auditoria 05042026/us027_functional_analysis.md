# Análisis Funcional Definitivo: US-027 (Copiloto IA - Auditoría ISO y Generador BPMN)

## 1. Resumen del Entendimiento
La US-027 encapsula un asistente Agentic LLM dentro del IDE BPMN (Pantalla 6). Utiliza comandos en Lenguaje Natural y cargas de documentos (RAG multimodal PDF/Imágenes) para construir diagramas autogenerados. Implementa una férrea disciplina SRE para evitar que el renderizado consuma todos los tokens del LLM o congele la pestaña de Google Chrome dibujando "spaghetti".

## 2. Objetivo Principal
Aceleración de Arquitectura Empresarial. Disminuir el tiempo de Discovery y mapeo de procesos, permitiéndole a la IA traducir memorandos largos a XML puro estricto. A su vez, servir como perro guardián normativo interactivo al analizar lo construido para proponer alertas ISO 9001.

## 3. Alcance Funcional Definido
**Inicia:** Con una petición "On Demand" del Arquitecto en su panel (Prompt/Archivo).
**Termina:** En la renderización del XML (vía layout matemático backend) sobre el lienzo, o en el rechazo transaccional por Prompt Injections.

## 4. Lista de Funcionalidades Incluidas
- **Severidad Geométrica (Auto-Layout):** Prohíbe al LLM intentar dibujar coordenadas X,Y de `<BPMNDi>`. Backend lo hace.
- **Data Diet (XML a JSON):** Transforma el XML inmenso a JSON mínimo antes de instanciar la petición OpenAI, ahorrando tokens.
- **Multimodal RabbitMQ (Workers):** Tika/Parsers anti-malware (ClamAV) en cola asíncrona para no matar el Front con OCR.
- **RAG Efímero (Aislamiento):** Base Vectorial `pgvector` tiene TTL estricto por sesión (se autodestruye al salir de la pantalla). Defiende contra Data Poisoning.
- **Traducción Activa & Restricciones:** Genera XML de Camunda restringido a 4 tipos de Nodo básico (sin Gateways Complejos), siempre en español.
- **Triage Píldoras Interactivas:** Pregunta al usuario si detecta roles/lógicas ambiguas.
- **Smart Merge / Undo Atómico:** Posibilidad de Rollback total del canvas post-IA sin aplastar nodos manuales previos.
- **Executable Flag Override:** Si la IA envía un XML inejecutable explícito, se frena el botón Desplegar (US-005).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Revocación Autoritativa Ciega (⚠️ CRÍTICO LÓGICO CA-05):** Establece que ante la detección de un intencional "Prompt Injection" (3 intentos), el usuario perderá su Rol `ROLE_PROCESS_ARCHITECT`. Esta orden automatizada y ejecutiva no contempla la altísima tasa de Falsos Positivos de un LLM Guardrail de Prompt Injection. Un Arquitecto operando semántica densa de ciberseguridad puede ser bloqueado accidentalmente, generando un incidente de Nivel 1. No debe existir revocación DB autónoma, sino Cuarentena/Soft-Lock hasta revisión CISO.
- **Undo Limit:** Se menciona que si el Chat minimiza hay un badge rojo, y que permite "CTRL+Z". No habla sobre un Redo si el humano borra un elemento de la IA pero quería recuperarlo.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Tunning nativo de LLM o despliegue LLM local in-house.
- Modificación directa del flujo en Motores de Ejecución vivos.
- Gateways Lógicos/Multimáticos inyectando `feel:` (Prohibido por CA-07).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Activo:** La protección Anti DoW (Denial of Wallet) mediante 5 generaciones/minuto de Rate Limit podría resultar muy restrictiva durante una presentación gerencial. El castigo silente a Arquitectos por Falsos Positivos es inaceptable y debe ser reconvertido a un Warning de Auditoría para que un humano penalice.
