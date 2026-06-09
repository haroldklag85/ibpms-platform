# Handoff Backend: Optimización de Concurrencia (J-04)

**Objetivo:** Habilitar L2 Caching en los endpoints de lectura masiva del Workdesk (DataGrid) para mitigar los timeouts causados por el asedio concurrente del entorno E2E.

**Instrucciones Arquitectónicas:**
1. **Caching Layer:** Habilitar `@EnableCaching` en la configuración de la aplicación si no está presente.
2. **ListarTareasService:** Anotar el método `listar` (o sus homólogos fuertemente consultados) con `@Cacheable("workdesk_tasks")`. Asegurarse de que las llaves incluyan los filtros aplicables (limit, offset, status, priority, delegatedUser).
3. **Cache Eviction:** Definir un mecanismo (TTL corto o @CacheEvict) al crear, skipear o delegar tareas, para que la UI no quede eternamente desincronizada.
4. **Infraestructura de Datos:** Asegurar que `application-dev.yml` / `application-e2e.yml` establezca un pool Hikari razonable (`maximum-pool-size: 50`) para soportar los hilos asíncronos concurrentes.

**Alineación Arquitectónica:**
- Se respeta ADR-001 (Hexagonal Architecture).
- Implementación de Caché validada y aprobada por la Jefatura para estabilizar la prueba.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
