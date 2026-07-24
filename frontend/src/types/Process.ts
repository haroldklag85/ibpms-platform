/**
 * @module Process
 * @description Contratos TypeScript para la ejecución de procesos BPMN (US-007).
 * Reflejan exactamente los DTOs del backend (BpmnExecutionController).
 * @traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal)
 */

/** Contrato de request para iniciar un proceso BPMN — POST /api/bpmn/instances */
export interface StartProcessRequest {
  /** Clave de la definición del proceso desplegado en Camunda (REQUIRED) */
  processDefinitionKey: string;
  /** Clave de negocio opcional para trazabilidad del caso */
  businessKey?: string;
  /** Variables iniciales del proceso (vacío para MVP) */
  variables?: Record<string, unknown>;
}

/** Contrato de response al iniciar un proceso BPMN — 201 Created */
export interface StartProcessResult {
  /** ID de la instancia de proceso generada por Camunda */
  processInstanceId: string;
  /** Clave de la definición del proceso */
  processDefinitionKey: string;
  /** Clave de negocio (puede ser null) */
  businessKey: string | null;
  /** Timestamp ISO 8601 del inicio */
  startedAt: string;
  /** Username del iniciador */
  startedBy: string;
}

/** Ítem del catálogo de procesos disponibles — GET /api/v1/design/processes/catalog */
export interface ProcessCatalogItem {
  /** Clave única del proceso */
  key: string;
  /** Nombre descriptivo del proceso */
  name: string;
  /** Versión desplegada */
  version: number;
  /** Fecha de despliegue ISO 8601 */
  deployDate: string;
  /** Estado del proceso (ej. ACTIVE, ARCHIVED) */
  status: string;
  /** Patrón de formulario asociado (opcional) */
  formPattern?: string;
}

/** Estructura de error RFC 7807 ProblemDetail del backend */
export interface BpmnProblemDetail {
  /** URI del tipo de error */
  type: string;
  /** Título legible del error */
  title: string;
  /** Código HTTP */
  status: number;
  /** Descripción detallada del error */
  detail: string;
  /** Lista de errores de validación por campo (solo en 400) */
  errors?: Array<{ field: string; issue: string }>;
}
