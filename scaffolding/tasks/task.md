# Arquitectura e Implementación PoC - iBPMS

- [x] 1. Validar requerimientos funcionales y técnicos con el usuario
    - [x] Separación Proceso vs Reglas (DMN)
    - [x] Asignación dinámica (Colas, RBAC, ABAC)
    - [x] Case Management y Paralelismo de eventos
    - [x] Formularios desacoplados (Micro-frontends)
    - [x] Gatillos por Correo (Exchange/Gmail)
    - [x] Integración transaccional (ERP, Bancos, Saga Pattern)
    - [x] Gestión Documental (SGDEA / MoReq) y análisis de módulo propio.
    - [x] Escalabilidad y Alto Volumen (CQRS / Stream Processing)
    - [x] Estrategia Evolutiva (V1 Azure VMs a V2 SaaS K8s vía Strangler)
- [x] 2. Consolidar Documento de Arquitectura Base (Implementation Plan)
- [x] 3. Diseñar Diagramas de Componentes (Mermaid)
- [x] 4. Crear "Solution-Architecture View" (Modelo C4: Contexto + Contenedor)
- [x] 5. Revisión y Aprobación Final del Arquitecto del Cliente

# Fase 2: Diseño de Software (Logical / Development View)
- [x] 1. Definir Estilo Arquitectónico Interno (Ej. Hexagonal / Clean Architecture)
- [x] 2. Modelar Entidades de Dominio Centrales (Expediente, Tarea, Regla)
- [x] 3. Formalizar Contratos de API (REST/OpenAPI v3)
- [x] 4. Delimitar Responsabilidades y Acoplamiento (Inversión de Dependencias)

# Fase 3: Prueba de Concepto Técnica (PoC)
- [x] 1. Documentar Arquitectura Formalmente (C4, ADR) en repositorio.
- [x] 2. Definir APIs (Puertos Primarios) YAMLs para Motor y Workspace.
- [x] 3. Diseño a Nivel de Código (Nivel 4), Entidades y Casos de uso `ProcessInstance`.
- [x] 4. Preparar entorno Java (Spring Boot) en `scratch/ibpms-poc`.

# Tareas Pendientes (QA / Deuda Técnica)
- [ ] **US-036 CA-01 (Hibridación de Roles EntraID vs Locales):** Si bien la lógica fuente (Src) se considera validada:
    - Déficit de Pruebas Unitarias / Integración: No hay rastros de asserts comprobando la transición a isExternalIdp en tests.
    - Déficit E2E: Falta un Playwright spec que simule un inicio de sesión híbrido.
- [ ] **US-036 CA-02 (El Guardián Absoluto - Root Super Admin):** Carece de pruebas de Integración y E2E Playwright. Las pruebas unitarias actuales son de Frontend (Vitest).
- [ ] **US-036 CA-03 (Clonación de Perfiles por Plantilla):** Carece de pruebas de Integración (Backend) y pruebas E2E Playwright. Las pruebas actuales se limitan a Vitest en el Frontend.
- [ ] **US-036 CA-04 (Segregación Iniciador vs Ejecutor):** Carece de pruebas Unitarias/Componente (Vitest), Integración (Backend) y E2E (Playwright). Implementado pero sin cobertura de QA.
- [ ] **US-036 CA-05 (Privacidad Visual de Colas):** Carece de pruebas de Integración (Testcontainers) que validen el filtro AspectJ/Hibernate (Row-Level Security) y pruebas E2E (Playwright) para el Workdesk multi-usuario.
- [ ] **US-036 CA-06 (Herencia de Roles Piramidal):** Carece de pruebas Unitarias/Componente (Vitest), Integración (Backend) y E2E (Playwright). El servicio fue refactorizado exitosamente para usar CTE Unificada, pero no hay tests que certifiquen el flujo piramidal.
- [ ] **US-036 CA-07 (Inmutabilidad por Desactivación Suave - Soft-Delete):** Implementado con éxito a nivel de entidad (`isActive`) y de Liquibase. Faltan pruebas Testcontainers que verifiquen que un rol borrado permanece en la BD pero no es recuperable por los endpoints de lectura públicos.
- [ ] **US-036 CA-08 (Aprovisionamiento de Transeúntes - Ciudadano Interno):** Implementado en `JwtAuthFilter` (JIT Provisioning). Faltan pruebas Unitarias (Mocks) o de Integración (Testcontainers) que afirmen que un Subject nuevo que inicia sesión con SSO recibe automáticamente el `ROLE_CIUDADANO_INTERNO`. Tampoco hay Playwright spec para este flujo inicial.
- [ ] **US-000 CA-01 a CA-04 (Arquitectura Base y Resiliencia):** La lógica base está implementada, pero requiere consolidación QA Zero-Mock.
    - Déficit de Integración (Backend): Faltan tests reales con Testcontainers para verificar respuestas 409, 400 y 500 bajo condiciones reales de BBDD, así como un test de integración para el enmascaramiento PII saliente.
    - Déficit E2E (Frontend): Carece de Playwright specs para verificar que la UI se degada grácilmente mostrando toasts 500/503 imborrables y que pinta los `validationErrors` del HTTP 400 correctamente en los formularios interactivos.
- [x] **US-001 CA-01 a CA-31 (Bandeja de Entrada Unificada):** Trazabilidad `@Traceability` inyectada en Backend (`WorkdeskQueryController`) y Frontend (`useSlaEngine`). La matriz certifica 100% de madurez. No hay deuda técnica bloqueante registrada.
