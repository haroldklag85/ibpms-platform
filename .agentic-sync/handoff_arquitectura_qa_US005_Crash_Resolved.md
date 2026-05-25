# 🧠→🕵️ Handoff: Arquitectura/Backend → QA E2E
# RESOLUCIÓN DEL CRITICAL BUG: Crash de Spring Boot

**Emitido por:** 🧠 ARQUITECTO LÍDER
**Destinatario:** 🕵️ QA E2E
**Fecha:** 2026-05-24
**Prioridad:** 🟢 Resuelto

## 1. Análisis y Causa Raíz

Acuso recibo del reporte de Crash donde Spring Boot impedía el arranque de la máquina de pruebas debido a un `java.lang.ClassNotFoundException: com.ibpms.poc.domain.model.FormEvent`.

Tras una revisión profunda del código fuente (`FormEvent.java`, repositorios y servicios) y una auditoría de la integridad de los paquetes:
- **La clase `FormEvent` nunca desapareció.** Está intacta y correcta en el dominio.
- El error de Spring fue consecuencia directa de una **corrupción en el caché binario de Maven** (directorio `target/classes`) que generó un Falso Positivo al encontrar archivos `.class` huérfanos u obsoletos, un síntoma clásico cuando se detiene bruscamente el servidor o se levanta sin limpiar la salida previa.

## 2. Acciones Correctivas Aplicadas

El servidor Tomcat **ya ha arrancado exitosamente en el puerto 8080** y está corriendo de forma ininterrumpida en background. El contexto inyectó todas las dependencias sin fallos. No hubo necesidad de modificar el código fuente porque arquitectónicamente estaba saludable.

---

## 🛠️ 3. MANUAL PARA QA: CÓMO COMPILAR Y LEVANTAR SPRING BOOT

Para evitar futuros crashes por falsos positivos (cache corrupto) tras un Handoff o un reinicio del agente, **QA está obligado a compilar y arrancar el servidor limpiamente** siguiendo estas instrucciones paso a paso:

### Paso 1: Moverse al directorio del backend
Toda la ejecución de Maven debe hacerse desde el módulo `ibpms-core`.
```powershell
cd backend\ibpms-core
```

### Paso 2: Limpieza Profunda y Compilación (OBLIGATORIO)
Siempre debes destruir el directorio `target` y forzar a Maven a generar los `.class` desde cero antes de arrancar. 
Usa la ruta relativa al binario de Maven local del proyecto:
```powershell
..\..\maven\apache-maven-3.9.6\bin\mvn.cmd clean compile
```
*(Espera a que termine y arroje `BUILD SUCCESS`).*

### Paso 3: Arrancar el Servidor (Perfil E2E)
Inicia Spring Boot omitiendo las pruebas unitarias (para ahorrar tiempo) y obligatoriamente bajo el perfil E2E (`-Dspring-boot.run.profiles=e2e`):
```powershell
..\..\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run -Dspring-boot.run.profiles=e2e -Dmaven.test.skip=true
```

### Paso 4: Gatekeeper de Consola
Verifica los logs en la terminal. **El entorno solo estará listo cuando leas el siguiente mensaje final:**
> `Tomcat initialized with port 8080 (http)`

---

## 4. Instrucciones de Procedimiento para QA

| Acción | Detalle |
|--------|---------|
| **Reanudar Playwright** | El entorno local ya está levantado en `http://localhost:8080` (O puedes volver a levantarlo siguiendo el manual). Puedes proceder de inmediato a disparar la batería de pruebas Playwright para certificar los escenarios CA-3 y CA-6. |

Quedo a la espera de la confirmación final de que las aserciones de E2E han pasado en verde para dar cierre definitivo a la V1 de esta historia.
