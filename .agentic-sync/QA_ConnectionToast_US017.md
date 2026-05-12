# Handoff: Agente QA - Certificación Zero-Mock Workdesk UI (US-017 / CA-19 al CA-26)

Este documento contiene las directrices, el contexto técnico y el comando exacto que debes ejecutar para certificar la estabilización de la interfaz de usuario del Workdesk, garantizando la eliminación definitiva del modal bloqueante "CQRS Engine" a favor del nuevo `ConnectionToast` no intrusivo.

## 🎯 Objetivo de la Misión
Ejecutar la suite de pruebas E2E correspondiente a la US-017 (Workdesk) para validar los Criterios de Aceptación (CA-19 al CA-26). Debes asegurar que la prueba corra contra el entorno real usando el perfil **Zero-Mock**, confirmando que el frontend se conecta de manera robusta al backend sin usar *stubbing* ni datos simulados.

---

## 🛠️ Contexto Arquitectónico y Prerrequisitos

Como Arquitecto Líder, he aplicado las siguientes mitigaciones previas que debes considerar en tu evaluación:
1. **Zero-Mock Pipeline:** Hemos configurado el proyecto de Playwright `Zero-Mock-E2E`. Este perfil desactiva cualquier intercepción de red que inyecte respuestas falsas. 
2. **Clean UI:** Se erradicó el modal rojo/bloqueante anterior. El nuevo `ConnectionToast` debe aparecer únicamente en la esquina inferior y auto-ocultarse sin interrumpir la navegación.
3. **Backend Estabilizado:** La recursividad infinita que causaba colapsos (errores HTTP 500) ha sido parcheada a nivel de base de datos JPA (`@JsonIgnore`), lo que significa que las respuestas de red ahora son estables.

---

## 🚫 Restricciones de Habilidades (QA Skills) y Gobernanza Zero-Mock

Bajo mi autoridad como Arquitecto Líder, se te **PROHÍBE** estrictamente utilizar las siguientes habilidades (skills) o funciones de Playwright durante esta certificación:
* **`page.route(...)`**: Queda completamente prohibida la intercepción de tráfico para forzar respuestas HTTP 200 o inyectar objetos JSON prefabricados.
* **`page.unroute(...)`** o **`context.route(...)`**.
* Alterar los objetos de estado global (como `localStorage` o cookies) para eludir la validación del backend real.
* Inyectar configuraciones de Vitest (`vi.mock()`) dentro del entorno E2E.

Si el test E2E falla por problemas de red o mapeo, tu instrucción es documentar el error exacto (Logs y Traces) y **NO** intentar parchear el test modificando el flujo de red. El entorno de UAT debe reflejar la realidad del servidor.

---

## 🚀 Instrucciones de Ejecución

Debes abrir tu consola terminal en el directorio `frontend/` y ejecutar el siguiente comando usando el framework Playwright:

```bash
cd c:\Users\HaroltAndrésGómezAgu\ProyectoAntigravity\ibpms-platform\frontend
npx playwright test e2e/certification/us017-connection-toast.e2e.spec.ts --project="Zero-Mock-E2E"
```

> [!TIP]
> Si encuentras falsos negativos por sincronización visual, puedes ejecutar la prueba con `--headed` o `--debug` para observar el flujo del `ConnectionToast` y ajustar los tiempos de espera (`waitForTimeout`) si la red real tiene mayor latencia que los antiguos mocks.

---

## 📋 Criterios de Éxito Esperados (Validación Final)

Al finalizar la ejecución, debes reportar al usuario lo siguiente:
- [ ] Confirmar que los 8 Criterios de Aceptación (CA-19 a CA-26) pasaron exitosamente bajo el perfil `Zero-Mock-E2E`.
- [ ] Confirmar que no hay errores 500 originados desde el backend durante la interacción de reconexión.
- [ ] En caso de fallo (ROJO), generar el volcado del log en el archivo `sprint_6_bugs.md` documentando el punto de quiebre. En caso de éxito (VERDE), autorizar el sellado formal de la US-017.

**[FIN DEL HANDOFF]** - *Puedes proceder a tu ejecución.*
