---
title: "Handoff Frontend - US-038 (CA-06 al CA-12)"
role: "Frontend"
epic: "US-038 - Asignación Multi-Rol y Sincronización EntraID"
iteration: "02-DEV-038-DAVID"
branch: "DevDavid"
---

# Handoff Arquitectónico: Frontend

## 1. Contexto y Objetivos
El objetivo de esta iteración es proveer a la interfaz visual los elementos de trazabilidad de los roles de los usuarios (Badges y Chips en la cabecera), así como proveer las vistas del Tablero de Anomalías (para resolución de alarmas de SoD) y la UI para la Delegación Jerárquica con Fechas en el Workdesk.

**Exclusiones:** El CA-09 ("Distributed Tracing V2 Ready") ha sido EXCLUIDO por referenciar explícitamente a arquitecturas V2.

## 2. Alineación Arquitectónica
* **ADR-002 (Vue3 Microfrontends):** Todo manejo de estado (como la delegación temporal o consolidación de tareas multi-rol) DEBE ir en Pinia. Los consumos de la API de anomalías y delegación DEBEN pasar por `apiClient.ts` o usar los stores correspondientes de Axios.
* **Componentización:** Se espera la reutilización o creación de componentes pequeños para los Badges (ej. `RoleBadge.vue`) en el Workdesk y la Cabecera.

## 3. Requerimientos Técnicos (Entregables)

### 3.1 Delegación Temporal (CA-07)
* Ampliar la Pantalla 14 (o el modal de Perfil) para que un usuario pueda delegar temporalmente su perfil seleccionando un usuario de destino (`delegate_id`), una `Fecha_Inicio` y una `Fecha_Fin`. Enviar estos datos al backend vía POST `/api/v1/security/delegations`.

### 3.2 Consolidación Visual y Badge de Procedencia (CA-10)
* En la grilla del Workdesk, asegurarse de que las tareas estén listadas unificadas sin necesidad de saltos de sesión.
* Inyectar visualmente un Badge discreto (ej. `Rol: Aprobador_Nivel_2`) en la fila correspondiente al contexto funcional de la tarea, indicándole al usuario bajo qué prerrogativa actúa.

### 3.3 Indicador de Dominio en Cabecera (CA-11)
* Modificar el `Master Header` (o `Layout` principal) para renderizar un micro-texto o chip visible resumiendo los roles operativos asignados (Ej: `Director Comercial | Aprobador VIP`), decodificando el state de Pinia o el token JWT unificado.

### 3.4 Tablero de Anomalías de Seguridad (CA-12)
* Dentro del Módulo de Seguridad/RBAC (Pantalla 14), crear una nueva pestaña (Tab) denominada "Tablero de Anomalías".
* Consumir el endpoint `GET /api/v1/security/anomalies` y mostrar un listado (en color rojo/alertas) de las incidencias (Ej: "SoD Detectado").
* Implementar un botón "Marcar como Subsanado" que haga `PUT /api/v1/security/anomalies/{id}/resolve`.

## 4. Criterios de Aceptación a Soportar
* **CA-07:** Interfaz de usuario para delegación temporal de rol.
* **CA-10 y CA-11:** UX/UI de roles unificados en listados y cabeceras.
* **CA-12:** UI del Tablero de Anomalías operando 100% sobre API.

## 5. Exclusiones
* **CA-06 y CA-08:** Son 100% lógicas de backend, Frontend solo reacciona con mensajes de error si la API retorna HTTP 403 / 409 por Violación SoD.
* **CA-09:** Excluido.

> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **Build obligatorio:** Ejecuta el protocolo Zero-Trust UI documentado en `.agents/skills/frontend_build_audit/SKILL.md`.

---
**INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_frontend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_frontend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.
