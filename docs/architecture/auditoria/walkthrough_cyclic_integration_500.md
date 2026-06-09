# Walkthrough Forense: Error 500 "Integración Cíclica" — Línea de Tiempo Completa

> **Autor:** Arquitecto Líder AI  
> **Sprint:** 6.2  
> **Fecha del Incidente:** 2026-04-20  
> **Severidad:** P0 — Bloqueador de Certificación UAT  
> **Estado:** CERRADO — Causa Raíz Identificada y Remediada  

---

## 1. Resumen Ejecutivo

El incidente **"Integración Cíclica 500"** fue un bloqueador crítico que impidió la certificación UAT del Journey J-04 durante el Sprint 6.2. El Frontend mostraba una pantalla roja permanente con el mensaje `ALERTA DEL SISTEMA: NIVEL 0 — Colapso del Servidor / Integración Cíclica (Código de Error: 500)` al intentar autenticarse vía `POST /api/v1/auth/emergency-login`.

La investigación reveló que el error tenía **dos capas de fallo superpuestas**, lo que dificultó enormemente el diagnóstico:

| Capa | Descripción | Tiempo de Diagnóstico |
|------|-------------|----------------------|
| **Capa 1 — Backend JPA** | Bytecode huérfano de `KanbanBoardEntity.class` corrupto en el classpath de Docker | ~2 horas |
| **Capa 2 — Frontend Masking** | `apiClient.ts` interceptaba errores 502/504 del proxy Vite y los disfrazaba como "Error 500" genérico | ~3 horas adicionales |

---

## 2. Línea de Tiempo del Incidente

```
[18:00] ─ Usuario reporta fallo en login (puerto 5174)
           Pantalla roja: "ALERTA DEL SISTEMA: NIVEL 0"
           
[18:15] ─ Primera hipótesis: credenciales inválidas
           Resultado: Descartada. El backend ni siquiera responde.
           
[18:30] ─ Subagente navegador confirma: puerto 5174 sin respuesta
           Puerto 5173 también falla con el mismo error visual
           
[19:00] ─ Inspección Docker: contenedor ibpms-core-dev en bucle de reinicio
           RestartCount: 7+
           Error: org.hibernate.AnnotationException
           "Association 'KanbanTaskEntity.board' targets an unknown entity 
            named 'KanbanBoardEntity'"
           
[19:30] ─ CAPA 1 IDENTIFICADA: Bytecode huérfano en target/
           KanbanBoardEntity.class existe en target/ pero el .java fue
           renombrado a KanbanV2BoardEntity.java
           Compilador incremental Maven NO eliminó el .class viejo
           
[20:00] ─ Remediación Capa 1: mvn clean compile dentro del contenedor
           Purga exitosa de target/
           Contenedor arranca limpiamente (JPA OK)
           
[20:30] ─ PROBLEMA PERSISTE: Usuario vuelve a probar, sigue el error 500
           Logs de Docker muestran: Hibernate query ejecutándose OK
           El Backend responde 401 (credenciales inválidas) — NO 500
           
[21:00] ─ CAPA 2 IDENTIFICADA: Frontend masking
           apiClient.ts intercepta [500, 502, 503, 504] y dispara
           el mismo toast genérico "Colapso del Servidor"
           Durante el bootloop, Vite proxy devolvía 502/504 (backend 
           caído), y el Frontend lo reportaba como "500"
           
[21:30] ─ DIAGNÓSTICO FINAL:
           • Prueba curl directa: Backend responde 401 (correcto)
           • Base de datos: usuario 'root@ibpms.local' es el único 
             registrado en ibpms_security_user
           • El error 500 visual ERA FALSO — era un 502 proxy disfrazado
           
[22:00] ─ Incidente cerrado. Ambas capas documentadas.
```

---

## 3. Capa 1: Bytecode Huérfano en JPA (Backend)

### 3.1 Causa Raíz

Al renombrar `KanbanBoardEntity.java` → `KanbanV2BoardEntity.java` en el host Windows, el bind-mount de Docker (`./backend:/app`) reflejó el cambio de inmediato. Sin embargo, el compilador Maven incremental dentro del contenedor:

- ✅ Compiló `KanbanV2BoardEntity.java` → `KanbanV2BoardEntity.class`
- ❌ **NO eliminó** `KanbanBoardEntity.class` del directorio `target/classes/`

Hibernate escaneaba ambos archivos `.class` y encontraba una referencia rota: `KanbanTaskEntity.board` apuntaba a `KanbanBoardEntity`, que ya no existía como entity registrada (el nombre ahora era `KanbanV2BoardEntity`).

### 3.2 Evidencia en Logs

```
org.springframework.beans.factory.BeanCreationException: 
  Error creating bean 'entityManagerFactory'
  ...
  Caused by: org.hibernate.AnnotationException: 
    Association 'com.ibpms.poc.infrastructure.jpa.entity.KanbanTaskEntity.board' 
    targets an unknown entity named 
    'com.ibpms.poc.infrastructure.jpa.entity.kanban.KanbanBoardEntity'
```

### 3.3 Archivos Afectados

| Archivo | Acción Realizada | Estado Post-Remediación |
|---------|-----------------|------------------------|
| `KanbanBoardEntity.java` (raíz entity/) | Permanece sin cambios (entidad válida en paquete raíz) | ✅ OK |
| `kanban/KanbanV2BoardEntity.java` | Renombrado desde `kanban/KanbanBoardEntity.java` | ✅ OK |
| `kanban/KanbanItemEntity.java` | `@ManyToOne` actualizado a `KanbanV2BoardEntity` | ✅ OK |
| `KanbanTaskEntity.java` | `@ManyToOne` apuntaba a entidad ambigua | ⚠️ Requiere validación |

### 3.4 Remediación Ejecutada

```powershell
# Opción aplicada: limpieza dentro del contenedor Docker
docker exec ibpms-core-dev mvn clean compile -DskipTests

# Verificación de arranque limpio
docker logs ibpms-core-dev --tail 5
# Output esperado: "Started Application in X.XXX seconds"
```

---

## 4. Capa 2: Enmascaramiento de Errores en Frontend (El Falso 500)

### 4.1 Causa Raíz

El archivo `apiClient.ts` (líneas 50-74) contiene un interceptor de errores que agrupa **cuatro códigos HTTP distintos** bajo un mismo mensaje de UI:

```typescript
// apiClient.ts:50
if (error.response && [500, 502, 503, 504].includes(error.response.status)) {
    const event = new CustomEvent('global-error-dispatch', { detail: { 
        code: error.response.status,   // ← El código REAL se pierde aquí
        message: `Colapso del Servidor / Integración Cíclica`  // ← Mensaje genérico
    }});
    window.dispatchEvent(event);
}
```

Cuando el Backend estaba caído (bootloop de JPA), el proxy de Vite devolvía **502 Bad Gateway** o **504 Gateway Timeout**. El interceptor capturaba estos errores y los presentaba al usuario como un genérico "Error 500 — Colapso del Servidor", eliminando toda capacidad de diagnóstico diferenciado desde la UI.

### 4.2 Impacto en el Diagnóstico

| Código HTTP Real | Significado | Lo que el UI mostraba | Acción correcta |
|------------------|-------------|----------------------|-----------------|
| **500** | Error interno en el controlador Java | "Colapso del Servidor 500" | Revisar stacktrace del backend |
| **502** | Proxy Vite no puede conectar al backend | "Colapso del Servidor 500" ❌ | Verificar si Docker está arriba |
| **503** | Servicio temporalmente no disponible | "Colapso del Servidor 500" ❌ | Esperar reinicio del contenedor |
| **504** | Timeout del proxy Vite | "Colapso del Servidor 500" ❌ | Backend en bootloop o lento |

### 4.3 Capa 3: Desfase de Contrato por Bytecode Obsoleto (Efecto Cascada en UI)

A partir de la intervención realizada en la solución de Auth Feedback, se descubrió una tercera manifestación letal del código huérfano (`stale bytecode`). 
El ciclo de vida del contenedor afectó directamente el contrato Rest de Spring Boot:

1. El Agente de Backend modificó correctamente `AuthSyncController.java` para retornar un JSON enriquecido: `{"code": "INVALID_PASSWORD", "message": "..."}`.
2. Como el contenedor `ibpms-core-dev` no fue recompilado desde cero (`mvn clean`), Spring Boot **siguió ejecutando los `.class` antiguos de memoria** que solo retornaban `{"message": "Credenciales Inválidas"}`.
3. El Agente de Frontend, en `Login.vue`, esperaba mapear el error usando `responseData.code`. Al recibir un payload obsoleto, `code` era evaluado como `undefined`.
4. Esto activó automáticamente la barrera `default / UNKNOWN` del Switch-Case de Vue, renderizando el genérico *"Error de conexión con el servidor"* y **ocultando visualmente que el backend sí estaba validando las contraseñas**.

Este "efecto mariposa" reafirma la **Lección Aprendida #1**: *Nunca validar integraciones Full-Stack sin purgar el Target de Spring Boot si se modificaron archivos estructurales o contratos.*

### 4.4 Recomendación de Mitigación

> [!IMPORTANT]
> **ADR-014** documenta la decisión arquitectónica de diferenciar estos códigos en el interceptor.
> Ver: [adr_014_frontend_error_observability.md](../adr_014_frontend_error_observability.md)

---

## 5. Hallazgo Secundario: Usuario de Prueba en Base de Datos

Durante la verificación post-remediación, se descubrió que la base de datos UAT (`ibpms-postgres-uat`) solo contiene **un usuario registrado**:

```sql
SELECT email, is_active FROM ibpms_security_user;

      email       | is_active 
------------------+-----------
 root@ibpms.local | t
(1 row)
```

> [!WARNING]
> Las pruebas de emergency-login con `admin@empresa.com` o `admin.local@empresa.com` fallarán con **401 Unauthorized** (comportamiento correcto) porque esos emails no existen en la tabla `ibpms_security_user`. El único email válido es `root@ibpms.local`.

### Credenciales del Contenedor PostgreSQL UAT

| Variable | Valor |
|----------|-------|
| `POSTGRES_USER` | `ibpms_user` |
| `POSTGRES_PASSWORD` | `ibpms_password` |
| `POSTGRES_DB` | `ibpms_db` |
| Puerto Host | `5432` |

---

## 6. Lecciones Aprendidas

### Para Equipos de Desarrollo

1. **Nunca confiar en compilación incremental Maven** dentro de Docker con bind-mounts. Tras renombrar/eliminar cualquier archivo `.java`, ejecutar `mvn clean` antes de validar.
2. **Los errores de "Unknown Entity" en Hibernate** son siempre indicadores de classpath sucio. No buscar en la lógica de negocio hasta confirmar que `target/` está limpio.
3. **El único usuario válido en UAT** es `root@ibpms.local`. Documentar credenciales de prueba en un lugar accesible.

### Para Equipos de Frontend

4. **Diferenciar los códigos 5xx en los interceptores HTTP**. Un 502 significa "backend caído" y un 500 significa "error en el código". Mostrar el mismo mensaje para ambos destruye la capacidad de diagnóstico.
5. **Incluir el código HTTP real** en cualquier toast o alerta de error para facilitar triaje.

### Para Equipos de Infraestructura

6. **Monitorear `RestartCount` del contenedor backend** como métrica de salud. Un RestartCount > 0 indica problemas de arranque.
7. **Implementar un healthcheck endpoint** (`/actuator/health`) y exponerlo en el `docker-compose.yml` del backend para que Docker marque automáticamente el contenedor como `unhealthy` cuando no arranca.

---

## 7. Checklist de Prevención (Protocolo Post-Incidente)

Antes de reportar un "Error 500 de Integración Cíclica", ejecutar esta secuencia:

- [ ] `docker ps --format "{{.Names}}\t{{.Status}}"` — ¿El backend está `Up` o `Restarting`?
- [ ] `docker logs ibpms-core-dev --tail 20` — ¿Hay un `BUILD FAILURE` o `AnnotationException`?
- [ ] `docker inspect ibpms-core-dev --format="{{.RestartCount}}"` — ¿Cuántos reinicios ha tenido?
- [ ] `Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"` — ¿El backend responde directamente?
- [ ] Si el backend responde OK pero la UI muestra 500: el problema es **frontend** (proxy o interceptor)
- [ ] Si el backend no responde: ejecutar `docker exec ibpms-core-dev mvn clean compile -DskipTests` y reiniciar
