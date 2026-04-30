# Handoff: AI QA / DEVOPS AGENT
**Iteración:** 66-DEV (US-002 / CA-6 al CA-10)
**Contexto Aislado:** Validación End-to-End, Testing, Seguridad, AppSec y SRE.

## 1. MISIÓN
Auditar la robustez del Flujo inverso del Motor de Reclamos (Unclaim / Data Purge) asegurando ausencia de fugas y deudas.

## 2. MATRIZ DE CERTIFICACIÓN OBLIGATORIA (QA)
1. **Audit V2 Exclusión:** Revisa si en los PRs de backend/frontend hay variables sugestivas de analítica futura no contemplada en V1.
2. **Aserción BOLA / IDOR (CA-06):** Inyecta peticiones POST `/api/tasks/{id}/unclaim` iterando sobre tareas robadas a otro Tenant. Confirma taxativamente el retorno `HTTP 403 / AccessDenied`.
3. **Data Purge AWS S3 Mock (CA-07):** Aserciona los logs de Tomcat. Confirma que existe una entrada `@Async` invocando borrado en nube de uploads huérfanos antes del cierre de sesión de la tarea.
4. **Resiliencia DOM (CA-08):** Asegura visual o estáticamente en Vue que la transición `.list-leave-to` está cableada nativamente antes de borrar objetos del array local.

## 3. ENTREGABLE ESTRICTO
Genera reporte GO/NO-GO de SRE. Si no hay regresión, aprueba.
`git stash save "temp-qa-US002-CA6-10"`
