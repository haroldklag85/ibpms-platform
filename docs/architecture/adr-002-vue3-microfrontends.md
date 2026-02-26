# ADR 002: Elección de Vue 3 y Vite para Experiencia de Usuario y Micro-Frontends

**Estado:** Aceptado
**Fecha:** 2026-02-23
**Contexto de la Decisión:** Arquitectura Frontend V1 y V2

## Contexto y Problema
La plataforma iBPMS (Intelligent Business Process Management System) requiere un motor de UI capaz de renderizar formularios dinámicos en tiempo real, definidos en la base de datos a través de JSON Schemas. Dado que la plataforma soporta múltiples procesos de negocio cambiantes (Case Management), compilar el frontend cada vez que un proceso cambia es inaceptable. Además, para la V2, la arquitectura técnica evoluciona hacia un modelo de **Micro-Frontends**, donde diferentes equipos podrán inyectar "Componentes Lego" aislados en una Bandeja Web unificada (Shell).

Debíamos evaluar qué framework de JavaScript moderno (React, Angular o Vue) ofrecía el mejor balance entre reactividad profunda para data dinámica, velocidad de desarrollo (Time-to-Market) y soporte para micro-arquitecturas.

## Decisión
Hemos decidido **utilizar Vue 3 (con Composition API y Script Setup) empacado y construido mediante Vite** como la tecnología base para el Frontend de la plataforma.

## Justificación
1. **Curva de Aprendizaje y Mantenibilidad:** Vue 3 ofrece una curva de aprendizaje considerablemente más suave para equipos Full-Stack (Java/Spring Boot), en comparación con la verbosidad de Angular o la complejidad de los hooks profundos de React `useEffect`.
2. **Reactividad Proxy Nativa:** Para el renderizado de JSON Schemas profundos y profundamente anidados (común en formularios médicos o financieros), el sistema de reactividad de Vue 3 basado en *Proxies* es excepcionalmente eficiente y predecible, mitigando renderizados fantasma.
3. **Agilidad en el Desarrollo (Vite):** Vite proporciona un *Hot Module Replacement (HMR)* casi instantáneo, acelerando la creación de PoCs y el desarrollo iterativo.
4. **Soporte para Micro-Frontends:** La abstracción limpia de los Single File Components (`.vue`) y el ecosistema de Vite (ej. vite-plugin-federation) facilitan inmensamente la exportación de componentes aislados (MFE) que serán consumidos por el "Shell" principal asíncronamente.

## Consecuencias
*   **Positivas:** 
    *   Desarrollo rápido de los Workbenches (Case Management).
    *   Fácil construcción de la librería de renderizado dinámico `JSON -> UI Validator`.
    *   Arquitectura "Lego" habilitada desde el día 1.
*   **Negativas (Riesgos a mitigar):** 
    *   El equipo Backend debe aprender los fundamentos de Vue 3. 
    *   Se debe establecer una política estricta de "No Lógica de Negocio en la UI". La validación final de los formularios DEBE ocurrir en el Backend, asumiendo siempre el paradigma optimista de "Eventual Consistency".
