/**
 * @module useProcessStore
 * @description Pinia store para la ejecución de procesos BPMN (US-007).
 * Gestiona el catálogo de procesos disponibles y la instanciación de nuevos casos.
 * @traceability US-007 — Ejecución BPMN, ADR-002 (Pinia State Management)
 */
import { defineStore } from 'pinia';
import { api } from '@/services/apiClient';
import type {
  StartProcessRequest,
  StartProcessResult,
  ProcessCatalogItem,
  BpmnProblemDetail,
} from '@/types/Process';

interface ProcessState {
  catalog: ProcessCatalogItem[];
  isLoadingCatalog: boolean;
  isStartingProcess: boolean;
  lastStartResult: StartProcessResult | null;
  error: string | null;
}

export const useProcessStore = defineStore('process', {
  state: (): ProcessState => ({
    catalog: [],
    isLoadingCatalog: false,
    isStartingProcess: false,
    lastStartResult: null,
    error: null,
  }),

  getters: {
    /** Procesos activos (filtra archivados) */
    activeProcesses: (state): ProcessCatalogItem[] =>
      state.catalog.filter((p) => p.status !== 'ARCHIVED'),

    /** Indica si hay un error activo */
    hasError: (state): boolean => state.error !== null,
  },

  actions: {
    /**
     * Obtiene el catálogo de procesos desplegados.
     * Reutiliza el endpoint existente GET /api/v1/design/processes/catalog.
     * @traceability US-007
     */
    async fetchCatalog(): Promise<void> {
      this.isLoadingCatalog = true;
      this.error = null;
      try {
        const { data } = await api.getCatalogProcesses();
        // El backend puede devolver array directo o envuelto en { content: [] }
        this.catalog = Array.isArray(data) ? data : (data.content ?? []);
      } catch (err: unknown) {
        this.error = this._extractErrorMessage(err);
        console.error('[useProcessStore] fetchCatalog failed:', err);
      } finally {
        this.isLoadingCatalog = false;
      }
    },

    /**
     * Inicia una nueva instancia de proceso BPMN contra el endpoint real.
     * POST /api/bpmn/instances → 201 Created
     * @traceability US-007, Zero-Mock Enforcement
     */
    async startProcess(request: StartProcessRequest): Promise<StartProcessResult | null> {
      this.isStartingProcess = true;
      this.error = null;
      try {
        const { data } = await api.startProcess(request);
        this.lastStartResult = data;
        return data;
      } catch (err: unknown) {
        this.error = this._extractErrorMessage(err);
        console.error('[useProcessStore] startProcess failed:', err);
        return null;
      } finally {
        this.isStartingProcess = false;
      }
    },

    /** Limpia el estado de error */
    clearError(): void {
      this.error = null;
    },

    /** Limpia el último resultado de inicio */
    clearLastResult(): void {
      this.lastStartResult = null;
    },

    /**
     * Extrae un mensaje de error legible del response del backend (RFC 7807).
     * Maneja ProblemDetail, errores de red y errores genéricos.
     */
    _extractErrorMessage(err: unknown): string {
      if (!err || typeof err !== 'object') return 'Error desconocido.';

      const axiosErr = err as { response?: { status?: number; data?: BpmnProblemDetail }; message?: string };

      if (!axiosErr.response) {
        return 'Error de conexión. Verifique que el servidor esté activo.';
      }

      const { status, data } = axiosErr.response;
      if (!data) return `Error HTTP ${status}`;

      // RFC 7807 ProblemDetail parsing
      if (data.detail) {
        const fieldErrors = data.errors?.map((e) => `${e.field}: ${e.issue}`).join('; ');
        return fieldErrors ? `${data.detail} — ${fieldErrors}` : data.detail;
      }

      if (data.title) return data.title;

      return `Error HTTP ${status}`;
    },
  },
});
