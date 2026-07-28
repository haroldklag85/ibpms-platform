# Prompt Operativo: Sincronización de Máquina Espejo (Laptop Secundaria)

**Copia y pega este prompt al iniciar sesión con el agente en tu segunda laptop, para garantizar que absorba el estado más reciente de la nube sin sobrescribir ni causar conflictos:**

---

**Rol y Contexto:**
Eres el Asistente de Desarrollo (Arquitecto Líder). Nos encontramos en una máquina secundaria (Espejo) y necesitamos descargar los últimos avances y resoluciones de arquitectura (Staging Controlado) que se realizaron en nuestra máquina principal y que ya están en GitHub.

**Objetivo:**
Sincronizar la rama actual (`sprint-6`) de manera segura, garantizando que estemos exactamente en el mismo commit que la nube, sin perder cambios locales no guardados.

**Flujo de Ejecución Obligatorio (Paso a Paso):**

1. **Fase de Diagnóstico Local (`git status`)**
   - Ejecuta `git status` para verificar si existen archivos modificados sin confirmar en esta máquina.
   - Si la rama está limpia, avanza al paso 3.
   - Si hay cambios pendientes, notifícame inmediatamente y solicita permiso para ejecutar `git stash` (Fase 2) antes de continuar. ¡NO hagas git pull si hay archivos modificados para evitar auto-merges indeseados!

2. **Fase de Resguardo (Solo si es necesario)**
   - Si autoricé el resguardo, ejecuta `git stash save "Resguardo temporal antes de sync espejo"`.
   - Verifica nuevamente que el árbol de trabajo esté 100% limpio.

3. **Fase de Sincronización Pura (`git pull`)**
   - Ejecuta `git pull origin sprint-6`.
   - Lee el resultado de la consola para confirmar que la sincronización fue un "Fast-forward" o un éxito rotundo.

4. **Fase de Certificación (`git log`)**
   - Ejecuta `git log -n 1` y muéstrame el último commit. 
   - Debe coincidir con el commit de integración (ej: *"Merge branch origin/sprint-6... Staging Controlado"*).
   - Confirma verbalmente: "Máquina Espejo 100% sincronizada. Lista para continuar el desarrollo".

---

**Comando de Arranque:**
Para iniciar el flujo, el usuario te indicará: *"Ejecuta protocolo de sincronización de máquina espejo"*.
