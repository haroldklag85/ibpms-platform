# Contrato de Arquitectura Frontend (Iteración 17 | US-005: CA-7 al CA-10)

**Rol:** Desarrollador Frontend Vue 3 / UI-UX Engineering.
**Objetivo:** Construir el "Gestor de Instancias Activas" permitiendo el paso quirúrgico de tokens antiguos a nuevas topologías BPMN, acatando reglas de seguridad (Zero Data-patching y Zero Guillotina).

## 📋 Contexto y Órdenes Directas (Implementación Estricta):

El sistema iBPMS ahora soporta despliegues de múltiples versiones del mismo diagrama (Ej: `Onboarding V1` y `Onboarding V2`). El Arquitecto Líder ordena la construcción de una interfaz gráfica transaccional para el administrador de procesos.

### Tarea 1: Interfaz de Cirugía Quirúrgica (CA-8)
Debes crear una nueva pestaña, modal o vista (`InstancesManager.vue`) asociada al Modelador.
*   **Regla de Guillotina (CA-8):** El panel listará las instancias en vuelo de la versión vieja. Debe proveer un Checkbox individual por cada fila. **TIENES ESTRICTAMENTE PROHIBIDO** crear un botón de "Seleccionar Todos" o "Migrar Todos Mágicamente". La selección es uno a uno (cirugía manual requerida por el Comandante).

### Tarea 2: Bloqueo Topológico Duro (CA-9 Feedback Visual)
El Backend nos enviará un JSON con la lista de instancias y una bandera `isMigratable`. 
*   Si `isMigratable === false`, tu UI debe **deshabilitar** (bloquear) el checkbox de esa fila específica.
*   Debes mostrar una insignia roja o tooltip (ej: `"Bloqueo: El nodo actual no existe en la Versión Destino"`).

### Tarea 3: Prohibición de Data Patching (CA-10)
Al hundir el botón final `[Ejecutar Migración Seleccionada]`, tu payload JSON (`POST /api/v1/design/processes/migrate`) debe contener **UNICAMENTE** un arreglo de strings `instanceIds`. 
*   **Restricción CA-10:** Tienes EXPRESAMENTE PROHIBIDO crear Inputs de texto, modales de captura o formularios dentro de esta pantalla para "rellenar campos faltantes". La operación es un empuje ciego de tokens; la completitud de los datos será responsabilidad funcional de los operarios posteriormente.

## 🛑 MANDATO LOCAL GATEKEEPER (REGLA DE ORO)
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` directamente a principal. 
Construye la UI, mockea la tabla de variables y congela todo:
`git stash save "temp-frontend-US005-ca7-ca10"`

Informa textualmente la confirmación del guardado.
