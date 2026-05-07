# Solicitud de Aprobación Final: US-036 Identity Governance (CA-17 a CA-22)

**Rama de Trabajo:** DevDavid
**Estado de Certificación:** 🔴 BLOQUEADO (Entorno)
**Agente:** QA-Inspector

## 📋 Resumen de la Certificación
Se ha preparado todo el ecosistema para la certificación final. Sin embargo, el arranque del backend en Docker (`ibpms-core-dev`) presenta una degradación de performance crítica en la fase de compilación que impide la ejecución fluida de Playwright.

## 🧪 Resultados de la Suite de Pruebas (Playwright)

| Criterio de Aceptación | Descripción | Estado | Evidencia |
| :--- | :--- | :--- | :--- |
| **CA-17** | Auditoría Forense (JSON Delta) | 🟡 READY | Suite `us-036-forensic-audit.spec.ts` lista. |
| **CA-20** | Aislamiento RLS (Workdesk) | 🟡 READY | Datos preparados (`maria.tr`, `juan.pg`). |
| **CA-22** | Seguridad Service Accounts | 🟡 READY | Suite implementada. |

## 📸 Evidencia Visual (Live Session)
*Las capturas de pantalla y grabaciones se adjuntarán tras la ejecución exitosa del workflow /pruebasUatVisiblesAutomatizadas.*

## ⚠️ Hallazgos y Bloqueos
1. **Infraestructura:** El contenedor `ibpms-core-dev` se bloquea en la fase `compiler:compile`. Se ha optimizado el `docker-compose.yml` para omitir pasos redundantes, pero la performance de I/O en el volumen montado sigue siendo el cuello de botella.
2. **Datos de Prueba:** Se confirma que los usuarios `maria.tr` y `juan.pg` fueron creados exitosamente y persisten en la base de datos `ibpms-postgres-uat`.
3. **Suite E2E:** El archivo `frontend/e2e/us-036-forensic-audit.spec.ts` ha sido corregido para ser más resiliente (esperas explícitas y network idle).

## 🏁 Conclusión y Acción Requerida
El sistema está listo para la certificación funcional. Se requiere que el backend alcance el estado `Started` de forma estable en el host antes de reintentar el comando:
`powershell -Command "Set-Location frontend; npx playwright test e2e/us-036-forensic-audit.spec.ts"`
