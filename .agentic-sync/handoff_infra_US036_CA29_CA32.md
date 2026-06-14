# 📄 Handoff de Arquitectura: Infraestructura / Base de Datos
> **US:** US-036 | **CAs:** CA-29 al CA-32 | **Iteración:** 08-DEV-DAVID

## 1. Metadatos de la Delegación
- **Rol Destino:** Agente Infra/BD
- **Objetivo:** Verificar la disponibilidad de Redis para Caché Híbrida (CA-32).
- **Alineación Arquitectónica:**
  - Se requiere validar que el servicio de Redis esté correctamente configurado en `docker-compose.yml` para soportar las anotaciones `@Cacheable` de Spring Boot.

## 2. Contexto de Negocio
Para evitar la saturación de red (Anti-JWT Bloat) y optimizar los tiempos de carga del frontend, se ha decidido que el layout del menú se calculará dinámicamente y se cacheará. El backend requiere Redis para guardar este cálculo temporalmente.

## 3. Criterios de Aceptación
- **CA-32 (Caché Híbrida):** El contenedor de Redis (Puerto 6379) debe estar vivo y accesible por el backend para almacenar el menú cacheado del usuario.

## 4. Directrices Técnicas y Arquitectónicas
- No se requieren nuevos scripts de Liquibase para estas CAs.
- Verifica que el entorno Docker tenga Redis configurado y expuesto. Si ya está, tu trabajo aquí es confirmar su operatividad.
- **Validación de esquema obligatoria:** Ejecuta verificaciones de sintaxis Liquibase o configuraciones docker-compose antes de hacer push.

## 5. Estructura de Archivos Esperada
- `docker-compose.yml` (Solo si es necesario agregar Redis, aunque debería estar según ADRs).

## 6. Instrucciones Operativas y de Comunicación
> ⚠️ **IMPORTANTE:** Todo desarrollo o configuración debe quedar exhaustivamente documentado. Se exige **PRECISIÓN QUIRÚRGICA** en cada cambio para evitar efectos colaterales en funcionalidades existentes. Se han detectado regresiones en iteraciones previas; cualquier daño a funcionalidades adyacentes será motivo de rechazo inmediato.

> **INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN:**
> 1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
> 2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
> 3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_infra.md`.
> 4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_infra.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
> 5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar git stash.

> 📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
> - Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
> - Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md` (naming, error handling, modularidad).
