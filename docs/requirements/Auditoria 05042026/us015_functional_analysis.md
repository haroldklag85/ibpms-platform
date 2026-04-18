# Análisis Funcional Definitivo: US-015 (MLOps Feedback Loop Nocturno)

## 1. Resumen del Entendimiento
La US-015 estipula un circuito cerrado de aprendizaje automático (Feedback Loop) dividido en Diurno (Recolector liviano asíncrono) y Nocturno (Entrenador Batched pesado) para afinar clasificaciones (Routing) y NLP Textual (Borradores).

## 2. Objetivo Principal
Garantizar que si el "Buzón Ágil" empieza a equivocarse, no dependa de tickets y código fuente para corregirlo; los Analistas Humanos "programan" la IA tácitamente corrigiendo su trabajo en la interfaz diaria.

## 3. Alcance Funcional Definido
**Inicia:** Edición de Sugerencia (US-014/US-012) produce POST `Fire and Forget` asíncrono a PostgreSQL/Rabbit.
**Termina:** Batch Scheduler ("Agent Data Scientist") procesa en O-Noche las filas del día.

## 4. Lista de Funcionalidades Incluidas
- **DLQ & Asincronicidad Estricta (Diario) (CA-1, CA-5):** Prohíbe que el POST feedback atasque a la cola principal de Hibernate. Emplea RabbitMQ con derivado a `mlops-dlq`.
- **Filtro de Contradicción y Ruido (CA-2):** Requiere Consenso Multi-Usuario >= 2 analistas dictaminando lo mismo para aceptarlo como un 'Patrón Verdadero de Error de IA', ignorando discrepancias y los "Zafacones puros".
- **Sistema de Pesos Jerárquicos (CA-2):** El feedback de un 'Senior/Rol Director' pesa x5 vs Junior.
- **Incompetencia Diurna Asumida (CA-4):** Ante un Confidence Score bajo, la IA prefiere "No decir nada" que equivocarse feo.
- **Trazabilidad Métrica (CA-3):** Loggea el reporte "Status: Trained" para purgar cola.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alucinación Arquitectónica Algorítmica (⚠️ CA-2):** El documento exige que en la noche el Agente "actualice los PESOS del modelo y actualice la red neuronal para imitar estilo institucional (NLG)". **GAP:** Matemáticamente irreal para la V1. Un Job nocturno local en SpringBoot *no puede* alterar los *Pesos de red (Weights de Float16)* de un modelo predictivo SaaS como OpenAI GPT-4 / Azure Foundry ni de forma instantánea ni gratis, a menos que despache un JSONL masivo de Fine-Tuning. Tampoco actualiza pesos al RAG Vector Database, eso simplemente son Embeddings textuales inyectados en la Búsqueda. Este CA es una alucinación teórica del autor: El "Agente" nocturno realmente debe conformarse con indexar el nuevo texto en una BD Vectorial (Qdrant/Pinecone) a manera de Prompt Few-Shot "Context Memory" local, nunca "re-entrenando la red de la IA".

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Entrenamiento Síncrono al instante (El aprendizaje es diferido para evitar picos de cómputo en Core Banking Hours).

## 7. Observaciones de Alineación o Riesgos
**Fricción Multi-Tenant:** El entrenamiento Few-Shot RAG derivado del feedback de un analista debe estar celosamente aislado bajo filtrado por Tenant. Si una compañía rechaza una tarjeta, esa "enseñanza" solo puede reflejarse a los usuarios lógicos de la misma empresa. El documento obvia alertar el particionamiento multi-cliente en el modelo de MLops, riesgo inminente de contaminación de sesgos inter-empresariales (Pérdida Confidencialidad).
