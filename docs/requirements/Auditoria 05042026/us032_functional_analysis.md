# Análisis Funcional Definitivo: US-032 (Orquestación de IA y Generative Task RAG)

## 1. Resumen del Entendimiento
La US-032 incorpora Inteligencia Artificial (LLM) como un 'Worker' formal y determinista de Camunda ("Generative Task"). Permite emitir borradores, estructurar JSONs e inhibir interacciones alucinadas mediante bucles RLHF (Self-Reflection y Validación Humana).

## 2. Objetivo Principal
Dotar a Camunda Platform de nodos predictivos que eliminen el 'Data-Entry' robótico de abogados/analistas en trámites pesados, extrayendo y respondiendo a requerimientos ciudadanos guiados incondicionalmente por Enterprise Prompts centrales y RAG de SGDEA.

## 3. Alcance Funcional Definido
**Inicia:** El token del BP entra al Generative Task (Pantalla 6 modelada).
**Termina:** El LLM entrega el Objeto JSON con el conocimiento redactado o el Motor aplica Fail-back a ruta manual (Fallback B).

## 4. Lista de Funcionalidades Incluidas
- **Enmascaramiento RAG / PII DLP (CA-3400/CA-3470):** Interceptor de cifrado de cédulas/nombres en peticiones salientes a OpenAi/Azure.
- **Doble Agente Validador (Self-Reflection) (CA-3453):** Modelo A escribe borrador, Modelo B ataca el borrador en base a reglas de alucinación para rechazarlo o aprobarlo antes del humano.
- **Failover Activo Multi-Vendor (CA-3430):** En caso de timeout de proveedor IA, redirige el prompt a la nube del competidor.
- **RLHF In-loop (CA-3464):** Retroalimenta el modelo con diferencias (Delta) si el humano edita un borrador final.
- **Transparencia Forense No Ciudadana (CA-3458):** Loguea ChainOfThought inmutable, pero no le cuenta al ciudadano final (Sin marca de agua en PDF, CA-3436).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alucinación de Concordancia Pos-Enmascaramiento DLP (⚠️ CA-3470):** Se propone que un interceptor de Seguridad reemplace datos PII por "Hashes" y envíe el hash al LLM, hidratándolo después en red local. **GAP Lingüístico:** Si la IA procesa Hashes crudos (`34bf21X`), pierde connotaciones de Género/Identidad en idiomas inflexivos ("Carlos=Él", "Ana=Ella"). El LLM responderá incoherencias pronominales sobre la petición ("Estimado 34bf21X ha sido aceptada..."). La técnica DLP no puede ser un "Pseudo-Hashing Ciego", debe ser un algoritmo NER de Sustitución Tipificada (`[PERSON_MALE_1]`, `[COMPANY_NAME]`) para mantener la integridad semántica de la construcción LLM.
- **Deuda Legal "Sin Marcas de Agua AI" (⚠️ CA-3436):** Mandato arquitectónico de esconder a los clientes que un LLM estructuró las cartas. Al margen del proceso de aprobación humana firmado, tratados legales internacionales (AI Act EU) y directrices corporativas obligan éticamente al "Disclosure" al interactuar mediado por IA. Esto debe ser consultado legalmente para evitar exponer a la corporación a demandas por fraude B2C.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- RAG en Archivos de Voz (WAV/MP3) o Video.
- Auto-descarga web de conectores IA (Function Calling de libre albedrío inhabilitado CA-3554).
- Traducción automática condicionada saliente (Diferida).

## 7. Observaciones de Alineación o Riesgos
Controlar Tokens limitados por "Task" (CA-3420) es fantástico contra un 'Wallet' corporativo. Evita sorpresas astronómicas de facturación a fin de mes limitando la cuota de la red RAG.
