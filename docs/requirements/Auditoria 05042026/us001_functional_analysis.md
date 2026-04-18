# Análisis Funcional y de Entendimiento: US-001

## Historia Analizada
**US-001: Obtener Tareas Pendientes en el Workdesk**

---

### 1. Resumen del Entendimiento

La US-001 define la **pantalla de aterrizaje operativa** (Pantalla 1 — Workdesk) del iBPMS. Es el centro de comando donde el operario ve todas sus tareas pendientes, sin importar si vienen de un proceso automatizado (BPMN/Camunda) o de un proyecto ágil (Kanban/Gantt). La pantalla mezcla ambos mundos en una sola tabla unificada.

La historia se estructura en **4 dominios**:

1. **Rendimiento y Anti-DDoS (CA-01 a CA-03, CA-09 a CA-10):** Paginación del lado del servidor, caché Redis para la oleada matutina de usuarios (8 AM), búsqueda con índices en base de datos, Skeleton Loaders, y Hard Limit que rechaza peticiones mayores a 100 registros.

2. **UX, Accesibilidad y Memoria (CA-04 a CA-05, CA-11 a CA-13):** Semáforos SLA en tiempo real con un solo temporizador global (en vez de uno por tarjeta), KeepAlive para preservar la pantalla al navegar, paginación sticky, Card Layout en móviles, animaciones CSS al desaparecer tareas reclamadas, y protección de privacidad operativa.

3. **Seguridad (CA-06, CA-14 a CA-15):** DTOs sanitizados (sin datos sensibles en la pestaña "Network"), aislamiento Multi-Tenant con filtro obligatorio `tenantId`, prevención de inyecciones SQL, prevención IDOR en delegación, y destrucción de sesión ante errores 401.

4. **Enrutamiento Inteligente (CA-07 a CA-08, CA-16 a CA-18):** Modo "Atender Siguiente" (Anti Cherry-Picking) con cruce de habilidades del operario, degradación elegante ante caída de Camunda, desempate por impacto financiero, y badge visual `[Impacto 🔥]`.

---

### 2. Objetivo Principal

Garantizar que el operario de negocio vea sus tareas pendientes priorizadas matemáticamente por SLA e impacto financiero, sin posibilidad de esquivar las tareas difíciles, en una interfaz que soporte cientos de usuarios simultáneos sin degradarse, y que combine orgánicamente tareas de procesos automatizados (BPMN) con tareas de proyectos ágiles (Kanban/Gantt) en una sola vista.

---

### 3. Alcance Funcional Definido

| Dimensión | Hasta Dónde Llega | Dónde Termina |
|---|---|---|
| **Visualización** | Lista/Grilla unificada con 5 columnas: Nombre, SLA, Estado, Avance, Recurso | No incluye la ejecución de la tarea (eso es US-029) |
| **Búsqueda** | Server-side con pg_trgm + debounce 300ms (CA-10) | No busca en tareas históricas cerradas ni suspendidas |
| **Tiempo Real** | WebSocket/SSE para desaparición de tareas reclamadas (CA-06, CA-13) | No hay chat en tiempo real ni notificaciones entre usuarios |
| **SLA** | Semáforo Ticking Engine con un solo temporizador global (CA-05, CA-11) | No calcula SLA; solo lo muestra. El cálculo viene de Camunda/BD |
| **Delegación** | Toggle para ver bandeja de subalternos (CA-04, CA-15) | Solo hacia abajo jerárquicamente (no lateral entre pares) |
| **Anti Cherry-Picking** | Feature Toggle + Skill-Based Routing (CA-08, CA-16) | No incluye reglas de negocio de las "habilidades" (skills); solo consume el array |
| **Degradación** | Kanban funciona si Camunda cae (CA-07, CA-18) | No hay modo offline completo; requiere BD relacional activa |
| **Responsive** | Card Layout en menor a 768px (CA-12) | No hay app nativa móvil |
| **Paginación** | Server-side estricta (CA-10) | No hay scroll infinito; es paginación clásica |

---

### 4. Lista de Funcionalidades Incluidas

#### A. Rendimiento y Protección
1. Paginación server-side con primer bloque de tareas (CA-01)
2. Ordenamiento forzoso por SLA más crítico (CA-01)
3. Búsqueda server-side con índices pg_trgm y debounce 300ms (CA-10)
4. Caché Redis para oleada matutina (CA-10)
5. Hard Limit de 100 registros por petición (CA-10)
6. Skeleton Loader (prohibido spinner bloqueante) (CA-10)
7. Máximo 15 tarjetas por página visual (CA-09)

#### B. UX y Accesibilidad
8. Grilla Unificada con 5 columnas estandarizadas + badge visual de tipo (CA-03)
9. Toggle de delegación para ver bandeja de subalternos (CA-04)
10. Semáforo SLA en tiempo real con iconografía para daltónicos (CA-05, CA-11)
11. Global Heartbeat Store con requestAnimationFrame (CA-11)
12. Interruptor Mute para silenciar alertas sonoras (CA-11)
13. KeepAlive para preservar filtros y scroll (CA-12)
14. Redirección automática a página 1 si la página actual queda vacía (CA-12)
15. Empty State gamificado cuando la bandeja llega a cero (CA-12)
16. Densidad condensada + Card Layout en móviles (CA-12)
17. Tooltips Zero-Click sobre el nombre de la tarea (CA-12)
18. Paginación sticky arriba y abajo (CA-12)

#### C. Tiempo Real
19. Desaparición de tareas reclamadas vía WebSocket/SSE (CA-06, CA-13)
20. Payload WebSocket atómico: solo acción e id (CA-13)
21. Throttling de 2 segundos para actualizaciones masivas (CA-13)
22. Animación CSS opacity:0 con Toast discreto al desaparecer (CA-13)
23. Privacidad operativa: "En gestión por otro Agente" sin nombre (CA-13)

#### D. Seguridad
24. DTO sanitizado sin PII ni variables internas de Camunda (CA-14)
25. Columnas rígidas sin polimorfismo en V1 (CA-14)
26. Filtro tenantId obligatorio + bind ORM anti-SQLi (CA-14)
27. Destrucción de sesión ante error 401 (CA-14)
28. Validación RBAC perimetral para delegación (CA-15)
29. Prevención IDOR ante manipulación de URL (CA-15)
30. Banner permanente "Estás viendo el escritorio de [Nombre]" (CA-15)

#### E. Enrutamiento Inteligente
31. Feature Toggle "Atender Siguiente" con Audit Log (CA-08, CA-16)
32. Skill-Based Routing: cruce de tarea vs habilidades del operario (CA-16)
33. Mecanismo de "Pausa/Skipeo Justificado" (CA-16)
34. Desempate por Prioridad de Impacto Financiero, luego Fecha de Creación (CA-17)
35. NULLS LAST para tareas sin fecha de vencimiento (CA-17)
36. Badge Impacto que fuerza Top 1 por impacto financiero (CA-17)
37. Columna "Avance": mapeo de nombre de tarea BPMN contra total de etapas (CA-17)
38. Degradación elegante cargando solo Kanban si Camunda cae (CA-07, CA-18)
39. Toast de degradación: "Sincronización BPMN degradada" (CA-07, CA-18)
40. Priorización de tablero general al re-login en otra máquina (CA-18)

---

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas

#### GAP-1: Contradicción de Paginación y Búsqueda (CA-01 vs CA-02 vs CA-09 vs CA-10)

Cuatro criterios se contradicen sobre cuántas tareas se cargan y cómo se busca:
- CA-01 dice: "El backend retorna el primer bloque de 50 tareas."
- CA-09 dice: "Máximo 15 tarjetas por página."
- CA-10 dice: "Hard Limit de 100 registros y búsqueda SOLO del lado del servidor."
- CA-02 dice: "El Frontend filtra sobre las 50 tareas precargadas en memoria Y en paralelo busca en el servidor."

El CA-02 y el CA-10 dicen cosas opuestas (búsqueda local vs búsqueda solo en servidor).

**Resolución recomendada:** CA-09 (15 tarjetas) gana como límite visual. CA-10 anula al CA-02. El "50" del CA-01 fue ejemplo.

#### GAP-2: Ausencia de Contrato API para la Grilla Unificada

Otras historias definen endpoints REST. La US-001, siendo la pantalla principal, no define el contrato API del Workdesk.

#### GAP-3: Algoritmo de Skill-Based Routing sin Definir (CA-16)

El CA-16 dice que cruzará la tarea contra las habilidades del operario, pero no define:
- Dónde se registran las habilidades de cada empleado.
- Si las habilidades son etiquetas simples o tienen niveles.
- Qué pasa si ninguna tarea coincide con los skills del operario.
- Si el "Skipeo Justificado" usa motivos predefinidos o texto libre.

#### GAP-4: Filtros de la Grilla no Especificados

La tabla tiene buscador de texto pero NO filtros como: "Solo tareas BPMN", "Solo tareas vencidas", "Solo tareas del proyecto X". Un operario con 200 tareas necesita filtros para organizar su trabajo.

#### GAP-5: Métrica de "Avance" (Columna 4) sin Fórmula Definida

La columna "Avance" no define:
- Para tareas BPMN: si es posición ordinal de la tarea dividida entre total de etapas.
- Para tareas Kanban: si es la columna actual dividida entre las columnas totales.
- Si se muestra como porcentaje (75%), fracción (3/4), o barra visual.

---

### 6. Lista de Exclusiones (Fuera de Alcance V1)

1. Ejecución de la Tarea (pertenece a US-029).
2. Columnas dinámicas por tipo de proceso (diferido a V2 por rendimiento).
3. Scroll infinito (paginación clásica).
4. App nativa móvil (solo responsive Card Layout).
5. Chat en tiempo real entre operarios.
6. Cálculo de SLA (solo visualización; el cálculo viene de Camunda/BD).
7. Modo offline completo (requiere BD relacional activa).
8. Delegación lateral entre pares (solo hacia abajo jerárquicamente).
9. Notificaciones push externas (email/SMS).
10. Auditoría detallada de quién vio qué tarea (solo reclamo en US-002).

---

### 7. Observaciones de Alineación o Riesgos para Continuar

**Riesgo Principal:** Los CAs 01, 02, 09 y 10 se contradicen sobre paginación y búsqueda. Requiere resolución formal como CA de remediación.

**Dependencias Externas de la US-001:**
- **US-002 (Reclamar Tarea):** Los WebSockets dependen de que US-002 publique el evento de asignación.
- **US-029 (Completar Tarea):** Toda ejecución de tarea está fuera de alcance.
- **US-036 (RBAC):** Delegación segura y Skill-Based Routing consumen la matriz de roles de Pantalla 14.
- **US-005 (Despliegue BPMN):** La columna "Avance" necesita la estructura del proceso BPMN.
- **US-008 (Kanban):** Las tareas Kanban de la grilla vienen del módulo Kanban.
- **US-031 (Gantt):** Las tareas de proyectos tradicionales también se consolidan.

**Fortalezas Sobresalientes:**
1. Global Heartbeat Store (CA-11) = cero fugas de memoria.
2. WebSocket Atómico (CA-13) = 99% ahorro de red.
3. Empty State Gamificado (CA-12) = psicología operativa inteligente.
4. Privacidad Operativa (CA-13) = prevención de conflictos laborales.
5. Anti Cherry-Picking con Feature Toggle (CA-08, CA-16) = pragmatismo operativo.
