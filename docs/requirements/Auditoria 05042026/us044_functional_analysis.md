# Análisis Funcional Definitivo: US-044 (Gobernanza de Inteligencia Artificial IA Limits & MLOps)

## 1. Resumen del Entendimiento
La US-044 representa el panel de control C-Level sobre la Autonomía Humana y Robótica del iBPMS. Súper Administradores pueden encender/apagar el "Piloto Automático" cognitivo (Tolerance Score), auditar el comportamiento AI-XAI y gobernar el reciclaje matemático (Garbage Collection Vectorial).

## 2. Objetivo Principal
Prevenir rebeliones de inferencia, Alucinaciones y Sobrecostos Cloud Vectoriales dotando a la gerencia de frenos hidráulicos, Mutex de exclusión (ShedLock) y listas negras implacables.

## 3. Alcance Funcional Definido
**Inicia:** Interacción del CISO/PMO en Pantalla 15.A.
**Termina:** El motor LangChain / PGVector inactiva vectores, retrocede Snapshots N-1, o frena su operación asíncrona Nocturna.

## 4. Lista de Funcionalidades Incluidas
- **Toggle Master Auto-Pilot Anti-Retroactivo (CA-4189):** Puede apagar la IA. Lo que cayó en buzón Humano no se "auto-procesará mágicamente" si enciendes la máquina otra vez.
- **Tolerancia Certeza Paramétrica UI (CA-4196):** El Confidence Threshold ya no está harcodeado en `.application.properties` (ej 0.82), reside vivo en el front-end y reza a tiempo real.
- **Blue-Green Data Swapping N-1 (CA-4220):** Rollback Inmediato SQL de vectores `is_active_model`. Rescata del "Cierpe de Inteligencia Degradada" devolviéndola al estado de ayer. 🌿 (Poderoso Feature MLOps).
- **GC Hard-Delete Vectorial Semanal (CA-4227):** Dominguero Purga > 7 Days evita AWS Bill Explosion.
- **ShedLock Mutex Job Overlapping Protection (CA-4235):** Evita colapso de RAM de EC2 si un Job Batch Diario asíncrono AI choca con el de ayer Inconcluso.
- **Lista Negra Input Ciberseguro Normalizado (CA-4252):** Evita fallos de trim/toLowerCase antes de DB COMMIT.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Alucinación Categórica de Hardware/Caché en XAI (⚠️ CA-4210):** Proclama que un "Negative Prompt" viaja mágicamente hacia la *"caché de memoria RAM del LLM"* para ejercer Amnesia Inmediata de los Patrones. **GAP GRAVE CIBERNÉTICO:** Los LLM de inferencia externa en V1 (OpenAI, Gemini, Claudie, DeepSeek V3) NO poseen una "memoria RAM cacheada per-tenant compartida" donde lanzar Negative prompts huérfanos. Las APIs LLM son endpoints sin-estado (Stateless REST). La única manera arquitectónica y realista de producir esa "Amnesia Cero Segundos" es que el **Backend de Spring Boot cargue y concatene este Negative Prompt en el System Instruction Envelope** durante *cada llamado* que el Auto-Pilot despache hacia el API. El documento confunde la RAM física del modelo (Alojada en GPUs de Google/Microsoft) contra el Cache del Framework Backend (LangChainJ).

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Reversión de modelos N-N (Ayer de ayer, de ayer). Solo permite la restauración tipo Blue-Green a "1 paso atrás" (N-1).

## 7. Observaciones de Alineación o Riesgos
La usabilidad del "AI Audit Log" donde un Micro-LLM auxiliar (XAI) "Traduce vectores a jerga legibilidad" (CA-4205) es un consumo doble de Token. Cada inferencia o re-entrenamiento consume LLM1 (Generador) y luego LLM2 (XAI Explicador). Duplica fuertemente los costos base del API solo para darle interfaz visual al CISO, pero es un requerimiento válido e innovador si el Budget de Cloud lo asimila.
