---
description: Enrutador Maestro de Certificación QA. Guía de toma de decisiones para seleccionar el workflow de prueba adecuado (UAT, E2E o API) según la metodología (ADR 011).
---

# Enrutador Maestro de Certificación QA (Selector de Flujo)

Eres el **Arquitecto Líder** o el **QA Lead** planificando el esfuerzo de certificación para Historias de Usuario (US). Para cumplir con la Gobernanza de Pirámide de Testing (ADR 011) dictada en `.cursorrules`, todas las validaciones asumen "Zero-Trust" sin usar Mocks in-memory.

Lee la Historia de Usuario actual en el SSOT y utiliza el siguiente árbol de decisión para seleccionar de manera unívoca **el workflow operativo correcto** para transferir el trabajo:

## 1. Evaluación: Naturaleza de la User Story

### A. Certificación Exclusiva Backend (APIs sin interfaz gráfica)
Si la US aborda lógica silenciosa (colas DLQ, RabbitMQ, CRON jobs) o endpoints sin pantalla de usuario:
*   **Decisión:** Ejecución de "Nivel 4 API" bajo el protocolo ADR 011 usando **REST Assured**.
*   **Acción de Enrutamiento:** Solicita invocar `/auditoriaIntegralUSDesarrollo`, pero añadiendo la instrucción explícita: *"QA debe validar el flujo usando la suite de REST Assured ya que no existe interacción frontend"*.

### B. Certificación de Interfaces (Flujos UAT con Frontend)
Si la US involucra paneles, formularios o vistas Vue, debes elegir la estrategia correcta basándote en la instrucción del sprint:

#### 1. Auditoría Estática Transversal (El Inspector)
*   **Condición:** *"Necesitamos un mapa detallado para ver si se construyó todo en Back, Front y Tests, antes de interactuar funcionalmente."*
*   **Workflow a Enrutar:** `/auditoriaIntegralUSDesarrollo`
*   **Enfoque:** Análisis de código y trazabilidad entregado en tabla Markdown.

#### 2. UAT Táctico Empírico (Modo Cazador)
*   **Condición:** *"QA debe probar el flujo levantando los Dockers, buscando bugs (feliz/triste) de manera profunda en las 4 capas, reportando evidencias forenses."*
*   **Workflow a Enrutar:** `/pruebasUatE2e`
*   **Enfoque:** Validación humana delegada al Agente.

#### 3. UAT Muestreo en Vivo (Modo Showcase)
*   **Condición:** *"El Product Owner humano solicitó VER la prueba explícitamente en modo narrado y comprobado."*
*   **Workflow a Enrutar:** `/pruebasUatVisibles`
*   **Enfoque:** El Agente QA interactúa y narra al usuario paso a paso con máxima transparencia.

#### 4. Automatización SDET (Regresión Permanente Playwright)
*   **Condición:** *"La US ya es estable y el arquitecto requiere transformar los Criterios de Aceptación en código para el CI/CD."*
*   **Workflow a Enrutar:** `/pruebasUatVisiblesAutomatizadas`
*   **Enfoque:** Construcción técnica de scripts `.spec.ts` y lanzamiento gráfico en Playwright UI.

---

## 2. Emisión de Directriz

Una vez finalizada la selección usando este manual, detener todo y comunicarse con el Usuario (Humano) así:

> *"He evaluado la Historia de Usuario [X] a través del Enrutador Maestro QA. Dado su contexto, he seleccionado el camino estratégico: [OPCIÓN ELEGIDA]. 
> Por favor, ejecuta el comando `[CÓDIGO_DEL_WORKFLOW]` para inicializar la sala limpia del Agente especialista."*
