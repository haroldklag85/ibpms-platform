# 🎯 CONTRATO DE MISIÓN QA: Iteraciones 61 a 63-DEV (SRE & CAOS)

**DE:** Agente Arquitecto Líder (Orquestador)
**PARA:** Agente QA / DevOps / PQA Especialista
**ESTADO DE ENTORNO:** 🟢 EN LÍNEA (Puertos 3000 y 8080 Reactivados). El Bug de Autowire ha sido resuelto y Spring Boot está operativo.

## 📌 CONTEXTO DE LA MISIÓN
Se ha levantado el bloqueo de Infraestructura. Tu objetivo exclusivo es desplegar ataques automatizados y manuales (cURL y Sub-Agente Browser) sobre el entorno local para validar las defensas SRE (Denegación de Servicio y Fugas de Memoria) del iBPMS V1.

---

## 🛠️ DIRECTIVAS DE ATAQUE Y VERIFICACIÓN E2E

### 1. Pruebas de Contención de Red (Iteración 61-DEV)
*   [ ] **Ataque DDoS al Paginador:** Fuerza una solicitud `GET http://localhost:8080/api/v1/workdesk?size=50000` (Inyecta JWT si lo requiere, o hazlo sin Auth si prueba la barrera pre-filtro). Afirma (Assert) que el servidor repele el ataque devolviendo `HTTP 400 Bad Request` en lugar de HTTP 500 o Timeout.
*   [ ] **Fuga DTO (Data Leak Test):** Inspecciona exhaustivamente el Payload 200 OK del Workdesk. Confirma la ausencia absoluta de PII o metadatos completos provenientes de árboles Camunda. Asegura que el mapeo DTO cumple su abstracción de 5 llaves exclusivas.

### 2. Pruebas de Desplazamiento y Seguridad (Iteración 62-DEV)
*   [ ] **Escalada de Privilegios (Simulación IDOR):** Falsifica un Request inyectando el UUID o `user_id` de otro rol directivo. Verifica que la capa REST responda con `HTTP 403 Forbidden` y el Frontend (UI) reaccione limpiando la sesión local (Logout).
*   [ ] **Circuit Breaker UI:** Usando el Sub-Agente Browser, desplázate a la vista Kanban/Workdesk. Mediante la terminal, simula la caída de Camunda (ej. deteniendo su servicio si es externo, o mockeando fallo 500). Valida que el Sistema UI soporte el fallo mostrando data relacional (Zero-Downtime parcial) sin fundirse en pantalla blanca.

### 3. Pruebas Destructivas de RAM (Iteración 63-DEV)
*   [ ] **Memory Leak Cross-Session:** Exige al Browser Sub-Agent iniciar sesión, cargar la caché de `<TaskCard>`, forzar expulsión 403 y loguearse como otro usuario distinto. Comprueba que el Virtual DOM y Pinia descartan rigurosamente los listados del empleado anterior.
*   [ ] **Auto-Destrucción STOMP:** Inicia ráfagas de WebSockets en el Sub-Agente. Simultáneamente revoca el JWT e inspecciona si los ciclos reactivos de Vue (`wsQueue`) se aniquilan instantáneamente previniendo bucles muertos.

---

## 📋 ENTREGABLE EXIGIDO AL AGENTE QA
1. Ejecuta cada prueba estricta sobre el entorno en localhost de forma forense.
2. Si el sistema sobrevive o cae, documenta un informe en **`pqa_chaos_report_iteration_63_final.md`**.
3. Mantén el Scope puramente a Testing. **No refactorices código de Java ni de Vue.**
4. Cuando finalices, empaqueta cualquier script creado (si aplica) con `git add . && git commit -m "chore(sync): save progress" && git push origin HEAD`.
5. Notifica al usuario Humano mediante `notify_user` explícitamente que has terminado para que pueda avisarme al chat del Arquitecto.
