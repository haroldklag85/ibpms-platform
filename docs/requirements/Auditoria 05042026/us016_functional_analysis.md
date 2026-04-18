# Análisis Funcional Definitivo: US-016 (Gestión Multi-Buzón con Políticas Locales)

## 1. Resumen del Entendimiento
La US-016 rige la "Parámetrización del Agente Cognitivo", permitiendo al PO modular la sensibilidad de la IA (Nivel Paranoia / Confidence Score) desde el UI y habilitar/deshabilitar flujos táctiles sin tocar el código fuente Java. Instala las "Reglas de Ingesta" de un correo o endpoint.

## 2. Objetivo Principal
Ofrecer Control de Grano Fino. Un buzón de "Demandas Legales Jurídicas" requiere máxima precisión (Confidence 95% para sugerir) vs un buzón "Info General SAC" (Confidence 70%).

## 3. Alcance Funcional Definido
**Inicia:** El Backend carga los settings del buzón desde Postgres (`ibpms_mailbox_config`).
**Termina:** El LLM ejecuta el análisis, inyectando las barandas pre-fijadas.

## 4. Lista de Funcionalidades Incluidas
- **Refresco Config en Runtime (CA-1):** Cambiar de tono formal a pasivo afecta al próximo mail instantáneamente (Caché evited/flushed).
- **Control de Tokens (Toggle IA CA-2):** Master Switch para apagar el RAG y tener buzones "tontos" ahorrando plata (No consume OpenAI en spam accounts).
- **Lista Blanca de Catálogos (CA-3):** Los correos de `tecnico@` solo pueden parir procesos SD del Tipo "Fallas Físicas", impidiendo que la IA alucine promoviendo crear un trámite "Facturación".
- **Umbral Confianza Dinámico (CA-4):** Score Mínimo (0-100%). Menor a esto lanza la Incompetencia del Fallback US-015 (Agente ciego en blanco).
- **Tono + Default Signature (CA-6):** Inyecta Firma corporativa (disclaimer de uso legal).
- **Bilingüe Idioma Puro (CA-7):** Procesarlo en el idioma en que entró (EN se queda en EN).
- **Alarma Caducidad OAuth/IMAP (CA-8):** Pantalla 15.B marca "Rojo" si MS Graph revoca Token de Lectura.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Aislamiento RBAC vs Visibilidad Genérica (⚠️ CA-5):** Decreta un "Enrutamiento Táctico" por buzón: Exige que un correo sea visualizado "*EXCLUSIVAMENTE a los usuarios que posean el Rol/Dueño asociado a ese buzón (RBAC)*". **GAP:** En la Pantalla 1B (US-011), existe una barra universal "Search All". Si el motor indexa universalmente los correos (Elasticsearch index global) y no aplica Forzado Hard-Coded de Roles por Documento, un analista de 'Soporte' podrá buscar la palabra "Demanda Legal" e interceptará el correo confidencial que iba al Dueño/Rol del Buzón de Legal. La seguridad perimetral a nivel "Document Elasticsearch" en la indexación no está definida. Elastic devolverá todo (Privilege Escalation a nivel de Lectura Global).

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Integración de Conectores de WhatsApp u Omni-Canalidad. Se restringe a Emails (Integración Office/Microsoft).

## 7. Observaciones de Alineación o Riesgos
**Saturación Operativa Local:** Obligar la inserción de Firmas (CA-6) forzándolas por el "Borrador In-Vivo UI" del LLM gasta severos Tokens Predictivos, ralentizando la respuesta. La firma corporativa estática JAMÁS debe obligarse al agente generativo para redactar, debe añadirse post-generación en el componente visual como un string literal anexionado (Concatenación String). Dejar que el LLM tipeé la firma corporativa o los Disclaimers de privacidad causará aberraciones y fallas de cumplimiento a la ley de Data (Habeas Data).
