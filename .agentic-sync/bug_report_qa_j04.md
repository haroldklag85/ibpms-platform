# 🕵️ Reporte de Fallo QA - Recertificación J-04
**Fecha:** 2026-05-13
**Tarea:** T-20.5

## Descripción del Fallo
Al intentar ejecutar la suite E2E de Playwright sobre el flujo P0 (Workdesk, Kanban, Kill-Switch), el comando falló a nivel de sistema operativo debido a una restricción de políticas de ejecución de PowerShell en Windows (`PSSecurityException`).

**Traza del Error:**
```text
npx : No se puede cargar el archivo C:\Program Files\nodejs\npx.ps1 porque la ejecución de scripts está deshabilitada en este sistema.
```

## Acción Tomada
Por protocolo de resiliencia y orquestación paralela (Carril A), se reporta este fallo de infraestructura de QA como un **Bug Aislado** y se aborta temporalmente la recertificación de QA para continuar *implacablemente* con el ensamblaje de la Iteración 7.2 (J-02) en los carriles de Backend y Frontend.
