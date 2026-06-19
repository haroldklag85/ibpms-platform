# Handoff Técnico: BUG-MONACO-BLANK (Pantalla Blanca en FormDesigner)

## Contexto Técnico
El usuario reporta que al navegar a `/admin/modeler/forms/designer`, la pantalla queda completamente en blanco y la consola muestra el error: `Uncaught (in promise) RegisterClientLocalizationsError: Cannot read properties of undefined (reading 'translations')`.

Este error es provocado por el componente `@guolao/vue-monaco-editor`, el cual por defecto intenta descargar dinámicamente *Monaco Editor* desde jsDelivr. Versiones recientes de Monaco Editor (>0.45) modificaron la arquitectura de los módulos NLS (Localizaciones), lo que causa una falla silenciosa en la promesa de inicialización de la librería si no están configurados correctamente, abortando la renderización de la vista lazy-loaded en Vue Router y dejando el `router-view` en blanco.

## Criterios de Aceptación a Validar
* **CA-BUG-1:** La ruta `/admin/modeler/forms/designer` debe renderizar el IDE y su toolbar superior correctamente sin arrojar errores en consola.
* **CA-BUG-2:** El editor de código JSON (Monaco) dentro del IDE debe cargar correctamente sin mostrar errores `RegisterClientLocalizationsError`.

## Instrucciones de Implementación (Frontend)
1. Abrir `frontend/src/views/admin/Modeler/FormDesigner.vue`.
2. Importar el `loader` de `@guolao/vue-monaco-editor`:
   ```typescript
   import VueMonacoEditor, { loader } from '@guolao/vue-monaco-editor';
   ```
3. Configurar estáticamente el loader para fijar la versión del CDN a una versión estable de Monaco (ej. `0.43.0`) justo después de los imports, mitigando la falla del NLS:
   ```typescript
   loader.config({
     paths: {
       vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.43.0/min/vs'
     }
   });
   ```
4. Opcionalmente, puedes aplicar esta configuración en el `onMounted` o directamente en el nivel raíz del `<script setup>`.

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_[ROL].md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_[ROL].md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
- Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.
