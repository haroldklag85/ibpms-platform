# ADR-005: Estrategia de Almacenamiento de Archivos Binarios (Attachments)

**Status:** Aprobado
**Date:** 2026-03-07
**Context:** US-039 (Formulario Genérico y Adjuntos)
**Autor:** Lead Software Architect

## Contexto y Problema
Actualmente, la plataforma iBPMS (V1/V2) exige la recolección de documentos técnicos, contratos o facturas como anexos de un Formulario de Tarea (Pantalla 7.B) o como partes integrales de un Expediente (Case Management).
El Product Owner ha consultado sobre la factibilidad técnica y "mejores prácticas" para el almacenamiento de estos archivos (`multipart/form-data`). Específicamente, cuestiona si debe crearse una Base de Datos exclusiva en nuestro clúster (Ej: MongoDB GridFS o persistir Blobs en PostgreSQL/MySQL).

## Decisión de Arquitectura: PROHIBICIÓN de Persistencia en Bases de Datos
Se establece como regla inquebrantable de arquitectura **NO almacenar bytes binarios (`BLOB/BYTEA`) directamente dentro de los esquemas transaccionales ni NoSQL de la empresa**, prohibiendo el diseño de un "MongoDB File Server".

Los binarios degradan exponencialmente el *Buffer Pool* y la RAM de cualquier motor de base de datos. Causan obesidades inmanejables (Terabytes en semanas), encarecen absurdamente los Backups lógicos/Snapshot, ralentizan las migraciones y monopolizan el I/O del disco duro impidiendo que el motor Camunda ejecute el BPMN eficientemente.

### Solución Tecnológica Mandataria (V1 Táctica y V2 Cloud-Native)
Todo archivo subido por un cliente (UI) al Backend debe seguir el patrón **Claim Check**:
1. El archivo es "subido" en streaming (Chunking) usando el SDK de almacenamiento de objetos puro en la nube: **Azure Blob Storage** (o AWS S3 Equivalent).
2. Si el archivo es estrictamente un oficio jurídico reglado en lugar de un anexo volátil, la integración lo inyectará directamente al Content Management System (Ej: **SharePoint / Alfresco / SGDEA Nativo**) utilizando adaptadores CMIS del Backend.
3. El motor de Base de Datos MySQL (y por ende, el proceso de Camunda) **ÚNICAMENTE persistirá el Metadata Tracker (URL o URI del Blob)** asociado al `expediente_id` o `tarea_id`.

## Argumentos del Trade-Off (Por qué Blob Storage gana por "knockout")

| Criterio | Guardar Binarios en DB (MongoDB/MySQL) | Object Storage (Azure Blob / S3) | Veredicto |
| :--- | :--- | :--- | :--- |
| **Costo (Storage)** | Altísimo (SSD Premium de Base de Datos) | Ínfimo (Centavos por GB en discos fríos/Cool Tier) | 🏆 Blobs son un 90% más baratos. |
| **Costo (Cómputo CPU/RAM)** | Carga la memoria RAM de la JVM y de la base de datos limitando concurrencia de transacciones | Nulo. El Backend solo transfiere un InputStream ciego. El disco de red lo maneja Azure. | 🏆 Descarga de I/O en la VM del Backend. |
| **Rendimiento Gral** | Destruye la latencia de las transacciones ACID al mezclar data estructurada con flujos binarios masivos. | Excelente para archivos masivos y descargas en paralelo directas (Pre-Signed URLs).| 🏆 Separación de responsabilidades. |
| **Backups y DR** | Respaldar y restaurar la base de datos toma días al sobrepasar los 5TB. | El Storage Account tiene su propio SLA de Geo-Replicación automática asíncrona infinita. La BD MySQL queda muy ligera (<50GB). | 🏆 RTO/RPO ultracorto para la Data Viva. |

## Impacto Inmediato
1. **Modelos JPA:** Entidad FileEntity o DocumentMetadataEntity en `ibpms-core` solo tendrá columnas `uuid, file_name, mime_type, blob_uri`.
2. **Infraestructura:** DevOps deberá desplegar un *Storage Account* y configurar su acceso en el Azure Key Vault para inyectarlo en las *Managed Identities* de las VMs.
3. **Frontend:** Nunca intentar convertir PDFs grandes a `Base64` en el JSON. Siempre mandar con Content-Type: `multipart/form-data`.
