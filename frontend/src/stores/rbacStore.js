import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useRbacStore = defineStore('rbac', () => {
    // Estado
    const roles = ref([])
    const isLoading = ref(false)
    const auditLogs = ref([
        { timestamp: '10:45am', message: 'Administrador añadió a @Pedro al rol VPE_Finanzas' },
        { timestamp: '09:30am', message: 'Al desplegar BPMN_Crédito, el sistema autogeneró el rol PROCESS:Credito:Analista_Riesgos' }
    ])

    // Getters computados
    const globalRoles = computed(() => roles.value.filter(r => r.type === 'GLOBAL'))
    const processRoles = computed(() => roles.value.filter(r => r.type === 'PROCESS_GENERATED'))

    // Acciones
    async function fetchRoles() {
        isLoading.value = true
        try {
            // Simulación de llamada API HTTP GET al backend construido en Sprint 22
            await new Promise(resolve => setTimeout(resolve, 800))
            roles.value = [
                {
                    id: 'e21b-4r4d-90op',
                    name: 'VPE_Finanzas',
                    type: 'GLOBAL',
                    description: 'Nivel de Jerarquía: 2 (Director). Permisos Globales: dashboard.view_all, process.override_sla',
                    members: [{ id: 1, email: 'juan.cfo@empresa.com' }, { id: 2, email: 'maria.tr@empresa.com' }]
                },
                {
                    id: 'v43x-8l2z-11qw',
                    name: 'Líder_SAC',
                    type: 'GLOBAL',
                    description: 'Acceso a buzones de Intake y Plan B. Permisos: inbox.manage, tickets.create_forced',
                    members: [{ id: 3, email: 'harolt.sac@empresa.com' }]
                },
                {
                    id: 'z99k-2j1m-44pp',
                    name: 'PROCESS:Credito_Hipotecario_v2:Analista_Riesgos',
                    type: 'PROCESS_GENERATED',
                    processDefinitionId: 'Credito_Hipotecario_v2',
                    laneId: 'Analista_Riesgos',
                    description: 'Aprobaciones de Riesgo bajo el carril Analista_Riesgos.',
                    members: [{ id: 4, email: 'grupo.riesgos.bogota@empresa.com' }] // Simula mapeo de grupo AD
                },
                {
                    id: 'a11v-5b6n-77uy',
                    name: 'PROCESS:Onboarding_Clientes:Firma_Legal',
                    type: 'PROCESS_GENERATED',
                    processDefinitionId: 'Onboarding_Clientes',
                    laneId: 'Firma_Legal',
                    description: 'Requisitos de Firmas Finales en proceso de Onboarding.',
                    members: []
                }
            ]
        } catch (error) {
            console.error("Error cargando roles desde API", error)
        } finally {
            isLoading.value = false
        }
    }

    return {
        roles,
        auditLogs,
        isLoading,
        globalRoles,
        processRoles,
        fetchRoles
    }
})
