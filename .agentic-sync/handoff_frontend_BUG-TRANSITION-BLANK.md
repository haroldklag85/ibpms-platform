# Handoff Técnico: BUG-TRANSITION-BLANK (Pantalla Blanca al Navegar entre Vistas)

## Contexto Técnico
El usuario reporta que al navegar desde la lista de formularios (`/admin/modeler/forms`) hacia el diseñador (`/admin/modeler/forms/designer`), la pantalla queda en blanco. Sin embargo, al recargar la página (F5) en la ruta del diseñador, este carga perfectamente.

Tras la auditoría arquitectónica, se ha determinado que **este no es un problema de Monaco Editor ni de Lazy Loading**, sino un **defecto crítico de las transiciones en Vue 3**. 
En el archivo `MainLayout.vue`, existen comentarios HTML (`<!-- @Traceability... -->`) directamente dentro de la etiqueta `<transition name="fade" mode="out-in">` y antes del `<keep-alive>`. 
En Vue 3, los comentarios son parseados como nodos virtuales (VNodes). La etiqueta `<transition>` exige estrictamente un **único elemento raíz**. Al existir comentarios, Vue detecta múltiples nodos (un fragmento), lo que corrompe la máquina de estados de la transición `out-in`. El componente saliente nunca termina de animarse, por lo que el componente entrante (`FormDesigner.vue`) **nunca se renderiza**, dejando la pantalla en blanco. Al presionar F5, no hay transición de salida, por lo que carga exitosamente.

## Criterios de Aceptación a Validar
* **CA-BUG-1:** La navegación SPA mediante botones o enlaces hacia cualquier ruta hija del `MainLayout` (ej. `/admin/modeler/forms/designer`) debe ocurrir sin dejar la pantalla en blanco y ejecutando el efecto *fade* correctamente.
* **CA-BUG-2:** Eliminar o reubicar estrictamente todos los comentarios HTML internos dentro del tag `<transition>` en `MainLayout.vue`.

## Instrucciones de Implementación (Frontend)
1. Abrir `frontend/src/layouts/MainLayout.vue`.
2. Localizar el bloque `<router-view>` (aprox. línea 253).
3. Dentro de `<transition name="fade" mode="out-in">`, **eliminar o mover FUERA del tag `<transition>`** los siguientes comentarios:
   ```html
   <!-- @Traceability(US = "US-001", CA = {"CA-12"}) Acierto UX: Keep-Alive retiene scroll y filtros en RAM para 0ms de carga en regresos -->
   <!-- @Traceability: US-005, CA-15 (Fix Welcome Modal Loop - Solución A) -->
   ```
4. Asegurar que el único hijo directo de `<transition>` sea `<keep-alive include="Workdesk">` (o directamente el `<component>` en su defecto).

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
