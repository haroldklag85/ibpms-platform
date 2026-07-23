# Handoff: AI DEVELOPER AGENT - BACKEND
**Iteración:** 64-DEV (US-001 / CA-16 al CA-18)
**Contexto de Memoria Aislada:** Backend Spring Boot. NO conoces Vue.

## 1. MISIÓN Y REGLA DE ORO V2
Tu misión es implementar matemáticamente los Criterios de Aceptación CA-16 al CA-18 de la US-001 en la capa Backend.
**REGLA DE ORO V2:** Analiza semánticamente el CA-16, CA-17 y CA-18. Si ALGUNO de ellos menciona capacidades de reportería avanzada, IA generativa a largo plazo, históricos masivos o analítica predictiva, considéralo "SCOPE V2" y EXCLÚYELO. Solo programa el núcleo de la versión 1.

## 2. NOTAS DE IMPLEMENTACIÓN TÉCNICA (TIN)
1. **Anti-DDoS Pagination (CA-10):** En el endpoint `@GetMapping("/workdesk")`, intercepta `Pageable`. Aplica `Math.min(request.limit, 100)`. Si detectas `size=50000`, arroja `HTTP 400 Bad Request` Inmediato para mitigar ataques.
2. **Search Optimizado (CA-10):** PROHIBIDO el uso de `ILIKE` crudo. Crea un índice Trigram en PostgreSQL (o ajusta el query): `CREATE INDEX idx_task_name_trgm ON tasks USING gin (name gin_trgm_ops)`.
3. **DTO Sanitization (CA-14):** Retorna en la API el objeto `WorkdeskGridDTO` mapeando EXCLUSIVAMENTE sus 5 columnas vitales. Sin PII ni metadatos brutos de Camunda.

## 3. ENTREGABLE ESTRICTO
Desarrolla el código, ejecuta pruebas JUnit y empaqueta obligatoriamente tu trabajo en git local usando exactamente:
`git stash save "temp-backend-US001-CA16-18"`
Notifica al humano cuando termines para que el Arquitecto Líder audite el diff.
