# Handoff: Lead Architect → Backend Developer

**Objetivo:** Implementar el API `POST /expedientes` para crear un expediente.

1. **Qué se completó:**
   - Definición del contrato OpenAPI (`openapi.yaml`) con el endpoint `POST /expedientes` y modelo `ExpedienteDTO`.
   - Actualización del diagrama C4 (Componentes) para incluir el nuevo caso de uso.
2. **Contrato a cumplir:**
   - El endpoint debe devolver `201 Created` con el cuerpo del expediente creado.
   - Validaciones de campos obligatorios según NFR-SEC-01.
3. **Cómo probarlo:**
   - Ejecutar `curl -X POST http://localhost:8080/api/expedientes -d @sample.json -H "Content-Type: application/json"`.
   - Verificar que la respuesta contenga `id` (UUID) y `status: "CREATED"`.
4. **Bloqueantes detectados:**
   - Necesario que el `Shared Transaction Manager` esté configurado antes de la implementación.
   - Se requiere la entidad JPA `Expediente` en el módulo `domain`.
   - **NUEVO (Copiloto M365):** En los `Driven Adapters` (Puertos de Salida), debes crear las interfaces Cliente (usando Feign o WebClient) para comunicarte con el **CRM** (Metadata de cliente) y **MS Graph API** (Generar borradores).

_Lead Architect (Gemini 3.1 Pro) – 2026-02-25_
