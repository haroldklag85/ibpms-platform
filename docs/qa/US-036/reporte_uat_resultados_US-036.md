# 📋 Reporte Oficial de Resultados UAT y Handoff de Remediación — US-036

> **Generado por:** 🧪 UAT-GUIDE LEAD (QA Tester)
> **Destinatario:** Arquitecto Líder y Enjambre de Desarrollo (DEV Agents)
> **Fecha del Reporte:** 2026-05-21
> **Historia de Usuario:** US-036 (RBAC, Zero-Trust y Gobernanza de Seguridad - ISO 27001)

---

## 1. 📊 Resumen Ejecutivo

Tras la ejecución humana de la **Guía de Pruebas UAT v1.0**, se han consolidado los siguientes resultados:

- **Total de Pruebas Ejecutadas:** 10
- **Tasa de Éxito (PASS):** 50% (5 pruebas)
- **Tasa de Fallo (FAIL):** 20% (2 pruebas)
- **Pruebas Bloqueadas (BLOQUEADO):** 30% (3 pruebas)
- **Veredicto General:** ❌ **RECHAZADA PARA PASE A PRODUCCIÓN**. Se requiere remediación inmediata por parte del equipo de Desarrollo.

---

## 2. ✅ Certificación Exitosa (PASS)

Las siguientes pruebas superaron la validación humana y los criterios de aceptación asociados se consideran estabilizados:

- **Prueba 1 (PASS):** Protección de Roles Nativos e Inmutabilidad.
- **Prueba 2 (PASS):** Herencia Piramidal y Desactivación Suave (Soft-Delete).
- **Prueba 3 (PASS):** Privacidad Visual, Topología Dinámica y Anti-JWT Bloat.
- **Prueba 4 (PASS):** Botón Táctico de Exorcismo (Kill-Session y Auto-Curación Zero-Trust).
- **Prueba 8 (PASS):** Bypass Anónimo de Procesos Públicos (Bloqueo efectivo y redirección a login).
- **Prueba 9 (PASS):** Experiencia de Caída Segura / UX Fallback (Captura correcta y renderizado de `ALERTA DEL SISTEMA: NIVEL 0`).

---

## 3. 🚨 Hallazgos, Defectos y Brechas (Hand-off a Desarrollo)

Para que el **Arquitecto Líder** planifique los esfuerzos de desarrollo, detallo a continuación las deficiencias encontradas con su diagnóstico a alto nivel:

### ❌ DEFECTO 1: Brecha de Funcionalidad en Clonación de Perfiles
- **Prueba:** 10 (Clonación de Perfiles por Plantilla - CA-03)
- **Estado:** `FAIL`
- **Observación Humana:** En la ficha del usuario no se tiene la opción "Asignar desde Plantilla / Clonar Roles", esto impide realizar la prueba.
- **Diagnóstico QA:** A pesar de que el Coverage Matrix reporta que el backend (`assignTemplateToUsers()`) está funcional, la interfaz de usuario en el Frontend no expone esta funcionalidad.
- **Acción Requerida para Frontend DEV:** Implementar el botón/opción "Clonar Roles" en la vista de edición de usuarios y enlazarlo con el endpoint correspondiente del backend.

### ❌ DEFECTO 2: Ruptura en Tablero de Auditoría ISO 27001
- **Prueba:** 6 (Informes Densos de Fiscalización y Traza Indeleble - CA-16, CA-17, CA-24)
- **Estado:** `FAIL`
- **Observación Humana:** La pestaña de ISO 27001 no está trayendo ninguna información del log de actividades.
- **Diagnóstico QA:** Se detecta una ruptura en la cadena de visibilidad. Puede ser un fallo en la persistencia de los eventos en BD, un error en el endpoint del Backend al retornar el JSON, o un fallo en el renderizado de la tabla en Frontend.
- **Acción Requerida para Arquitecto/DEV:** 
  1. Verificar si la tabla `ibpms_audit_reports` de base de datos tiene datos reales.
  2. Verificar si el endpoint `GET` de reportes está devolviendo un Array vacío o dando error 500.
  3. Revisar el componente Frontend para asegurar la correcta renderización.

### ⚠️ BLOQUEO 1: Deficiencia Severa UX y Falla Estructural en Delegación
- **Prueba:** 5 (Módulo de Delegación Autónoma y Exorcismo - CA-09, CA-23)
- **Estado:** `BLOQUEADO / BRECHA`
- **Observación Humana:** No se explica en la UI dónde asignar tareas para heredar. Adicionalmente, NO se heredan permisos ni accesos (al ingresar el "Analista 2", no podía ver nada ni tenía acceso a la información delegada).
- **Diagnóstico QA:** Hay un doble fallo:
  1. **Fallo UX:** La interfaz no deja claro el flujo de delegación.
  2. **Fallo Backend/Security (Grave):** La lógica de delegación (`/api/v1/security/delegations`) no está inyectando dinámicamente los roles o la Row-Level Security (RLS) al usuario delegado. El usuario destino está recibiendo tareas que el sistema no le permite visualizar (violación cruzada de CA-05 vs CA-09).
- **Acción Requerida para Arquitecto/DEV:** Rediseñar la inyección de permisos en tiempo de ejecución para el delegado (Unión Matemática Multirrol temporal) y mejorar el Frontend de Delegaciones.

### ⚠️ BLOQUEO 2: Imposibilidad Empírica de probar Service Accounts
- **Prueba:** 7 (Robots de Integración y API Keys - CA-10, CA-22)
- **Estado:** `BLOQUEADO`
- **Observación Humana:** Se generó la API Key exitosamente (`gtFj2LHZ846ocUtHFqgSH3bp9kOtQmM56clv6Fu8lIc`), pero al no disponer de Postman o herramientas de API en el entorno UAT, el humano no pudo validar su funcionamiento.
- **Diagnóstico QA:** La funcionalidad de generación opera correctamente. La prueba no es de interfaz gráfica sino de interoperabilidad de red (Machine-to-Machine).
- **Acción Requerida para QA DevOps:** Convertir esta prueba UAT manual en una prueba automatizada (Ej. Playwright API Request, Vitest, o RestAssured en Backend) que certifique que el `ApiKeyAuthFilter` responde HTTP 200 al usar la cabecera `X-API-Key`.

---

## 4. 🧭 Directriz de Enrutamiento (Siguiente Paso)

**Atención @Arquitecto Líder:**
Con base en este reporte, se solicita orquestar la **Ronda de Remediación Quirúrgica** invocando a los especialistas de Backend y Frontend para abordar los 2 Defectos y resolver el Bloqueo 1. El Bloqueo 2 debe enviarse a la pila de automatización de QA E2E.
