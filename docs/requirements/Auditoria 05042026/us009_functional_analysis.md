# Análisis Funcional Definitivo: US-009 (Visualizar Salud del Proceso - BAM Dashboard)

## 1. Resumen del Entendimiento
La US-009 engrana a Grafana como motor de visualización (Business Activity Monitoring - BAM) empotrando Dashboards directamente en la Pantalla 5 de iBPMS. Aprovecha JWT para autenticación pasante y obliga a usar esquemas planos de datos para no colapsar el motor Camunda con reportes.

## 2. Objetivo Principal
Dar a la Gerencia una cabina de control táctica para leer el "estado del arte" de los procesos (atascos operativos, SLA's, volumetría). Lo hace democratizando Grafana sin requerir navegar fuera de la herramienta.

## 3. Alcance Funcional Definido
**Inicia:** El Gerente de Operaciones abre la Pantalla 5 y pide un KPI (Ej: Tareas Atrasadas).
**Termina:** El Frame carga mediante proxy Auth, se levanta la analítica Multi-Tenant, y permite un Click-Through para navegar al Workdesk con filtros aplicados (Drill-down).

## 4. Lista de Funcionalidades Incluidas
- **Grafana Embedding:** Proxy OAuth/JWT emitiendo rol "viewer" a un iframe inyectando `TenantID`.
- **Aislamiento Multi-Tenant:** RLS o WHERE en los SQL para no cruzar información de inquilinos.
- **Interactive Drill-Down:** Event interceptor de Grafana a Vue Router.
- **Asincronicidad DB (Anti-DDoS):** Consultas jamás hechas contra Tablas Vivas; se lee de Replica Read-Only o DWH (Actualización cada 10m).
- **Prohibición de BLOB Scan (Flattening):** Microservicio (CDC) mastica el JSON variable en `ACT_RU_VARIABLE`, extrayendo campos clave y depositándolos en columnas planas rígidas.
- **Editor Embebido (Self Service):** Para que gerentes jueguen con métricas nuevas sin desarrollo IT.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Desincronización Cognitiva de Capas UX (⚠️ CA-5 vs CA-3):** El CA-5 de protección SRE exige que los datos de Grafana provengan de una réplica asincrónica ("atrasada" hasta por 10 minutos). El CA-3 exige que al hacer clic en un dashboard que dice "15 Tareas Atrasadas" (Drill-Down), la app redireccione a la Pantalla 1 y pre-filtre esas 15 tareas en TIPO REAL. ¿Qué ocurre si un operario acaba de completar 5 de esas tareas hace 3 minutos? La DB Máster de la Pantalla 1 mostrará 10 Tareas. El gerente creerá que hay un "Bug en el sistema visual" porque el Dashboard dice 15, y la Tabla dice 10. Se estipula insertar un "Timestamp Note" en el Dashboard (*Última Actualización 10:04*) para evitar pánico de nivel gerencial.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógica Forecast de predicción predictiva de carga de trabajo (AI Forecasting diferido a V2).
- Construcción in-app de paneles, delegada toda a Grafana engine.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Operativo Mínimo:** Obligar el CDC flattening (CA-7) exige una estructura de Base de Datos inmensamente resiliente. Si el bus de mensajería falla, las métricas darán $0 para los días del incidente. Obliga la creación de Retry DLQs asíncronos para el extractor.
