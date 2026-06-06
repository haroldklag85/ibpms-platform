# 🤝 Handoff de Arquitectura: Sprint 1 (Fase Shift-Left Frontend)

> **Destinatario:** Agente de Frontend / Vue 3 / Pinia
> **Alcance:** Unit Testing en Capa Lógica y Seguridad (Code Freeze en vigor)
> **Directiva:** Mocks con Happy-DOM. DOM explícita limitación: Playwright está vetado temporalmente.

---

## Bloque 1: Aislamiento Pinia y Security Context (US-036)

**Objetivo:** Si un hacker inyecta localStorage forjado, el Store Pinia debe descartarlo basándose en expiración o validación interna de Roles.
**Acciones:**
1. Instalar/Validar config `vitest.config.ts` con `environment: 'happy-dom'` y soporte Vue-Test-Utils.
2. Crear `useAuthStore.spec.ts`.
3. Testear el pipeline de inicialización de roles, asegurando que un JWT sin rol administrativo colapse el flag `isAdmin` a `false`.

## Bloque 2: Workdesk Store y SLA Math (US-001, US-043)

**Objetivo:** El semáforo no debe atascar el sistema y el Ghost Deletion debe remover arreglos in-memory correctamente.
**Acciones:**
1. Crear `useSlaEngine.spec.ts`. Pasarle Mock-Dates manuales y probar que una tarea con SLA < 15% arroje el estado `UrgencyType.RED`.
2. Crear `useWorkdeskStore.spec.ts`. Inicializar el store con un array estático de 5 tareas. Disparar el evento artificial STOMP "Ghost Deletion" y comprobar que la longitud pasa a 4 instantáneamente.

## Bloque 3: Módulo B2C Transaccional & Formularios (US-003)

**Objetivo:** Probar sin UI que el parser de estructuras convierte objetos genéricos Zod correctamente.
**Acciones:**
1. Mapear tests sobre el `FormEngineService.ts` a nivel de parseo JSON.
2. Evitar renderizar `<form>`, únicamente el modelo de datos.

## Firmas de Recepción y Criterio de Gate final Frontend
- [ ] Leído y analizado.
- [ ] Stores cubiertos por `.spec.ts` en verde sin dependencias de ciclo Vue App.
- [ ] Comando `npm run test:unit` habilitado.
