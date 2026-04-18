# Análisis Funcional Definitivo: US-005 (Desplegar y Versionar un Modelo de Proceso BPMN)

## 1. Resumen del Entendimiento
La US-005 fundamenta las capacidades CI/CD funcionales del PMO dentro de la plataforma. Dicta cómo un archivo `BPMN 2.0` se materializa en la base de datos como reglas activas de negocio. Incluye las lógicas extremadamente peliagudas de *Migraciones en Vuelo* (qué pasa con los procesos vivos al publicar reglas nuevas), el autodescubrimiento de Ciberseguridad (RBAC desde Carriles) y los *Pre-Flight Checkers* que previenen explosiones del motor backend por diagramas rotos o ambigüedades lógicas en las dependencias.

## 2. Objetivo Principal
Garantizar la Gobernanza Estricta en el Ciclo de Vida del Modelo. Evitar que un "despliegue rápido" contamine o arruine la experiencia formal de miles de casos que ya fluyen dentro de la máquina (Protección de Derechos Adquiridos = In-Flight Instances), asegurando al mismo tiempo la auditabilidad de todo cambio sistémico estructural (Versionamiento Transparente).

## 3. Alcance Funcional Definido
**Inicia:** Desde el lienzo del "Editor BPMN" (Pantalla 6) cuando el Arquitecto opríma la secuencia [Analizar] > [Testear] > [Desplegar].
**Termina:** En el cierre completo de la persistencia relacional en Camunda (Version `N+1`) y la inyección en CQRS. Cubre "Pre-Flights", Amnistías al migrar, tableros de pánico incidentes y Rollbacks transaccionales visualizados en el Workdesk.

## 4. Lista de Funcionalidades Incluidas
- **Pre-Flight Analyze de Ejecutabilidad:** Filtro barrera (Linting de Reglas iBPMS y Camunda). Validaciones Anti-Errores fatales.
- **Zero-Bypass Form Start:** Obliga a asociar formularos Zod a Start Events (Sincronía US-024).
- **Auto-Nomenclatura Variable Estricta:** Requisito mandatorio para instanciación.
- **RBAC Discovery:** Inferencia de roles auto-generados vía BPMN `Lanes`.
- **Grandfathering Forzoso (Ley del Abuelo):** Sin auto-migraciones peligrosas ciegas. V1 y V2 corren juntos en background.
- **No Guillotina / Bloqueo Topológico Duro:** Cirugía quirúrgica manual de migración solo si topológicamente ambos nodos en V1 y V2 existen. (No matar instancias si la V2 las anula).
- **Zero Data-Patching Humano:** Prohíbe inventar data obligatoria de V2 en migraciones de casos V1. Modifica la UX para pedirlo en vuelo. (Lazy Validation / Amnistía Técnica).
- **Late vs Deployment Binding (DMN):** Protección a resoluciones DMN frente al tiempo del token.
- **Rollback Instantáneo Histórico.** Revivir la V2 sobre una V3 con 1 tap.
- **Bloqueo Pesimista Editores (Lock).** Impide salvar un flujo si "maria.lopez" está editándolo.
- **Auto-Guardado (Borradores en Pantalla 6) y Sandbox de Prueba Efímero.** Cero rastro relacional del test.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Tratamiento Póstumo de Roles Fantasma / Zombies (⚠️ CA-6):** Al desplegar la V2, el sistema infiere el carril `Aprobadores` y le da un Rol. Si en la Versión 3 el carril se borra, el archivo US-005 asume "crear al desplegar éxitosamente" pero es ciego y amnésico al momento de *purgar roles expirados*. Esto inducirá a miles de Roles inútiles colgando en la BD/Keycloak causando contaminación.
- **Deadlock Eterno de Edición (⚠️ CA-16):** Asigna un *Lock Exclusivo* sobre el proceso `[Solicitud_Credito]` para el usuario Actual. El CA no define jamás qué pasa si "Maria.lopez" cierra el navegador forzosamente sin liberar el Lock. Omitir un mecanismo *"TTL Lock Expiration"* o un *"Force Break Lock"* de Administrador resultará en Procesos de Negocio inmodificables de por vida para toda el área.
- **Sincronización Transaccional de Cicatriz (CA-14):** Se inyecta una marca/franja visual en un caso sobre migraciones de V1 a V2. Pero al haber bases CQRS independientes para vista cliente (Pantalla 17), el CA no blinda explícitamente en qué API se deposita este payload inmutable del historial.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lógica generativa in-time (El CA-17 aclara que el Copiloto IA no se gatilla solo en tiempo real, sino en "On Demand" con un botón).
- Migración Automática Forzada Masiva sin control.

## 7. Observaciones de Alineación o Riesgos
**Riesgo Activo (Arquitectónico):** Las "Roles Zombies" de RBAC (CA-6) y los "Deadlocks de Edición Infinitos" (CA-16) causarán dolores en Producción en el Mes 2 post-GoLive al colapsar las métricas por exceso de registros nulos y bloqueos de interfaz en diseñadores. Hay que generar un SRE Patch para ambos.
