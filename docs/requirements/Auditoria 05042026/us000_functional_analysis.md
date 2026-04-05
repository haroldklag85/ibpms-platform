# Análisis Funcional y de Entendimiento: US-000

## Historia Analizada
**US-000: Resiliencia Integrada y Enmascaramiento PII Visual**

---

### 1. Resumen del Entendimiento
La US-000 no detalla un flujo de negocio específico (como la creación de un caso), sino que decreta **fundaciones no-funcionales y arquitectónicas universales** ("Criterios Universales") que deben cumplirse en todo el ecosistema iBPMS. Su propósito es definir un escudo protector transversal que aborda la resiliencia en la capa de interfaz, la estandarización de errores de validación, la prevención de colisiones concurrentes (Optimistic Locking) y la protección física en pantalla de los Datos de Identificación Personal (PII).

### 2. Objetivo Principal
Proteger la estabilidad visual (UX) y salvaguardar los datos confidenciales corporativos, dictaminando patrones de manejo de errores, interceptores y enmascaradores, para que frente a caídas de red, ataques, problemas de integridad de datos transaccionales, o filtraciones de información sensible, la plataforma no exponga debilidades visuales ni información protegida (TC, SSN, Médicos).

### 3. Alcance Funcional Definido
El alcance abarca todas las pantallas del Frontend (SPA) de forma transversal (Global) y la capa del API Gateway/BFF del Backend. Termina estrictamente en la interfaz de usuario para el manejo de excepciones HTTP (50x, 400, 409) interactuando con el Backend, y en el enmascaramiento superficial (vía LLM o regex en el momento de despacho o recepción) para ocultar información PII no estructurada.

### 4. Lista de Funcionalidades Incluidas
1. **Fallback UI Interceptor (HTTP 5xx):** Capturador global en Frontend (React/Vue/Angular) para montar un componente `[ErrorStateGlobal]` previniendo la "Pantalla Blanca".
2. **Sanitización de Logs de Producción:** El Backend volcará el rastreo (StackTrace) íntegro en herramientas ELK sin devolverlo jamas en la respuesta JSON pública.
3. **Mapeo Semántico de Validaciones Zod/DTO (HTTP 400/422):** Devuelve errores estandarizados (`{field, issue, translatedMessage}`) para iluminar en rojo de forma perimetral únicamente los `<input>` específicos que fallaron.
4. **Optimistic Locking UI (HTTP 409):** Soporte reactivo en la UI para notificar "Conflictos de Concurrencia" cuando otro operario guardó una `Version N` sobre la `Version N-1` que intentamos someter, obligando a re-pintar.
5. **PII Redaction Interceptor:** Componente intermedio (regex o agente LLM) antes de inyectar texto libre a la pantalla del operador, tachando patrones numéricos TC o SSN con un comodín de reemplazo `[CONFIDENCIAL - CLASE PII]`.

### 5. Lista de Brechas, GAPs o Ambigüedades Detectadas
- **GAP de Enmascaramiento LLM (Rendimiento):** El criterio de "PII Redaction" (CA-4) sugiere que un "LLM hallará secuencias". Invocar a un LLM sincrónicamente durante grandes peticiones GET a campos de texto libre introducira latencias monumentales (>1-5 segundos por registro). Se requiere limitar esta tarea a *Regex* determinísticos para grillas, o mover el Redactor LLM al proceso *inbound/guardado* asíncrono y no al *despacho/Frontend*.
- **GAP Zero-Trust (Seguridad PII en Base de Datos):** La historia detalla que "el string se despache... y oscurecerá... antes de rehidratar el Frontend". Si el dato PII no estructurado reposa íntegramente en claro en la BD, la capa Backend sigue siendo vulnerable en una fuga SQL o ataque directo DUMP.
- **Identificación de Superpoderes:** Se dicta que ocurre si es un "operario (Sin superpoderes)". No se declara el comportamiento para usuarios *con* superpoderes (¿Se deshabilita el interceptor devolviendo la TC en claro?).
- **Resolución UI del Código 409:** Al recibir 409, el FE avisa "Registro alterado recientemente". No se define el vector de resolución funcional (¿El FE recarga la página forzosamente haciendo perder el trabajo del Usuario B? ¿Hay una interfaz de Merge conflictiva?).

### 6. Lista de Exclusiones (Fuera de Alcance)
- Lógica de enmascaramiento estático criptográfico estructural a nivel base de datos (`AES-256` en las columnas en disco de MySql/Postgres); solo se dicta ofuscamiento en tránsito/vista (Frontend/API).
- Implementación de flujos de autenticación o RBAC base; esto se deriva a historias como US-036/038.
- Redirección de colisiones HTTP 409 para resolución técnica (Merge conflict management tipo GIT para formularios); la historia se limita a avisar y rechazar el guardado.

### 7. Observaciones de Alineación o Riesgos (Arquitectura)
> [!WARNING]
> La latencia transaccional sufrirá si delegamos a un LLM el "triage PII en caliente" al despachar peticiones GET para popular bandejas y workdesks. El enmascaramiento debe resolverse OBLIGATORIAMENTE por RegEx en el Backend de Lecturas rápidas, o delegarse a la inserción (Mutando los campos y versionando un "Texto Libre Ofuscado" vs "Texto Libre Rudo" desde el Worker de ingestión). Todo lo demás (Error 500/400/409) es una arquitectura web tradicional impecable.
