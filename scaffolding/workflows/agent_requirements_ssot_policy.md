---
description: Política de Lectura y Comprensión de Requerimientos (SSOT) para Agentes de IA
---

# 📖 DIRECTRIZ OBLIGATORIA PARA AGENTES: Consumo del Alcance V1 (SSOT)

**Atención a todos los Agentes de IA (Arquitectos, Desarrolladores Back/Front, QA) operando en el entorno `ibpms-platform`:**

Para evitar alucinaciones operativas, desviación del alcance (Scope Creep) o redundancia documental, el proyecto ha consolidado todos los requerimientos funcionales en una **Única Fuente de Verdad (Single Source of Truth - SSOT)** minimalista. 

**ANTES de sugerir, refactorizar o escribir cualquier línea de código (Vue 3 o Spring Boot Java), usted DEBE:**

## 1. Localizar la Bóveda de Requerimientos
Usted SOLO debe basar sus asunciones funcionales en el contenido exacto de la siguiente ruta:
`docs/requirements/`

Cualquier documento fuera de esa ruta (o dentro de la subcarpeta `future_roadmap/`) **NO APLICA** para el release actual (V1 MVP) y no debe ser construido todavía.

## 2. Jerarquía de Lectura Obligatoria

Si usted tiene una duda sobre qué construir o cómo validar una feature, debe consultar los siguientes 4 documentos en este orden jerárquico estricto:

### Nivel 1: `docs/requirements/functional_requirements.md` (El "Qué" y el "Por Qué")
- **Cuándo leerlo:** Antes de iniciar cualquier macro-tarea (Épica).
- **Para qué sirve:** Define el Product Requirements Document (PRD). Le explicará el contexto de negocio, el problema que resolvemos y agrupa las características principales (Formularios Dinámicos, Bandeja Compartida, Copiloto IA M365, Tableros Kanban).

### Nivel 2: `docs/requirements/v1_user_stories.md` (El "Cómo" Técnico - Gherkin)
- **Cuándo leerlo:** Al momento de programar o diseñar un Controlador REST, Servicio Java o Componente Vue.
- **Para qué sirve:** **ESTE ES SU CONTRATO PRINCIPAL.** Contiene las Historias de Usuario hiper-detalladas usando el estándar BDD (Behavior-Driven Development) en sintaxis Gherkin (Given-When-Then).
- **Regla:** Si usted como Agente asume un comportamiento que *no está explícitamente detallado en el Gherkin*, su asunción es inválida. Ajústese a los Criterios de Aceptación literales del documento.

### Nivel 3: `docs/requirements/v1_moscow_scope_validation.md` (El "Cuándo")
- **Cuándo leerlo:** Cuando esté tentado a construir componentes "Nice to Have" excesivamente complejos (Ej. Módulos de Machine Learning Predictivo, OCR Hotelero).
- **Para qué sirve:** Es el escudo anti-desviación. Si le encomiendan construir algo que está en la lista de *"WON'T HAVE"*, usted debe detenerse y notificar al usuario (Human-in-the-Loop) que esa tarea viola las reglas del MVP Táctico.

### Nivel 4: `docs/requirements/non_functional_requirements.md` (Restricciones Arquitectónicas)
- **Cuándo leerlo:** Al configurar Bases de Datos, CI/CD, Reglas de Autenticación OIDC, Caché (Redis) o Máximos de Concurrencia.
- **Para qué sirve:** Garantiza que el código no solo funcione, sino que cumpla métricas corporativas Enterprise de tiempos de respuesta, escalabilidad, y la separación de capas dictaminada por la Arquitectura Hexagonal.

## 3. Resolución de Discrepancias

1. **Gherkin vs PRD:** Si hay una contradicción funcional entre la descripción macro del `functional_requirements.md` y un escenario Gherkin de `v1_user_stories.md`, **SIEMPRE PREVALECE EL GHERKIN** como detalle técnico final.
2. **Wireframes vs Requirements:** Si un Wireframe (ubicado en `docs/architecture/v1_wireframes.md`) exige un botón que **no existe** en las Historias de Usuario, usted debe consultar al Usuario, *no reinventar una Historia fantasma*.

> **Objetivo Final:**
> Su obligación como Agente es apegarse 100% al alcance pre-verificado (SSOT). **Si no está escrito en estos 4 archivos, no es un requerimiento de la V1.** Construya lo que se pide; nada más, nada menos.

-- Fin de la Directriz --
