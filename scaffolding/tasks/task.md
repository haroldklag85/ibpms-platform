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
