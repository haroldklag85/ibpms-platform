# Mapeo de Arquitectura BPMN 2.0 a Historias de Usuario (iBPMS)

**Rol Análisis:** Product Owner / Process Architect
**Contexto:** Este documento toma la especificación gráfica y técnica del estándar ISO BPMN 2.0 y mapea exactamente cómo el iBPMS V1 soporta cada elemento, qué valor de negocio aporta, y en qué Historia de Usuario (HU) o Requerimiento está cobijado.

---

## 1. Eventos (Events)
*Define lo que "ocurre" en el sistema.*

*   **Implementación iBPMS:**
    *   **Evento de Inicio (Start):** Lo usamos para disparar instancias nuevas de un proceso (Ej: El usuario presiona "Iniciar Solicitud" en la Pantalla 0).
    *   **Evento Intermedio (Intermediate):** Crucial para los `Timers`. Si un usuario no responde en 2 días, el evento de tiempo dispara un correo. También usamos *Message Events* para escuchar Webhooks (Pantalla 11).
    *   **Evento de Fin (End):** Termina el caso, sella el expediente en "Completado" y archiva en BD.
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Motor de Ejecución de Procesos (BPMN Core)`.
    *   **HU:** *"Como usuario, quiero iniciar un proceso seleccionándolo desde el Catálogo..."* (Start Event).
    *   **HU:** *"Como administrador, quiero configurar SLAs (Timers) y Escalamientos en Tareas..."* (Intermediate Boundary Event).

## 2. Actividades (Activities)
*El trabajo real que hace la empresa.*

*   **Implementación iBPMS:**
    *   **Tareas (User Tasks):** Es el 90% de nuestra plataforma humana. Una User Task en BPMN genera una tarjeta visual en la "Bandeja de Tareas Unificada" (Pantalla 1) asignada a una persona. Al darle clic, abre el Formulario JSON (Pantalla 2).
    *   **Tareas de Servicio (Service Tasks):** Tareas invisibles que ejecuta la máquina (Ej: Llamar a nuestro API Rest local, o ejecutar un script de envío de correo en la Pantalla 11).
    *   **Sub-Procesos:** Los usamos para modularizar el código visual. Ej: Un "Onboarding" llama al subproceso "Diligencia Debida Legal", permitiendo reusar flujos completos.
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Bandeja Unificada y Formularios Dinámicos`.
    *   **HU:** *"Como trabajador, quiero ver mis tareas pendientes (User Tasks) ordenadas por SLA en una lista unificada..."*
    *   **HU:** *"Como trabajador, quiero abrir una tarea y diligenciar un Formulario Dinámico (JSON Schema)..."*

## 3. Compuertas (Gateways)
*El ruteo lógico basado en decisiones corporativas.*

*   **Implementación iBPMS:**
    *   **Rombo Exclusivo (Exclusive Gateway):** "O se aprueba, O se rechaza, nunca ambos". Lo usamos atado a nuestra Pantalla 4 (Taller de Reglas IA). El Gateway lee la regla `DMN` y sabe por qué camino desviar la tarea de facturación.
    *   **Rombo Paralelo (Parallel Gateway):** Cuando un documento se radica, el flujo se divide en 2 para que Finanzas revise el dinero y Legal revise el contrato al mismo tiempo. Ahorra semanas de "cuellos de botella secuenciales".
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Tablas de Decisión e IA`.
    *   **HU:** *"Como Arquitecto, quiero asociar una Compuerta Lógica a una regla de negocio (DMN) para que el flujo avance automáticamente..."*

## 4. Datos (Data)
*La carga útil (Payload) y la inmutabilidad documental.*

*   **Implementación iBPMS:**
    *   **Objetos de Datos (Data Objects):** En Camunda, esto se traduce en *Process Variables*. Nosotros lo revolucionamos guardándolo como un gran campo nativo `JSON` en MySQL 8 (`ibpms_case.payload`).
    *   **Almacenes de Datos (Data Stores) / Archivos:** En lugar de guardar archivos en la BD de procesos, modelamos el Data Store apuntando al **SGDEA** (Nuestra bóveda documental con Hash SHA-256 en Azure Blob/Pantalla 12).
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Bóveda Documental y Cumplimiento (SGDEA)`.
    *   **HU:** *"Como trabajador, quiero subir anexos probatorios a mi tarea y que estos queden sellados inmutablemente bajo una política de retención..."* (Data Object -> Data Store).

## 5. Objetos de Conexión (Connecting Objects)
*El pegamento de la orquestación.*

*   **Implementación iBPMS:**
    *   **Flujos de Secuencia:** Es el "Código Espagueti" pero visualizado. El motor sigue la flecha estricta. Un usuario no puede hacer la "Tarea 3" si la flecha no ha pasado por la "Tarea 2". Nos garantiza el **Cumplimiento Normativo (Compliance)** matemático.
    *   **Flujos de Mensaje:** Arquitectónicamente los usamos en la V2 cuando módulos SaaS distintos (Ej: Módulo OCR Recepción) hablan con el Motor iBPMS Central mediante llamadas API cifradas por el APIM.
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Diseñador y CRUD de Procesos`.
    *   **HU:** *"Como Administrador, quiero dibujar flujos conectando actividades (Sequence Flows) en un lienzo para definir el estándar operativo..."*

## 6. Swimlanes (Piscinas y Carriles)
*Gobierno de Idenitdad (Segregación y Asignación).*

*   **Implementación iBPMS:**
    *   **Pools:** Definen un caso aislado (`ibpms_case.id`). Las variables JSON de la piscina de "Recursos Humanos" no ensucian las variables de la piscina de "Compras".
    *   **Lanes (Carriles):** Como lo definimos antes, mapean directamente a los Array de Roles (`candidate_groups`) de nuestra arquitectura relacional. Si mueves una tarea al "Lane: VP Finanzas", la tarea se le oculta en la UI a los demás empleados por reglas del Token OIDC (Pantalla 14).
*   **Gestión en Historias de Usuario / PRD:**
    *   `Épica: Seguridad, Autenticación y RBAC`.
    *   **HU:** *"Como Administrador, quiero vincular los carriles (Lanes) del diagrama con los grupos de EntraID para asignar responsables masivamente..."*

## 7. Artefactos (Artifacts)
*Documentación colaborativa del sistema.*

*   **Implementación iBPMS:**
    *   Dado que queremos apuntar a "Citizen Developers" (Usuarios que no saben programar), las Anotaciones de Texto sirven como manual operativo en vivo. Un empleado puede abrir la Pantalla 5 (Rastreador), mirar la caja donde está el proceso, y leer la anotación que el Arquitecto dejó flotando: *"Nota: Esta tarea siempre demora 2 días por ley."*
*   **Gestión en Historias de Usuario / PRD:**
    *   Está embebido implícitamente en la herramienta integradora `bpmn-js` de la Pantalla 6, siendo una ayuda visual puramente de Frontend almacenada dentro del string XML del diagrama.
