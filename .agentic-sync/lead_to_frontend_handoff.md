# Handoff: Lead Architect → Frontend Developer

**Objetivo:** Implementar la UI para crear un expediente.

1. **Qué se completó:**
   - Se añadió al `openapi.yaml` el endpoint `POST /expedientes` con el modelo `ExpedienteDTO`.
   - Se actualizó el diagrama C4 (Nivel 3) para reflejar el nuevo caso de uso y la interacción del UI con el Backend.
2. **Contrato a cumplir:**
   - La pantalla debe enviar un `POST` a `http://localhost:8080/api/expedientes` con el cuerpo JSON generado por el formulario.
   - Mostrar mensaje de éxito y el `id` retornado.
3. **Cómo probarlo:**
   - Ejecutar la aplicación (`npm run dev`) y abrir `http://localhost:3000/expedientes/new`.
   - Completar los campos obligatorios y pulsar **Crear**; verificar que el toast indique "Expediente creado" y que el ID aparezca en la lista.
4. **Bloqueantes detectados:**
   - Necesario que el Backend devuelva `201 Created` con el cuerpo completo.
   - Se requiere la definición del componente Vue `ExpedienteForm.vue` bajo `src/components/`.

_Lead Architect (Gemini 3.1 Pro) – 2026-02-24_
