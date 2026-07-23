# 🏗️ Handoff Arquitectónico — Corrección UI Form Designer

## 1. Metadatos y SSOT
- **Iteración/Sprint**: DevDavid
- **Rama de Trabajo**: DevDavid
- **Bug ID**: BUG-UI-DESIGNER
- **Criterios de Aceptación (CAs)**: Estabilización de UI.
- **SSOT**: Bug reportado visualmente en `/admin/modeler/forms/designer`.
- **Flujo de Trabajo**: Frontend Exclusivo (QA Omitido por solicitud).

## 2. Alineación Arquitectónica y ADRs
- **Validación de ADRs**: 
  - Cumplimiento de **ADR-002 (Microfrontends Vue 3)**. Se deben respetar los lineamientos de TailwindCSS sin introducir estilos inline forzados ni dependencias externas.
- **Diagnóstico Arquitectónico**:
  - El error reportado en consola (`Unchecked runtime.lastError: The message port closed before a response was received`) **es un falso positivo provocado por extensiones de Chrome** (ej. Vue DevTools, AdBlockers, Grammarly). **PROHIBIDO** invertir tiempo intentando depurar o "arreglar" este error a nivel de aplicación Vue.
  - El problema real radica en el desbordamiento de Flexbox (`flex`) entre el `Toolbox` (`w-64`), el `Lienzo Central` (`flex-1`) y el `IDE Monaco` (`w-2/5 min-w-[350px]`), lo que causa solapamiento en resoluciones de pantalla estándar.

## 3. Rutas Exactas y Contexto Preexistente
- **Archivo Objetivo**: `frontend/src/views/admin/Modeler/FormDesigner.vue`
- **Contexto Preexistente**: 
  La estructura del layout principal es:
  ```html
  <main class="flex-1 flex min-h-0 relative">
    <aside class="w-64 shrink-0">...</aside>
    <section class="flex-1">...</section>
    <aside class="w-2/5 min-w-[350px] shrink-0">...</aside>
  </main>
  ```
  La interacción entre el canvas central aislado en Shadow DOM (`max-w-4xl`) y los anchos mínimos está rompiendo el renderizado cuando la pantalla no es ultrawide.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")
Se requiere aplicar ajustes quirúrgicos en las clases de Tailwind de `FormDesigner.vue` para garantizar que el lienzo central escale adecuadamente sin solaparse:

1. **Ajuste de la sección Central (Canvas)**:
   Asegurar que tenga `min-w-0` y maneje su propio overflow para que Flexbox no lo desborde.
   ```html
   <!-- Cambiar: -->
   <section class="flex-1 bg-gray-50/50 flex flex-col relative">
   <!-- Por: -->
   <section class="flex-1 min-w-0 bg-gray-50/50 flex flex-col relative overflow-x-auto">
   ```

2. **Ajuste del IDE Monaco (Panel Derecho)**:
   Reducir el ancho relativo en pantallas menores a 2XL o permitir un colapso más fluido.
   ```html
   <!-- Cambiar: -->
   <aside v-show="!isFullScreen" class="w-2/5 min-w-[350px] bg-[#1e1e1e]...">
   <!-- Por: -->
   <aside v-show="!isFullScreen" class="w-[30%] lg:w-1/3 2xl:w-2/5 min-w-[300px] bg-[#1e1e1e]...">
   ```

3. **Manejo del Error de Consola**: No realizar cambios en el código para el error de `runtime.lastError`. Notificar al usuario que utilice modo Incógnito para verificar la ausencia del error.

## 5. Matriz de QA y Testing Atómico
*Nota: Flujo QA delegado explícitamente ("No").* El desarrollador Frontend deberá auto-verificar localmente:
| Test Name | Validado | Aserción Esperada |
|-----------|----------|-------------------|
| Responsive Flex | UI | El lienzo central se contrae correctamente sin invadir el IDE Monaco ni la barra izquierda en resolución 1366x768. |
| Console Check | Console | El error de port closed se ignora justificadamente por extensión de Chrome. |

## 6. Mensaje de Despacho (Comunicación al Agente Especialista)

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **📋 DIRECTIVA DE INFRAESTRUCTURA HÍBRIDA:**
> El backend Spring Boot corre en consola local (NO en Docker) en el puerto `8080`. Antes de iniciar tu trabajo, ejecuta el protocolo de pre-validación:
> 1. `curl -s http://localhost:8080/actuator/health` → Debe responder `{"status":"UP"}`.
> 2. Si no responde, arráncalo con: `cd backend && mvn spring-boot:run -pl ibpms-core -Dspring-boot.run.profiles=default`.
> 3. Verifica los servicios Docker: `docker ps` → PostgreSQL (`5433`), Redis (`6379`) y RabbitMQ (`5672`) deben estar `Up (healthy)`.
> **PROHIBIDO** levantar el backend vía Docker o modificar el `docker-compose.yml`.

> Build obligatorio: Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_[ROL].md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_[ROL].md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION` y programa la solución.
> 6. **ANTES del commit final**, actualiza `docs/sprints/gobernanza_pm/CHANGELOG_NO_TECNICO.md` explicando tu trabajo en lenguaje sencillo y amigable para un CEO (QUÉ hiciste y PARA QUÉ sirve). 
> 7. Finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
