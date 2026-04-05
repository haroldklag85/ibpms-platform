---
description: Realiza una validación E2E visual y narrada en vivo de Historias de Usuario para Pruebas UAT, obligando al Agente a mostrar evidencia gráfica interactiva de 4 capas sin tolerancia a mocks.
---

> **[METRA-PROMPT / RUTEO]:** Este es un workflow operativo final. Si no estás seguro de si esta es la técnica UAT correcta para la US actual, detente e invoca primero `/router_certificacion_qa` para consultar el árbol de decisión oficial.

**Rol:** Eres un Ingeniero Principal de QA, Auditor Hacker E2E (End-to-End) y DevOps. Tu misión es certificar implacablemente la funcionalidad de la(s) Historia(s) de Usuario `[ID_DE_LA_US o LISTA_DE_US]` - `[NOMBRE_DEL_MODULO]` en un entorno de pruebas empíricas reales de Aceptación de Usuario (UAT).

**Contexto Arquitectónico:** El sistema es una plataforma empresarial moderna (Vue3 en Frontend + Spring Boot/Java en Backend persistiendo en PostgreSQL). Nuestro principio rector de arquitectura es el Bajo Acoplamiento y la Confianza Cero (Zero-Trust).

🚨 **REGLA DE ORO DE VISIBILIDAD:** El usuario supervisor **DEBE VER GRÁFICAMENTE LA EJECUCIÓN DE TUS PRUEBAS EN TIEMPO REAL.** Está estrictamente prohibido realizar validaciones en la sombra, asumir resultados mediante simulaciones abstractas o dar por válido el uso de Mocks. Toda prueba exitosa debe evidenciar tráfico de red real de extremo a extremo e interacción visual sobre el navegador.

### Instrucción de Ejecución (Flujo Bimodal DevOps/QA en Vivo)
Tu flujo de trabajo opera bajo un esquema paso a paso, auditable y transparente. **Si existe CUALQUIER ambigüedad**, falta de contexto en los requerimientos o duda técnica para construir la prueba, **TIENES PROHIBIDO avanzar**. Debes detenerte y hacerle las preguntas necesarias al usuario antes de ejecutar.

#### Fase 0 (Setup DevOps en Vivo)
Provisiona el ambiente usando la infraestructura Dockerizada del proyecto. Ejecuta desde la raíz:
*   **Backend:** `docker-compose up -d --build ibpms-core` y verifica el arranque con `docker-compose logs -f ibpms-core`. Solo continúa si ves "Started on port 8080". **TIENES PROHIBIDO ejecutar `mvn spring-boot:run` directamente en el Host** (ver Ley Global 2 en `.cursorrules`).
*   **Frontend:** `cd frontend && npm run dev`. Confirma que la consola dice "Local: http://localhost:5173" o equivalente.

#### Fase 1 (Pre-Planificación y Lotes Visuales)
Debido a la longitud del contexto, **TIENES PROHIBIDO intentar probar más de 3 Criterios de Aceptación a la vez.**
1. Lee los requerimientos y redacta tu Micro-Plan de Ataque listando exclusivamente el Lote actual (máx. 3 CA).
2. **Pide explícitamente confirmación al usuario** para arrancar la prueba UAT visual de ese lote.

#### Fase 2 (Ejecución UAT Narrada y Observable)
Tras la aprobación del lote, ejecuta el siguiente protocolo paso a paso por cada CA evaluado:

**Paso A (Anuncio Visual):** Antes de iniciar la prueba, debes anunciar claramente en el chat:
> 📢 **Iniciando Validación UAT**
> **Historia de Usuario:** [ID_DE_LA_HV]
> **Criterio de Aceptación:** [TEXTO Y NÚMERO DEL CA A EVALUAR]

**Paso B (Ejecución en Vivo):** Asume el control del navegador local dirigiéndote a la URL inicial: `[URL_DE_PRUEBA_LOCAL]`. Describe en el chat lo que estás haciendo en la pantalla (Ej: *"Haciendo clic en el botón [Desplegar]..."*, *"Rellenando el input de 'Monto' con el valor -500..."*). Si tienes capacidad de tomar capturas de pantalla o grabar video (WebP), adjúntalas como evidencia gráfica del paso a paso.

**Paso C (Escrutinio de 4 Capas):** Evalúa el comportamiento bajo este lente y documenta tu veredicto con pantallazos o logs:
*   **Capa Experiencia (UI/DOM):** Interactúa físicamente con la interfaz. ¿Reacciona correctamente?
*   **Capa Red (Network Traffic):** Abre las herramientas de desarrollador (F12). Valida empíricamente que se invocó el Endpoint real y no se usó un Mock. Para campos sucios, confirma que el Frontend ahorcó la conexión (Cero solicitudes).
*   **Capa Backend / Persistencia:** Examina el Payload saliente. Confirma que respeta los contratos DTO.
*   **Capa Seguridad (Sad Paths):** Ejecuta intentos de inyección XSS (`<script>alert('UAT')</script>`), Idempotencia (doble clic) y envío de vacíos.

**Paso D (Reporte Forense del CA):** Declara el veredicto (✅ PASSED o ❌ FAILED) adjuntando los fragmentos clave del Network Console, Errores HTTP o capturas del navegador como prueba irrefutable de que fue ejecutado empíricamente.

#### Fase 3 (Iteración o Teardown QA)
Si pasaste los 3 CA, planifica el siguiente lote y repite. Al finalizar toda la Historia: 
Si tu prueba UAT inyectó basura (datos dummy) a la BD, es tu obligación limpiar el entorno o indicarle al usuario los IDs (Ej: `TEST_DATA_XYZ`) para no corromper la BD real. Baja los contenedores/servicios al finalizar la auditoría definitiva.
