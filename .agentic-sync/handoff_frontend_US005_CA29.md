# Handoff Frontend — US-005, CA-29

> **Historia de Usuario:** US-005 - Desplegar y Versionar un Modelo de Proceso (BPMN)
> **Criterio de Aceptación:** CA-29 (Copiar y Pegar Fragmentos entre Procesos)
> **Estado:** Delegado para Ajustes de Código e Integridad de Trazabilidad

---

## 1. Requerimientos Técnicos

### R1. Decorar el servicio `clipboard` de `bpmn-js` en `BpmnDesigner.vue`
En el archivo `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, tras la inicialización de `modelerInstance = new BpmnModeler(...)` en el hook `onMounted()`, realice la decoración de los métodos del servicio `clipboard` para compartir el árbol de copia entre instancias a través de `localStorage`:

1. **`clipboard.get`**:
   - Recuperar el árbol serializado desde `localStorage` bajo la clave `bpmn_shared_clipboard`.
   - Si existe, parsearlo (`JSON.parse`) y retornarlo.
   - De lo contrario, retornar los datos locales del clipboard.
2. **`clipboard.set(data)`**:
   - Invocar el comportamiento original del clipboard nativo.
   - Serializar el árbol de elementos copiado a JSON de forma segura eliminando claves con referencias circulares (como `$parent`, `parent`) y guardarlo en `localStorage` bajo `bpmn_shared_clipboard`.
   - Utilice un replacer personalizado para evitar el error `Converting circular structure to JSON`:
     ```typescript
     const seen = new WeakSet();
     const serialized = JSON.stringify(data, (key, value) => {
       if (key === '$parent' || key === 'parent') {
         return undefined;
       }
       if (typeof value === 'object' && value !== null) {
         if (seen.has(value)) {
           return undefined;
         }
         seen.add(value);
       }
       return value;
     });
     localStorage.setItem('bpmn_shared_clipboard', serialized);
     ```
3. **Exponer el Clipboard para Testabilidad**:
   - En el bloque final del script, exponga un método en la instancia del componente Vue:
     ```typescript
     const getModelerClipboard = () => {
       return modelerInstance ? modelerInstance.get('clipboard') : null;
     };
     ```
     Asegúrese de definirlo y agregarlo a los exports/macros o exponerlo en el script setup para que la suite de pruebas pueda acceder a él.

### R2. Trazabilidad Obligatoria (Ley Global 3)
Asegúrese de incluir la marca de trazabilidad en las áreas de código modificadas:
`// @Traceability: US-005, CA-29 Copiar y Pegar Fragmentos entre Procesos`

### R3. Validar que la compilación Frontend funcione
Ejecute el build de producción del frontend para asegurar que no hay errores de TypeScript o empaquetado:
`npm run build`

---

## 2. Directivas de Validación y Calidad

- **Clean Code:** Mantenga la legibilidad y la estructura original del archivo.
- **TDD:** Asegure que al ejecutar los tests de Vitest, la suite `BpmnDesigner.spec.ts` pase exitosamente.
- Cree su plan de trabajo en `.agentic-sync/approval_request_frontend.md` y espere la aprobación del Arquitecto Líder antes de modificar el archivo.

---

**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
