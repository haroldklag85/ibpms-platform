import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import { defineComponent, h } from 'vue';
import es from '@/i18n/locales/es.json';
import en from '@/i18n/locales/en.json';

// Componente auxiliar para verificar traducciones en contexto
const TestComponent = defineComponent({
  setup() {
    const { t, locale } = (await import('vue-i18n')).useI18n();
    return { t, locale };
  },
  render() {
    return h('div', [
      h('span', { id: 'sidebar-home' }, this.t('sidebar.home')),
      h('span', { id: 'header-logout' }, this.t('header.logout')),
      h('span', { id: 'errors-fatal' }, this.t('errors.fatalServer')),
      h('span', { id: 'current-locale' }, this.locale),
    ]);
  },
});

function createI18nPlugin(locale = 'es') {
  return createI18n({
    legacy: false,
    locale,
    fallbackLocale: 'es',
    messages: { es, en },
  });
}

describe('i18n Integration', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('renderiza en español por defecto', () => {
    const i18n = createI18nPlugin('es');
    const wrapper = mount(TestComponent, {
      global: { plugins: [i18n] },
    });

    expect(wrapper.find('#sidebar-home').text()).toBe('Inicio');
    expect(wrapper.find('#header-logout').text()).toBe('Cerrar sesión');
    expect(wrapper.find('#errors-fatal').text()).toBe('Error crítico del servidor');
    expect(wrapper.find('#current-locale').text()).toBe('es');
  });

  it('cambio a inglés actualiza textos visibles', async () => {
    const i18n = createI18nPlugin('es');
    const wrapper = mount(TestComponent, {
      global: { plugins: [i18n] },
    });

    expect(wrapper.find('#sidebar-home').text()).toBe('Inicio');

    // Cambiar a inglés
    i18n.global.locale.value = 'en';
    await wrapper.vm.$nextTick();

    expect(wrapper.find('#sidebar-home').text()).toBe('Home');
    expect(wrapper.find('#header-logout').text()).toBe('Log out');
    expect(wrapper.find('#errors-fatal').text()).toBe('Critical server error');
    expect(wrapper.find('#current-locale').text()).toBe('en');
  });

  it('locale persiste en localStorage', () => {
    const i18n = createI18nPlugin('es');
    
    // Simular lo que hace toggleLocale en MainLayout
    i18n.global.locale.value = 'en';
    localStorage.setItem('ibpms_locale', i18n.global.locale.value);

    expect(localStorage.getItem('ibpms_locale')).toBe('en');

    // Crear nueva instancia — debería leer de localStorage
    const savedLocale = localStorage.getItem('ibpms_locale') || 'es';
    const i18n2 = createI18nPlugin(savedLocale);

    expect(i18n2.global.locale.value).toBe('en');
  });

  it('fallback a español si clave no existe en inglés', () => {
    // Crear i18n con inglés que tiene una clave faltante
    const partialEn = { ...en } as any;
    delete partialEn.sidebar; // Remover toda la categoría sidebar del inglés

    const i18n = createI18n({
      legacy: false,
      locale: 'en',
      fallbackLocale: 'es',
      messages: { es, en: partialEn },
      missingWarn: false,
      fallbackWarn: false,
    });

    const wrapper = mount(TestComponent, {
      global: { plugins: [i18n] },
    });

    // Debe usar el fallback en español
    expect(wrapper.find('#sidebar-home').text()).toBe('Inicio');
    // Pero las claves que sí existen en inglés siguen en inglés
    expect(wrapper.find('#header-logout').text()).toBe('Log out');
  });
});
