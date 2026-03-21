import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import MainLayout from '../../components/layout/MainLayout.vue';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';

describe('US-051 CA-6: MainLayout Renderizado Dinámico (Iteración 47)', () => {
    let router: any;
    let authStoreMock: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        router = createRouter({
            history: createWebHistory(),
            routes: [{ path: '/', component: { template: '<div>Dashboard</div>' } }]
        });
    });

    it('Carga el menú iterativamente desde el Payload JSON de Entra ID/Store', async () => {
        // Simular respuesta JSON anidada del Menú de Navegación del Servidor
        const mockMenuTree = [
            {
                id: 'menu-admin',
                label: 'Administración',
                icon: 'ShieldIcon',
                children: [
                    { path: '/admin/users', label: 'Gestión Usuarios' },
                    { path: '/admin/roles', label: 'Matrices RBAC' }
                ]
            },
            {
                id: 'menu-workspace',
                path: '/workdesk',
                label: 'Mi Bandeja',
                icon: 'BriefcaseIcon'
            }
        ];

        // Se provee el Mock Tree al Componente (Supongamos que usa provide/inject o props)
        const wrapper = mount(MainLayout, {
            global: {
                plugins: [router],
                provide: { navigationMenu: mockMenuTree }
            }
        });

        await wrapper.vm.$nextTick();
        
        // Aserción 1: Validamos que iteró al nivel superior
        const menuLinks = wrapper.findAll('.nav-link-top');
        expect(menuLinks.length).toBeGreaterThanOrEqual(2);
        expect(wrapper.text()).toContain('Administración');
        expect(wrapper.text()).toContain('Mi Bandeja');

        // Aserción 2: Validamos las ramas anidadas
        const nestedLinks = wrapper.findAll('.nav-link-child');
        expect(nestedLinks.some(n => n.text().includes('Gestión Usuarios'))).toBe(true);
        expect(nestedLinks.some(n => n.text().includes('Matrices RBAC'))).toBe(true);
    });

    it('HOTFIX US-051 Regresión: Garantiza el anclaje del botón "Inicio" inyectado desde el JSON del Backend', async () => {
        const mockMenuTree = [
            {
                id: 'menu-home',
                path: '/home',
                label: 'Inicio',
                icon: 'HomeIcon'
            }
        ];

        const wrapper = mount(MainLayout, {
            global: {
                plugins: [router],
                provide: { navigationMenu: mockMenuTree }
            }
        });

        await wrapper.vm.$nextTick();
        
        // Aserción matemática estructural: El nodo 'Inicio' NO puede desaparecer del Layout
        const homeLink = wrapper.findAll('*').filter(node => node.text().includes('Inicio'));
        expect(homeLink.length).toBeGreaterThan(0);
    });
});
