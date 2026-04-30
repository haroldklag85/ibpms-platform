# Handoff: Agente QA - Certificación Suite J-04 (53 Escenarios E2E)

Bajo la autoridad del Arquitecto Líder, te encomiendo la ejecución de la suite de validación pendiente (remanente de la Iteración 6.1). Esta suite aborda las fallas detectadas en la evaluación manual y abarca el Journey J-04 completo.

## 🎯 Objetivo de la Misión
Ejecutar los 53 Escenarios E2E para certificar el **Journey J-04** y prepararnos para el UAT Release.

## 🚫 Gobernanza Arquitectónica (Zero-Mock Enforcement)
1. **Sin Intercepciones:** Todo el tráfico HTTP, autenticaciones y validaciones de formulario deben cursar hacia el backend real (Docker Compose) sin interceptores de red. **Queda terminantemente prohibido el uso de `page.route()`, `context.route()` o simuladores.**
2. **Data Seed Genuino:** Asume que la base de datos ya cuenta con el "data seed" poblado (usuarios, catálogos, configuraciones SLA iniciales). No debes inyectar datos falsos en el LocalStorage para bypasear validaciones.

## 🚀 Instrucciones de Ejecución
Deberás localizar los archivos de pruebas (`*.spec.ts`) correspondientes al Journey J-04 en el directorio de Playwright (`frontend/e2e/`).

1. Lanza los tests bajo el perfil validado:
   ```bash
   npx playwright test e2e/certification/smoke-j04-operario.e2e.spec.ts --project="Zero-Mock-E2E"
   ```
   *(Ajusta la ruta de acuerdo a cómo estén segmentados los 53 escenarios en la suite E2E)*.

2. Documenta cualquier fallo de Timeout, Assertion Error o Status 500 en el archivo `docs/sprints/sprint_6_bugs.md` con su respectivo Trace y Bug ID. 
3. No intentes enmendar los fallos; tu labor es evidenciarlos objetivamente.

Procede con la ejecución y entrégame el reporte técnico y el veredicto final.
