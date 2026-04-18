# Análisis Funcional Definitivo: US-010 (Generar y Descargar PDF a partir de datos del caso SGDEA)

## 1. Resumen del Entendimiento
La US-010 transforma metadata estructurada JSON que viaja en un proceso de negocio, y la inyecta mecánicamente en plantillas Word (`.docx` `<<variables>>`) para parir documentos Legales (PDFs inmutables) evadiendo costosos desarrollos "Pixel Perfect" mediante FOP. Provee soporte PKI (Firmas) y trazabilidad forense (SHA-256).

## 2. Objetivo Principal
Mitigar el riesgo Humano "Copy-Paste" corporativo donde un practicante redacta mal un contrato basándose en versiones Excel obsoletas. Todo Acto Administrativo que nace libre de alteraciones humanas reduce demandas operativas en un 99%.

## 3. Alcance Funcional Definido
**Inicia:** Ante un Trigger de Camunda REST (UserTask "Generar" o ServiceTask Automático).
**Termina:** Con el PDF fabricado, validado criptográficamente y expuesto en SharePoint o eliminado si su vida fue "Efímera" (Modo Consulta Cero Retención).

## 4. Lista de Funcionalidades Incluidas
- **Plantillaje Variable:** Reemplazo directo y cíclico (loop ForEach en arrays).
- **Tolerancia N/A:** Interpolación resiliente si la variable de proceso fue omitida en el form.
- **Bifurcación de Persistencia:** (EPHEMERAL = Muere en 15 minutos / PERSISTENT = SharePoint Vault forzoso).
- **Hardening Legal (PKI X.509):** Estampado interno de firma digital al instante del render y sello visual para evidenciar No-Repudio.
- **Viaje en el Tiempo Documental:** Búsqueda teórica de Plantillas pasadas para renderizaciones antiguas.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Legalidad Criptográfica Corrompida (⚠️ CRÍTICO JURÍDICO CA-6):** Dicta que cuando un auditor visite un caso de hace 2 años para leer su contrato, el motor buscará su "Plantilla Histórica V1" y lo *Re-Renderizará en vivo* ("Time-Travel Rendering"). **GAP FATAL:** Jurídicamente, si un documento "PERSISTENT" nació hace 2 años, obtuvo una firma PKI y un cert-timestamp criptográfico emitidos ese día. Si nosotros "ensamblamos" artificialmente el contrato HOY asumiendo los JSON del ayer, el PDF resultante será técnicamente nuevo. Carecerá de la firma electrónica original que rubricó el cliente y poseerá un hash SHA-256 distinto. Para procesos en modo PERSISTENT, volver a renderizar es ilegal: el auditor *debe* descargar físicamente el binario original alojado en SharePoint (US-035).

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Llenado o extracción inversa de PDF (OCR para sacar data del PDF al JSON = US-035 Scope TBD).
- Visualización Online nativa (Rechazada a favor del visualizador de Entra O365 embebido / Iframe CA-7 en US-035).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Crítico:** Renderizar *Time Travel* es solo aplicable a variables "Efímeras". Todo flujo cerrado Persistente está estrictamente amparado bajo la directiva forense SGDEA, requiriendo recuperar el binario inmutable original, lo cual anula y choca rotundamente con este CA-6 en modo PERSISTENT.
