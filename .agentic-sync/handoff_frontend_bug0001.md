# 🏗️ Handoff Arquitectónico: Frontend BUG-0001 (Estilos y Responsive en FormDesigner)

## 1. Metadatos y SSOT (Single Source of Truth)
- **Iteración:** bug-01devDavid
- **Bug ID:** bug-0001
- **Rama Git:** DevDavid
- **User Story:** N/A (Bug Fix Visual)
- **CAs:** N/A (Reparación de estilos en FormDesigner)
- **Path del SSOT:** N/A (Bug visual reportado por imagen, afecta componente `FormDesigner.vue` y estilos Tailwind).
- **Flujo de Trabajo:** Frontend

## 2. Alineación Arquitectónica y ADRs (Architecture Decision Records)
- **Validación de ADRs:** Cumplimiento de `adr-002-vue3-microfrontends.md` usando clases utilitarias de Tailwind CSS para la interfaz.
- **Lineamientos Transversales:** Todo ajuste de estilos debe mantenerse encapsulado en el framework de Tailwind (utilizando utilitarios `w-full`, flexbox, grid, breakpoints `md:`, `lg:` para responsive). No se deben crear hojas de estilo globales externas a menos que sea estrictamente necesario.
- **Trazabilidad de la Solución:** La solución utilizará flexbox/grid nativo de Tailwind para reparar el desbordamiento o mal posicionamiento del "Campo Base (Semilla)" en el centro del diseñador.

## 📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA
El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
**PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

## 3. Rutas Exactas y Contexto Preexistente
- **Path Absoluto:** `frontend/src/views/admin/Modeler/FormDesigner.vue`
- **Estado Actual:** El usuario reporta que en el diseñador bidireccional (IDE de Formularios Vue3/Zod), el área central donde se renderiza dinámicamente el componente visual de un input (ej. `Campo Base (Semilla)`) tiene estilos rotos (falta de alineación, overflow, o no respeta anchos). Además, el layout general no es responsivo, rompiendo la experiencia en resoluciones menores.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
1. **Inspección Visual (Canvas Central):**
   Dentro de `FormDesigner.vue`, revisa el `<section class="flex-1 min-w-0 bg-gray-50/50 flex flex-col relative overflow-x-auto">`.
   Revisa los estilos flex y anchos de los campos arrastrados en el Canvas y el contenedor `shadow-dom-isolation-wrapper`.
2. **Corrección Responsive:**
   Asegúrate de que la estructura que divide la pantalla (Toolbox Izquierda, Canvas Central, Monaco IDE a la derecha) use los flexbox/grid correctos para no desbordar. Revisa las clases de ancho y flex (`flex-1 min-w-0`, `w-64`, `w-[30%]`) para asegurar que todo sea responsivo (agregar soporte a mobile/tablet si es necesario ocultando paneles o usando `flex-col`).
3. **Corrección de la generación de vistas del formulario:**
   Verifica cómo se está calculando y mostrando el campo base y su estilo (label + input). El componente debe ocupar su espacio adecuadamente sin salirse de su contenedor.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

## 5. Matriz de QA y Testing Atómico
*Como `Necesita QA = no`, no se requiere un handoff separado para QA, pero el dev debe verificar:*
- Nombre del test a ejecutar: Ejecuta los tests existentes en `frontend/src/views/admin/Modeler/__tests__/` (o de integración) para confirmar que los cambios de clase Tailwind no rompieron snapshots o el renderizado.

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)

Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve).
7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
