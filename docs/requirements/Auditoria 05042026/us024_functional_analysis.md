# Análisis Funcional Definitivo: US-024 (Creación Global Restringida Plan B)

## 1. Resumen del Entendimiento
La US-024 documenta el mecanismo "Fuerza Bruta" o "Bypass manual" para crear instancias de Service Delivery (Casos Camunda) sin depender de un flujo previo guiado por correos de clientes. Está diseñado para admins.

## 2. Objetivo Principal
Asegurar que existan herramientas para arrancar el orquestador directamente desde la interfaz web, gobernando IDs, variables iniciales, bloqueos anti-duplicados y permitiendo la destrucción en caliente del caso.

## 3. Alcance Funcional Definido
**Inicia:** Un admin aprieta "+ Nuevo Caso", perdiendo protección RAG.
**Termina:** El caso nace en Camunda o, inversamente, el admin lo liquida físicamente de la faz del sistema.

## 4. Lista de Funcionalidades Incluidas
- **Bypass de Instanciación:** Rendereo dinámico de formularios asociados al BPMN (CA-2). ID generable auto o parametrizado (CA-3).
- **Control de Ciclo de Vida (Soft Delete & Compensación):** El admin puede matar una instancia In-Flight, aniquilando el JWT/Token físico en Camunda API (`DELETE`), marcando en Base datos como anulado (CA-7), y finalmente emitiendo un evento asíncrono para destruir soportes huérfanos en SGDEA (CA-13).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Desastre Arquitectónico de Consistencia (Patrón SAGA Roto) (⚠️ CA-7 y CA-13):** CA-7 manda a "Aniquilar Físicamente" la instancia del motor Camunda vía REST. Acto seguido, en CA-13 manda al backend HTTP Java a despachar un Comando de Compensación (Saga/Evento) al Gestor Documental para eliminar archivos pesados. **GAP:** Si el motor de procesos Camunda fue vaporizado (Hard Delete del Engine), y la red/API de SharePoint falla temporalmente, el comando compensatorio (CA-13) se perderá en el Limbo porque el orquestador de persistencia Camunda YA NO EXISTE para gestionar los re-intentos (Retries) del SAGA documenta. Nunca se hace *Hard Delete* vía API Engine en una micro_arquitectura distribuida. Se debe crear un `Message Boundary Event` dentro del mismo BPMN de Camunda llamado "Cancelación Forzada Administrador", que despache The Cleanup Tasks antes de matar sanamente el token.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- SLA Override: Excluye permitir la manipulación personal del SLA timer desde el formulario.

## 7. Observaciones de Alineación o Riesgos
Hay un choque conceptual en el CA-6 (Anti-Clones): Permite clonación forzada para el mismo cliente indicando "procesos paralelos simultáneos agnósticos". Si un humano falla y oprime doble "Guardar", va a despachar doble trámite idéntico. Faltó gobernanza en capa UI por Idempotencia.
