# ADR 011: Gobernanza de Pirámide de Testing Multi-Nivel

**Fecha:** 2026-04-05
**Estado:** Aceptado
**Contexto:** Plataforma Core iBPMS (Backend & Frontend)

## Contexto y Problema

Durante las iteraciones tácticas tempranas (Ej: Iteración 69-DEV y certificación US-034), se identificó un riesgo sistémico en el aseguramiento de la calidad de integración:
1.  **Falsos Positivos de H2:** Los tests de base de datos se ejecutaban contra H2 (en memoria) en lugar de PostgreSQL real. Esto ocultó bugs temporales relacionados con el dialecto SQL (ej. `gen_random_uuid()` o bloqueos de transacciones) y la incompatibilidad con `pgvector`.
2.  **Mocking de Broken/MQ:** El comportamiento de enrutamiento complejo (Dead Letter Exchanges para expedientes) se validaba falseando envíos en memoria.
3.  **Bugs de Interfaz No Detectados:** `jsdom` en unit tests validaba que los props de Vue 3 estuviesen en el VDOM, pero ocultaba defectos de CSS, desbordamiento (overflow) o z-index en componentes renderizados en navegadores reales.
4.  **Seguridad Perimetral Asumida:** `MockMvc` operaba saltándose capas reales de los filtros HTTP, requiriendo mockeos engorrosos de `SecurityContextHolder` en lugar de probar los tokens JWT directamente.

## Decisión

Para mitigar el riesgo arquitectónico y alinear el código con la infraestructura Cloud-Native (V2), se instaura la **Gobernanza de Pirámide de Testing en 4 niveles** como política innegociable para cualquier Pull Request (PR) o commit a `main`:

1.  **Reemplazo Definitivo de H2 y Testcontainers por Infraestructura Estática Real:**
    *   **Backend (Por Defecto y CI/CD):** Se elimina explícitamente el uso de bases de datos en memoria (H2) por generar falsos positivos. Asimismo, **se prohíbe el uso de Testcontainers** para crear instancias efímeras debido a las limitaciones críticas de recursos computacionales y RAM en las estaciones de trabajo locales.
    *   **Mandato Arquitectónico:** Todos los tests de Integración (marcados con `@SpringBootTest` o `@DataJpaTest`) deben apuntar obligatoriamente a la base de datos y broker estáticos levantados previamente por el archivo `docker-compose.e2e.yml` (Puertos estándar: Postgres 5433, Rabbit 5673, Redis 6380), a través de las credenciales de `application-test.yml`. Esto preserva el protocolo Zero-Mock (uso de infraestructura genuina) pero evita el colapso de la máquina.
2.  **Test de Contrato BDD (REST Assured):**
    *   Se complementa `MockMvc` implementando `REST Assured` para validar de forma íntegra el flujo HTTP (black-box), inyectando JWTs malformados o faltantes para certificar la respuesta del filtro de autenticación perimetral (Ej. `DlqAdminControllerApiIT`).
3.  **Playwright Component Testing (CT):**
    *   **Frontend:** Se instala `@playwright/experimental-ct-vue`. Los componentes visuales complejos deberán someterse a tests CT (`npm run test:ct`) renderizando en motores Chromium/Webkit reales dentro del pipeline CI/CD.

## Consecuencias Positivas

*   **Zero Falsos Positivos Intangibles:** El código que aprueba localmente, aprueba de la misma manera exacta contra las versiones Linux/Docker de sus servicios paralelos.
*   **Postura Real ante Fallas Repetitivas (Resiliencia):** Se podrá testear el *Circuit Breaker* matando el contenedor temporal en medio del test mediante comandos del container API de `Testcontainers`.
*   **Testing Visual Genuino:** Las librerías de UI complejas se auditarán bajo el motor real que usan los analistas.

## Consecuencias Negativas

*   **Requisito de Infraestructura:** El desarrollador local NO podrá ejecutar `mvn test` en modo offline puro; requiere `Docker Desktop` o `Colima` activo.
*   **Tiempo de Pipeline Incrementado:** El arranque de los contenedores sumará aproximadamente 20-30 segundos al *boot-time* del *test phase*. Las ejecuciones CI/CD requerirán runners con perfiles de memoria más grandes.
*   **Curva de Mantenimiento Playwright:** Obliga al equipo de Front-end a mantener mapeos precisos de imports de estilos y componentes globales en el archivo `playwright/index.ts`.
