# 🏗️ Handoff: Backend - Completar Topología de Menú Dinámico (Server-Driven UI)

## 1. Metadatos y SSOT
- **Iteración/Sprint:** 6.2-DEV
- **User Story / Tarea:** Resolución de visibilidad de módulos (Super Admin Sidebar).
- **Path del SSOT:** `frontend/src/router/index.ts`
- **Flujo de Trabajo:** Backend -> QA.

## 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs:** El diseño *Server-Driven UI* dictamina que el Backend tiene la autoridad absoluta sobre qué elementos del menú se pintan según los roles del usuario. Actualmente, la clase `MenuLayoutUseCase.java` viola la escalabilidad del sistema al tener solo 3 ramas quemadas en código y estar fuertemente desfasada del enrutador frontend.
- **Trazabilidad de la Solución:** Debemos inyectar todos los módulos principales correspondientes al `ROLE_SUPER_ADMIN` como nodos padre en el DTO de respuesta, para que el componente `MainLayout.vue` los pinte automáticamente.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo Objetivo:** `backend/ibpms-core/src/main/java/com/ibpms/poc/application/usecase/ui/MenuLayoutUseCase.java`
- **Estado Actual:** El caso de uso despacha "Inicio", "Mi Workdesk" y una carpeta de "Administración y Gobernanza". Faltan 6 grupos principales que agrupen las rutas huérfanas.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Debes modificar el método `getBuildLayoutForUser` en `MenuLayoutUseCase.java` y asegurarte de inyectar estos grupos (para `ROLE_SUPER_ADMIN`), ya sea como raíces o como carpetas.

Recomendación Prescriptiva de Taxonomía de Nodos Padre (al mismo nivel de 'Inicio' y 'Mi Workdesk'):

```java
if (userRoles.contains("ROLE_SUPER_ADMIN") || userRoles.contains("Global Admin")) {
    
    // RAMA: Service Delivery
    MenuItemDTO serviceDelivery = new MenuItemDTO("Service Delivery", "mdi-account-group", null);
    serviceDelivery.addChild(new MenuItemDTO("Triaje Intake", "mdi-filter", "/intake-triage"));
    serviceDelivery.addChild(new MenuItemDTO("Intake Manual", "mdi-text-box-plus", "/admin/intake"));
    serviceDelivery.addChild(new MenuItemDTO("Customer 360", "mdi-account-details", "/admin/customer360"));
    layout.add(serviceDelivery);

    // RAMA: Project Builder
    MenuItemDTO projectBuilder = new MenuItemDTO("Project Builder", "mdi-rocket", null);
    projectBuilder.addChild(new MenuItemDTO("Project Builder", "mdi-hammer-wrench", "/admin/project-builder"));
    projectBuilder.addChild(new MenuItemDTO("Gestor de Proyectos", "mdi-view-dashboard-variant", "/admin/projects/manager"));
    projectBuilder.addChild(new MenuItemDTO("Agile Hub", "mdi-chart-timeline-variant", "/admin/projects/agile-hub"));
    layout.add(projectBuilder);

    // RAMA: Analytics & BAM
    MenuItemDTO analytics = new MenuItemDTO("Analytics & BAM", "mdi-chart-bar", null);
    analytics.addChild(new MenuItemDTO("Dashboard BAM", "mdi-monitor-dashboard", "/admin/analytics/bam"));
    layout.add(analytics);

    // RAMA: Integration Hub
    MenuItemDTO integration = new MenuItemDTO("Integration Hub", "mdi-api", null);
    integration.addChild(new MenuItemDTO("Catálogo de Conectores", "mdi-book-open-page-variant", "/admin/integration/catalog"));
    integration.addChild(new MenuItemDTO("Connector Builder", "mdi-puzzle-edit", "/admin/integration/builder"));
    integration.addChild(new MenuItemDTO("Visual Mapper", "mdi-sitemap", "/admin/integration/mapper"));
    integration.addChild(new MenuItemDTO("DLQ Dashboard", "mdi-alert-octagon", "/admin/integration/dlq"));
    layout.add(integration);

    // RAMA: SGDEA
    MenuItemDTO sgdea = new MenuItemDTO("SGDEA", "mdi-folder-lock", null);
    sgdea.addChild(new MenuItemDTO("Bóveda Documental", "mdi-safe", "/sgdea/vault"));
    layout.add(sgdea);

    // RAMA: Gobernanza e Identidades
    MenuItemDTO governance = new MenuItemDTO("Gobernanza", "mdi-gavel", null);
    governance.addChild(new MenuItemDTO("Gobernanza de Identidades", "mdi-card-account-details", "/admin/security/identity"));
    governance.addChild(new MenuItemDTO("PMO / SLA Management", "mdi-timer-settings", "/admin/pmo/settings"));
    layout.add(governance);
}
```

*Importante:* Asegúrate de importar cualquier dependencia extra y compilar.

## 5. Matriz de QA y Testing Atómico
**Prueba Unitaria (MenuLayoutUseCaseTest.java):**
- **Validación:** Modificar o crear la prueba unitaria para validar que cuando el rol inyectado sea `ROLE_SUPER_ADMIN`, la respuesta devuelva un layout con al menos **8 elementos en la raíz** (Inicio, Workdesk, y los 6 grupos listados arriba).

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)
**Para Agente Backend:**
> "Refactorización Server-Driven UI obligatoria. Ejecuta las adiciones en `MenuLayoutUseCase.java` para proveer los 6 grupos faltantes al enrutador frontend. Recuerda correr el protocolo Zero-Trust SRE de compilación en el contenedor docker antes de finalizar."
