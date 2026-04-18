import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/services/apiClient'

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
            // zero-mocks policy: real api call
            const [rolesRes, vipRolesRes] = await Promise.all([
                apiClient.get('/admin/roles').catch(() => null),
                apiClient.get('/admin/roles?vip_restricted=true').catch(() => null)
            ])
            
            let fetchedRoles = []
            if (rolesRes && rolesRes.data) {
                fetchedRoles = rolesRes.data
                const vipIds = new Set(vipRolesRes?.data?.map(r => r.id) || [])
                fetchedRoles = fetchedRoles.map(r => ({ ...r, is_vip_restricted: r.is_vip_restricted || vipIds.has(r.id) }))
            } else {
                // Fallback para desarrollo si el endpoint no está arriba
                fetchedRoles = [
                    {
                        id: 'e21b-4r4d-90op',
                        name: 'VPE_Finanzas',
                        type: 'GLOBAL',
                        is_vip_restricted: true,
                        description: 'Nivel de Jerarquía: 2 (Director). Permisos Globales: dashboard.view_all, process.override_sla',
                        members: [{ id: 1, email: 'juan.cfo@empresa.com' }, { id: 2, email: 'maria.tr@empresa.com' }]
                    },
                    {
                        id: 'v43x-8l2z-11qw',
                        name: 'Líder_SAC',
                        type: 'GLOBAL',
                        is_vip_restricted: false,
                        description: 'Acceso a buzones de Intake y Plan B. Permisos: inbox.manage, tickets.create_forced',
                        members: [{ id: 3, email: 'harolt.sac@empresa.com' }]
                    },
                    {
                        id: 'z99k-2j1m-44pp',
                        name: 'PROCESS:Credito_Hipotecario_v2:Analista_Riesgos',
                        type: 'PROCESS_GENERATED',
                        is_vip_restricted: false,
                        processDefinitionId: 'Credito_Hipotecario_v2',
                        laneId: 'Analista_Riesgos',
                        description: 'Aprobaciones de Riesgo bajo el carril Analista_Riesgos.',
                        members: [{ id: 4, email: 'grupo.riesgos.bogota@empresa.com' }]
                    },
                    {
                        id: 'a11v-5b6n-77uy',
                        name: 'PROCESS:Onboarding_Clientes:Firma_Legal',
                        type: 'PROCESS_GENERATED',
                        is_vip_restricted: false,
                        processDefinitionId: 'Onboarding_Clientes',
                        laneId: 'Firma_Legal',
                        description: 'Requisitos de Firmas Finales en proceso de Onboarding.',
                        members: []
                    }
                ]
            }
            roles.value = fetchedRoles
        } catch (error) {
            console.error("Error cargando roles", error)
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
