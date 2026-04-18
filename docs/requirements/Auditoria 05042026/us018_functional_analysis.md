# Análisis Funcional Definitivo: US-018 (Métricas de Desempeño Inteligente AI / BAM)

## 1. Resumen del Entendimiento
La US-018 busca visibilizar en Pantalla 5 el comportamiento, telemetría y eficiencia macro del iBPMS, haciendo foco en qué tanta injerencia y rentabilidad logran los Agentes Copilot Integrados o los Motores de Background.

## 2. Objetivo Principal
Exponer el ROI de la unidad de negocio Inteligente del iBPMS mediante KPIs en Dashboards centralizados, consolidando información asíncrona de las Vistas planas de CQRS.

## 3. Alcance Funcional Definido
**Inicia:** Lectura pasiva de `ibpms_business_metrics_flat` y logs transaccionales por la interfaz Grafana o Pantalla 5 Integrada.
**Termina:** Visualización de métricas y exportación Baseline ROI.

## 4. Lista de Funcionalidades Incluidas
- **Tasas de Intervención (CA-2098):** Aceptaciones (No-Touch automation), edición y rechazo humano de tareas auto-inferidas.
- **Agrupamiento Analítico:** Categorizar eficiencias por idioma, buzón, "Baseline histórico antes vs después".
- **Fallos Third-Party:** Contar tiempos perdidos o timeouts con integraciones corporativas (CRM).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Especificación Difusa de Telemetría (GAP Analítico Menor):** A diferencia de historias previas con profundidad extrema técnica, el Gherkin se restringe a "visualiza la métrica X" y asume orígenes. No se específica la fórmula matemática para definir "Retorno de Inversión" en "Tiempo/Tokens" contra "Horas Humano Ahorradas" (que es el fin real de plataformas Agentic AI). Dependerá enteramente de la libre decisión del desarrollador BAM o del Ingeniero BI acoplado.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Modelos predictivos ML Dashboards (Forecasting de ventas / Cargas). Todo esto es analítica retrospectiva.

## 7. Observaciones de Alineación o Riesgos
La US-018 confirma que existe un ecosistema de explotación desacoplado. Deberá basarse en la arquitectura CQRS aplanada dictada en la US-017 (Data Change Capture / Flat Tables) para que los Querys en Joins que hace la UI BAM no tumben a los usuarios haciendo Data Entries en las zonas transaccionales core. La resiliencia está asegurada arquitectónicamente.
