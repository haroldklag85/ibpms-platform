---
description: Definición de arquitectura multi-agente con separación estricta de roles, memorias y contextos mediante el protocolo Agentic Handoff.
---

# 🏛️ Arquitectura Multi-Agente Estricta (Separación de Contextos)

## 1. Contexto y Objetivo
Para evitar la contaminación de contexto (Context Bloat), mezcla de responsabilidades y el aumento de alucinaciones derivado de que un mismo agente de IA asuma múltiples funciones en una sola ventana de chat, se instaura una **Arquitectura Multi-Agente Estricta**. 
El objetivo es garantizar que cada agente especializado opere con una memoria aislada, enfocado únicamente en su dominio, y que el Agente Arquitecto Líder se mantenga puramente como un ente orquestador y revisor.

## 2. Definición de Roles y Límites (Separación de Memorias)

### 👑 Agente Arquitecto Líder (Orquestador)
*   **Rol:** Project Manager, Diseñador de Soluciones y Revisor de Código.
*   **Límites:** **TIENE ESTRICTAMENTE PROHIBIDO PROGRAMAR CÓDIGO FUNCIONAL** (Vue/Java) o cambiar de personalidad. Su contexto se mantiene limpio y enfocado en la arquitectura (Gherkin, C4 Model, .cursorrules).
*   **Responsabilidad:** Leer los requerimientos, dividir el trabajo, redactar los contratos (Entradas/Salidas) y delegar las tareas a los subagentes bloqueando su contexto mediante archivos físicos. Efectúa la auditoría final usando `git stash pop` y consolida el `commit`.

### ⚙️ Agente Backend (Especialista Java/Spring Boot)
*   **Memoria Aislada:** Debe ser invocado en una **NUEVA VENTANA DE CHAT**. No conoce de UI, Vue ni de requerimientos comerciales más allá del contrato que se le entrega.
*   **Responsabilidad:** Leer su instrucción delegada, programar la Arquitectura Hexagonal, realizar pruebas unitarias (JUnit) y empaquetar su trabajo exclusivamente con `git stash save "temp-backend-US[X]"`.

### 🎨 Agente Frontend (Especialista Vue 3/TypeScript)
*   **Memoria Aislada:** Invocar en su propia **NUEVA VENTANA DE CHAT**. No conoce el código interno de Java ni la base de datos.
*   **Responsabilidad:** Consumir el API real, construir componentes interactivos en Vue/Tailwind respetando los contratos DTO, y empaquetar obligatoriamente con `git stash save "temp-frontend-US[X]"`.

### 🔎 Agente QA / DevOps 
*   **Memoria Aislada:** Invocar en su propia **NUEVA VENTANA DE CHAT**.
*   **Responsabilidad:** Ejecutar validaciones E2E empíricas, auditar las 4 capas (UX, Red, Backend, Seguridad) o crear scripts de Playwright sin acceso a refactorizar el código fuente productivo.

## 3. Mecanismo de Interacción y Trazabilidad (Protocolo .agentic-sync)

Dado que los agentes operan en chats (memorias) separados, **se prohíbe la delegación "de boca a boca" (copiar párrafos gigantes en el chat)**. La comunicación se fundamenta en el sistema de archivos físicos del repositorio.

1.  **Orquestación Escrita:** El Arquitecto Líder analiza la tarea y crea **archivos Markdown de delegación** en la carpeta oculta `.agentic-sync/` (Ej. `.agentic-sync/handoff_frontend_US003.md`). Estos archivos contienen el contexto exacto y minucioso para ese rol específico.
2.  **Invocación en Salas Limpias:** El usuario humano actúa como el canal de activación. Abre un nuevo chat y envía un "Puntero Corto" al agente especialista. Ej: *"Actúa como Agente Frontend y ejecuta estrictamente lo solicitado en el archivo `.agentic-sync/handoff_frontend_US003.md`"*.
3.  **Ejecución y Empaquetado:** El especialista lee el archivo, programa sus capas, guarda en el *Stash* y lo notifica en su propio chat.
4.  **Auditoría y Cierre:** El humano regresa a la ventana del Arquitecto Líder e informa la finalización del especialista. El Arquitecto Líder recupera el código (`git stash pop`), lo revisa basándose únicamente en el *diff* (para mantener su memoria estable) y aprueba el *commit*.

> **Resultado:** Este diseño erradica las alucinaciones por cruce de dominios, garantiza trazabilidad absoluta en el disco (`.agentic-sync/`) y mantiene a cada agente operando dentro de su zona de genio con máxima precisión.
