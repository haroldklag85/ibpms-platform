---
name: Zero-Mock & Real Database Enforcement Protocol
description: Regla de gobernanza estricta para erradicar el uso de mockAdapter.ts y obligar a pruebas E2E reales contra la base de datos Dockerizada, garantizando que Frontend, Backend y QA colaboren sobre un Full-Stack genuino.
version: 1.0.0
---

# 🛑 Protocolo Zero-Mock y Pruebas E2E Reales

## 📌 Propósito
La arquitectura del proyecto prohíbe terminantemente crear "falsos positivos" mediante interceptores de red en el frontend (ej. `mockAdapter.ts`). Este anti-patrón ha causado que Pruebas E2E pasen con éxito mientras el entorno de Base de Datos y Backend se encontraba funcionalmente inoperativo. 

Todo agente (Frontend, Backend, y QA) DEBE asegurar que el ecosistema entero funciona como un ente integrado de Extremo a Extremo, apoyado en fuentes de datos verídicas y un backend de respuesta real.

---

## 🚫 Anti-Patrones Estrictamente Prohibidos

1. **PROHIBIDO EL USO DE `mockAdapter.ts` (O Similares)**
   - Ningún Agente Frontend tiene permitido encender, reactivar, extender o sugerir el uso de interceptores Axios/Fetch para silenciar integraciones no resueltas. 
   - El código de red debe "Fallar Rápido" (Fail-Fast) con `404` o `500` si el backend no expone el servicio. 

2. **PROHIBIDO EL TESTEO SOBRE AIRE (QA)**
   - Ningún script de Playwright o Cypress/QA puede ser validado sobre un entorno falso. Es una violación de seguridad si el Playwright pasa sin mutar el estado en la base de datos de Docker.

3. **PROHIBIDA LA POSTERGACIÓN EN BACKEND**
   - El Agente Backend no puede diferir la creación de tablas JPA o Migraciones argumentando que "El Frontend está mockeando el flujo". La base de datos relacional dicta la realidad absoluta.

4. **PROHIBIDO EL USO DE TESTCONTAINERS (Hardware Limitations)**
   - Debido a las restricciones severas de CPU y RAM de las laptops locales de desarrollo, **ESTÁ ESTRICTAMENTE PROHIBIDO instanciar Testcontainers** en las pruebas de integración de Backend. Levantar contenedores efímeros satura la máquina, provocando timeouts. Las pruebas deben ejecutarse apuntando siempre a la infraestructura de contenedores estáticos (BD ya viva) definida en el `docker-compose.e2e.yml`.

---

## ✅ Directivas Específicas por Rol

### 💻 1. Agentes Frontend
Si un endpoint falla porque el backend aún no ha finalizado una historia de usuario, tu deber es implementar resiliencia real (**Graceful Degradation**):
- Captura el error de red (vía `try/catch` o el store de Axios).
- Renderiza componentes visuales indicando "Servicio No Disponible Temporalmente" y alerta sobre la consola real.
- Bajo ninguna circunstancia puedes enmudecer el error interceptando la red con diccionarios de datos harcodeados.

### 🔧 2. Agentes Backend
- Tu modelo de Entidades (JPA) y tus ramas de Migración (`db/changelog/*`) son la única base sólida sobre la cual respira el ecosistema.
- Asegúrate de nutrir siempre el archivo `seed-e2e.sql` asegurándote de que la semilla refleje un estado inicial completo y funcional para el agente de QA.

### 🧪 3. Agentes de Quality Assurance (QA / Playwright)
- Tus pruebas E2E deben validar el circuito completo (*Full-Stack*). Los navegadores *headless* se levantan de manera local, así que procura mantener los scripts óptimos.
- **Mandato Cero Mocks:** Las pruebas unitarias/integración de Backend siguen obligadas a cumplir este protocolo. Para que la máquina no colapse, **tu entorno objetivo NUNCA es Testcontainers**.
- **Entorno de Pruebas Obligatorio:** Tus comandos y pruebas de integración deben configurarse mediante el `application-test.yml` para conectarse a los puertos fijos del `docker-compose.e2e.yml` que ya está corriendo en segundo plano (ej. Postgres en 5433). Valida explícitamente que esos contenedores estáticos están recibiendo la carga.

---

## 🎯 Gatillo de Ejecución
Este SKILL se dispara cada vez que un Agente recomiende usar "fijaciones de datos", "dummies", "mocks en el state de vue" o cuando un agente de QA comience a escribir scripts de Playwright/Vitest orientados a historias de validación técnica.
