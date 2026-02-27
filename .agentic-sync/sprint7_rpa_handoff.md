# 🤖 Handoff Document - Sprint 7: Módulo RPA Python (Web Scraping Lex)

**Atención: Equipo de Agentes de Desarrollo y DevOps (Python Squad)**
Este documento contiene las especificaciones arquitectónicas y de diseño inmutables para la construcción del Robot RPA de Extracción Judicial del iBPMS (V1). Todo esfuerzo de codificación que inicies en tu sesión debe adherirse a estas instrucciones provistas por el Arquitecto Líder.

---

## 🎯 Objetivo del Sprint
Desarrollar un Web Scraper / Worker en **Python** capaz de navegar por un portal de la rama judicial (simulado o propuesto), extraer metadatos de nuevas resoluciones o notificaciones (ej. "Número de Proceso", "Demandante", "Decisión", "Fecha"), empaquetarlo en un Payload JSON y enviarlo por método `POST` al Webhook Inbound de nuestro Backend Java.

## 🏛️ Restricción Arquitectónica (CRÍTICA)
En respeto al **Contrato de Infraestructura IaaS V1 (3 VMs totales)**, tenemos recursos de Memoria RAM y Procesamiento de fondo limitados.
*   **PROHIBIDO:** Construir el script envolviéndolo en un servidor web estático infinito (como *Flask*, *FastAPI* o *Django*) o como un demonio `while True` que ponga el sistema a dormir.
*   **OBLIGATORIO:** Construir el aplicativo utilizando el patrón de **Contenedor Efímero (Ephemeral Job)**. El código de Python debe encenderse (ejecutando `main.py`), raspar los datos, disparar los webhooks, limpiar su rastro de memoria y ejecutar un `exit(0)` explícito para que el contenedor de Docker se apague inmediatamente.

## 🛠️ Stack Tecnológico Permitido
1.  **Motor:** Python 3.11+ o superior (Imagen Alpine o Slim para Docker).
2.  **Librerías de Extracción:** Puedes usar `BeautifulSoup4` y `requests` (para páginas estáticas). Si el portal objetivo está muy ofuscado (SPAs/Angular/React), estás autorizado a usar una librería "Headless" como `Playwright`.
3.  **Librerías de Integración:** Estrictamente la librería estándar de `requests` o `aiohttp` para disparar el mensaje JSON hacia el iBPMS Core.

## 📝 Contrato de Interoperabilidad (Output API)
Cuando el Robot de Python finalice su recolección de un documento (o lote de ellos), debe ejecutar un PUSH al sistema.
Asume que el endpoint del iBPMS se configurará por Variables de Entorno (`IBPMS_WEBHOOK_URL`).

**Estructura esperada del JSON (Payload de Integración):**
```json
{
  "origen": "RPA_RAMA_JUDICIAL",
  "tramite_id": "RAD-2026-X84-00",
  "descripcion_notificacion": "Auto que admite la demanda ejecutiva...",
  "partes": ["Banco Ejemplo S.A.", "Juan Pérez"],
  "fecha_publicacion": "2026-02-27T00:00:00Z",
  "metadata_adicional": {
      "tipo_documento": "AUTO_ADMISORIO",
      "requiere_decision_urgente": true
  }
}
```

## ✅ Pasos de Acción Inmediata para el Agente Python:
1.  En la raíz del proyecto global, crea una carpeta llamada `rpa/judicial-scraper`.
2.  Desarrolla el script principal (ej. `scraper.py`) con la inyección de dependencias necesaria, manejando correctamente las excepciones (si el portal del estado se cae, el script debe morirse con error 1 para que el sistema de logs lo registre, no colgarse iterando).
3.  Escribe el archivo `requirements.txt` y el `Dockerfile` asociado.
4.  Crea un archivo `.env.example`.
5.  Añade las instrucciones al DevOps para que levante este script orquestado como un trabajo eventual (ej. invocándolo desde un Trigger programado en Kubernetes o un Crontab del sistema de Docker local).

¡Procedan a ejecutar el Sprint 7 y reporten el éxito mediante Git Commits parciales!
