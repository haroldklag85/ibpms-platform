# Solicitud de Aprobación Arquitectónica: Infraestructura US-038

**Rol:** Agente Infra/BD
**Iteración:** 01-DEV-038-DAVID
**Rama:** DevDavid

**Arquitecto Líder:**
He completado el análisis y el modo PLANNING para la base de infraestructura solicitada en la US-038 (Sincronización EntraID y Multi-Rol). A continuación presento el resumen de hallazgos y el plan de inyección estructural:

## 1. Verificación Redis (CA-01)
Audité el archivo `docker-compose.yml`. El contenedor de Redis (`ibpms-redis`) está configurado adecuadamente con el puerto 6379, volumen persistente `redis_data` y `--appendonly yes`. El entorno es 100% capaz de soportar las pruebas de resiliencia de desconexión / reconexión (Fail-Open Policy). No requiero hacer ajustes aquí.

## 2. Inyección DDL (CA-03 y CA-04)
Para garantizar la Gobernanza de Identidades y el Break-Glass sin perder trazabilidad técnica, propongo ejecutar el siguiente esquema a través de Liquibase:

**Changeset Propuesto:** `48-us038-user-metadata.sql`
- `ALTER TABLE ibpms_security_user ADD COLUMN jit_claims_json JSONB;`: Requerido para almacenar todos los metadatos transitorios de EntraID sin romper el esquema estricto.
- `ALTER TABLE ibpms_security_audit_log ADD COLUMN is_break_glass BOOLEAN DEFAULT false NOT NULL;`: Bandera inmutable para disparar alertas de CISO en inicios de sesión de contingencia.
- `ALTER TABLE ibpms_security_audit_log ADD COLUMN justification TEXT;`: Requisito regulatorio para el Break-Glass.

## Solicitud Formal
Solicito autorización expresa para pasar a modo EXECUTION, crear los archivos mencionados y registrarlos en el `db.changelog-master.yaml` para su empuje.

Espero tu Veredicto Técnico.
