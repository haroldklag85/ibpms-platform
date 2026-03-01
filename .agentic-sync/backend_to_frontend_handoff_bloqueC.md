# Handoff Bloque C: Backend -> Frontend (Analítica & Dashboards)

**Ref:** US-009, US-018
**Estado:** Endpoints Codificados en el API (Zero-Trust Rest APIs).

El Agente de Backend ha finalizado el modelo de recolección de métricas. El Frontend puede consumir estos endpoints para alimentar los _charts_ de Vue (Ej. Chart.js, Recharts, ECharts).

## 1. Process Health (BAM Camunda) - US-009
Expone el estado bruto e índice de SLAs del clúster Camunda.

* **Endpoint:** `GET /api/v1/analytics/process-health`
* **Auth:** Obligatoria JWT (Rol: `Role_Directivo` o `Role_Admin_Intake`)
* **Retorno (JSON):**
```json
{
  "activeCases": 120,
  "completedCases": 3400,
  "activeTasks": 450,
  "overdueTasks": 15
}
```

## 2. Métricas de IA y Copiloto - US-018
Expone el comportamiento del módulo NLP/Llama3 sobre las automatizaciones y auditorías.

* **Endpoint:** `GET /api/v1/analytics/ai-metrics`
* **Auth:** Obligatoria JWT (Rol: `Role_Directivo` o `Role_Admin_Intake`)
* **Retorno (JSON):**
```json
{
  "totalAiEvents": 2540,
  "generatedDmns": 421,
  "autoRoutedEmails": 2119,
  "averageSimilarityScore": 0.942
}
```

**Instrucción UI:** Para el score de similitud vectorial (`averageSimilarityScore`), mapeen esto en el frontend como un "Índice de Certeza de la IA" en formato porcentaje (Ej. 94.2%).
