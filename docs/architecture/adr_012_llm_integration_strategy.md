# ADR-012: Patrón de Integración Agnóstica de Modelos Fundacionales (LLM Plugin Architecture)

**Fecha de Aprobación:** 2026-04-09
**Estado:** Aceptado
**Módulo:** AI Core Integration (Backend)

## 1. Contexto Escenario
El ecosistema de IBPMS debe conectar con distintos proveedores de IA (OpenAI, Gemini, Claude) dependiendo de las limitantes regulatorias de cada cliente o de costos de API. Integrar monolitos gigantescos como LangChain o Spring AI expone la Plataforma a obsolescencia violenta (vulnerabilidades) y peso de dependencias innecesario (Zero-App-Bloat). Tras realizar una auditoría forense al proyecto **OpenClaw**, hemos verificado que abstrayendo nativamente las llamadas HTTP (vía REST/WebSocket) se elimina la necesidad de intermediarios SDK pesados, delegando el peso al motor que importa (Camunda) y no a la librería de integración de IA.

## 2. Decisión Arquitectónica (ADR)
Se dictamina utilizar un patrón de adaptador/plugins desacoplado fundamentado en  puertos y adaptadores (ADR-001):

### 2.1 Cero Dependencias Monolíticas de IA
Queda **ESTRICTAMENTE PROHIBIDO** inyectar al `pom.xml` o Node `package.json` SDKs masivos enfocados a agentes autónomos externos. Quedan vetadas librerías como `langchain`, `llamaindex` o `spring-ai-core` a menos de que un Comité de Arquitectura expida un RFC formal que justifique el exceso.

### 2.2 Patrón Plugin-SDK y Contratos Tipados (Capabilities)
Se crearán Puertos (Interfaces) puras orientadas a las capacidades (Capabilities) requeridas por las User Stories.
- **Port:** `LlmChatProvider` (Para procesamiento de Texto)
- **Port:** `LlmEmbeddingProvider` (Para RAG y Vectorización)
En estos contratos (Ports) no existirá mención técnica alguna a "OpenAI" ni "Anthropic". Su POJO (Payload Base) dependerá netamente del Dominio (`ProcessInstanceId`, `MaxTokens`, etc.).

### 2.3 Conectividad "Raw" Directa
La capa de Adaptador (Infrastructure) que implemente dichos ports (`AzureOpenAiAdapter`, `VertexAiAdapter`) deberá valerse pura y exclusivamente de los clientes HTTP nativos de su Framework (Ej. `RestClient` nativo de Spring Boot 3 o `fetch` en Node).
Estas capas nativas se encargarán del parsing del JSON crudo de la red aplicando un Rate Limiting defensivo propio.

### 2.4 Tolerancia Zero-Trust de Secretos 
El secreto del Modelo (`API_KEY` o `BEARER_TOKEN`) no se codifica, ni se manda en logs, y **no debe** subirse en un Header sin pasar por el módulo de autorización. Los adaptadores leerán el Token del módulo de Secrets de la máquina anfitriona en tiempo real o por variables inyectadas de CI/CD. No se permite transporte de Keys por el UI VUE.

## 3. Consecuencias
*   **Positivas:** Reducción drástica del tamaño del despliegue base (Jar/Container size), Cero Lock-in hacia Vendors de Abstracción (No morimos si LangChain muere). Reemplazo rápido usando Patrón Adapter-Factory inyectando dependencias.
*   **Negativas (Trade-offs):** Creación propia de serializadores/deserializadores DTO para las payloads hacia OpenAI/Gemini de nuestra autoría. Pérdida de "azúcar sintáctico" ofrecido por Langchain para construir grafos (compensado porque usaremos BPMN en Camunda).

## 4. Trazabilidad
Diseñado en base al comportamiento observado en OpenClaw e inyectado como pilar para el US-054.
