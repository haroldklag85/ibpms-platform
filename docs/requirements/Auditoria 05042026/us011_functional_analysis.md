# Análisis Funcional Definitivo: US-011 (Filtrado Transversal en Bandeja Avanzada - Docketing)

## 1. Resumen del Entendimiento
La US-011 es la Pantalla de Visualización (Inbox) de SAC. Opera como un buzón inteligente supercargado (tipo Zendesk) que no solo permite filtrar por Cliente/Asunto, sino que consume los metadatos inyectados invisibles por la Inteligencia Artificial (Sentiment, Classification) para ordenar urgencias sin intervención humana previa.

## 2. Objetivo Principal
Eliminar el "Cherry-Picking" (analistas escogiendo los correos fáciles). Al aplicar Weighted FIFO y ordenamiento por Semáforo de Riesgo (IA), el sistema fuerza a los analistas a atender primero las amenazas legales o usuarios furiosos.

## 3. Alcance Funcional Definido
**Inicia:** Entrando a Pantalla 1B.
**Termina:** Haciendo clic en un correo bloqueándolo en BD para su visualización.

## 4. Lista de Funcionalidades Incluidas
- **Queries Relacionales (CA-1):** Filtrado por tags en `ibpms_metadata_index`.
- **Booleans IA (CA-2):** Filtrar si IA ya mandó Acuses de recibo o no.
- **Triage (CA-3):** Sorting dinámico por Sentiment Score.
- **Filtrado Tipo Documento (CA-4):** Filtro ciego (No extensión del adjunto, sino "Es un Contrato").
- **Full Text Search (CA-6):** Búsqueda ES en body y anexos.
- **Soft Lock Concurrencia (CA-7):** Prevención de doble toque humano para tickets de SAC.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Promesa Falsa (Ruptura de MVP) (⚠️ CA-6 vs CA-9 de US-035):** Exige imperativamente que la búsqueda profunda ES (Elasticsearch) indexe el "texto interior de los anexos gracias al OCR". Sin embargo, durante el escrutinio a la US-035 certificamos que la Capacidad Neuronal OCR fue DIFERIDA A V2 en su CA-9 (manejándose fotos e imágenes en V1 como simples Binary Blobs). **GAP:** Es una falacia funcional que el Frontend ofrezca un motor de búsqueda profundo a nivel "Adentro de Anexos" si el Backend y el Data Pipeline no poseen OCR contratado en la V1. Esto generará "falsos negativos", donde el término "Indemnización" está escrito en un JPG adjunto, el usuario lo busca, y la DB dice que no existe. Obliga a remover el alcance OCR de este CA inmediatamente.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Auto-Asignación Round Robin de analistas (CA-7 se limita a Soft-lock de auto-consumo - pull model).

## 7. Observaciones de Alineación o Riesgos
**Densidad Elasticsearch:** Sostener una Full Text Search con re-indexamiento asíncrono significa costo de infraestructura Opex inflado. Si Elasticsearch falla, la bandeja 1B quedará inoperante paralizando toda la compañía de SAC.
