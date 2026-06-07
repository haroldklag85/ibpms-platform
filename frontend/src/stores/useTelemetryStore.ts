import { defineStore } from 'pinia';
import { api } from '@/services/apiClient';

export interface TelemetryInstance {
    id: string;
    processDefinitionId: string;
    businessKey?: string;
    startTime: string;
    endTime?: string;
    state: string; // ACTIVE, COMPLETED, SUSPENDED
}

export interface TelemetryIncident {
    id: string;
    incidentType: string;
    incidentMessage: string;
    executionId: string;
    activityId: string;
    processInstanceId: string;
    processDefinitionId: string;
    incidentTimestamp: string;
}

export const useTelemetryStore = defineStore('telemetry', {
    state: () => ({
        instances: [] as TelemetryInstance[],
        incidents: [] as TelemetryIncident[],
        isLoading: false,
        error: null as string | null
    }),
    actions: {
        async fetchInstances(status?: string) {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await api.getTelemetryInstances(status);
                this.instances = response.data || [];
            } catch (err: any) {
                this.error = 'No se pudieron cargar las instancias. Verifica la conexión con el servidor.';
                console.error('Error fetching telemetry instances:', err);
                this.instances = [];
            } finally {
                this.isLoading = false;
            }
        },
        async fetchIncidents() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await api.getTelemetryIncidents();
                this.incidents = response.data || [];
            } catch (err: any) {
                this.error = 'No se pudieron cargar los incidentes. Verifica la conexión con el servidor.';
                console.error('Error fetching telemetry incidents:', err);
                this.incidents = [];
            } finally {
                this.isLoading = false;
            }
        }
    }
});
