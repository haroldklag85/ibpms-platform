# Handoff Backend - Iteración 34 (US-028: CA-10 a CA-11)

## Propósito
Retornar al campo de batalla para habilitar el endpoint de Certificación. Cuando el QA Humano constata que el Sandbox Frontend está verde, empujará un Payload validado para lograr un Sellado Criptográfico (ISO 27001) del esquema Zod.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-11 (Sello Criptográfico):** 
  - Exponer endpoint `POST /api/v1/design/forms/{id}/certify`.
  - Recibir el String crudo del `ZodSchema` final y el `DummyPayload` usado para auditarlo.
  - Computar el hash **SHA-256** del esquema Zod.
  - Almacenar el Hash y bandera de certificación en la tabla `Form_Definitions` (o entidad correspondiente).
  - Insertar registro obligatorio en `Audit_Logs` (Timestamp, Usuario QA, Payload Dummy, Hash) para garantizar trazabilidad.

## Directrices V1 
El algoritmo Hash debe ser determinista (Nativo de Java `MessageDigest` o utilería de Spring/Apache Commons). 

## Tareas Java
1. Crear el DTO `FormCertificationRequest` (schema, dummyPayload, qaUserId).
2. Crear la lógica en `FormDesignerController` o similar.
3. Actualizar la capa de Servicio y Repositorio para inyectar la firma SHA-256 y crear el registro de auditoría.
