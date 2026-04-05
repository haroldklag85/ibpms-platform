# Análisis Funcional Definitivo: US-025 (Experiencia de 'Cards' Dinámicas por Rol UX)

## 1. Resumen del Entendimiento
La US-025 dictamina toda la gobernanza del Frontend de cara a los usuarios (Operarios, Admins, Clientes). Condiciona estrictamente cómo Vue.js debe comportarse (Client Side Rendering, Virtual Scrolling, Ocultamiento en vez de Disabled) para salvaguardar la carga cognitiva y blindar la aplicación de vulnerabilidades visuales.

## 2. Objetivo Principal
Asegurar una interfaz paramétrica que muta perimetralmente según la JWT Identity y que prioriza la ergonomía y la protección asíncrona (Optimistic UI, Loading Skeletons, Socket notifications).

## 3. Alcance Funcional Definido
**Inicia:** El Backend inyecta variables de permisos en el JWT Payload.
**Termina:** El nodo de Vue Router/Store procesa las variables y corta el DOM.

## 4. Lista de Funcionalidades Incluidas
- **Layout y A11y:** CSR estricto (CA-20), Sidebar colapsable (CA-26), Skeleton to Spinner (CA-11), DOM Removal no disabled (CA-10). Tab-based UI (CA-29).
- **Control RBAC y Tokens:** Guards perimetrales (CA-5), selector de perfil en colisiones múltiples (CA-6), Soft-Lock de inactividad por modal (CA-27), Log-in forzoso ante cambios (CA-7).
- **Virtualización:** Vista scroll de +5,000 Action Cards limitadas en DOM (CA-22).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Crash de Promesas Axios en el Soft-Lock Floating (⚠️ CA-27):** El Criterio ordena inyectar un Modal visual para rescatar una sesión vencida "congelando todo sin ir al Login". **GAP:** Cuando el JWT expira, el backend arroja un `401 Unauthorized` frente a un request. El Vue Router dibujará el Modal, pero la Promesa/Request original enviada por Axios tirará Excepción matando el proceso de fondo (Ej: estaba guardando una tarea compleja). Si no se diseña una "Cola de Retención Axios" (Request Queue Interceptor) que pause la Promesa hasta que el Modal resuelva el nuevo JWT y luego reinyecte el request, el usuario "destrabará la pantalla" pero su click anterior habrá muerto silenciosamente.
- **Riesgo Impersonación (⚠️ CA-9):** Exige "Ver Sistema Como" para emular bugs. Si un SuperAdmin usa impersonación, absorbe el Token de un Operario. ¿Se audita en base de datos qué hizo el SuperAdmin con el token del Operario? Se necesita Header `X-Impersonated-By`.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Accesibilidad tipo Lectores de Pantalla (Screen Readers / JAWS) no es exigido estrictamente, solo Ring Focus visual.

## 7. Observaciones de Alineación o Riesgos
Excelente dictamen arquitectónico (DOM Removal > Disabled HTML) que mitiga vectores donde atacantes remueven el CSS attribute mediante `DevTools` F12 para emitir Posts a operaciones financieras.
