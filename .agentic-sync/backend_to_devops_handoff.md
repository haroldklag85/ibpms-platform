# Handoff Report: Backend Core -> DevOps
**Fecha:** 2026-02-25
**Origen:** Agente Backend
**Destino:** Equipo DevOps / SRE

## Contexto y Cambios Estructurales
Hemos finalizado el Sprint 3 que robustece la Arquitectura Data y Storage del núcleo `ibpms-core`. El equipo de Backend ya cerró la creación de las Entidades JPA y las configuró explícitamente sin "auto-creación mágica" (`hibernate.ddl-auto: validate`). Ahora gozamos de la única verdad mediante DDLs definidos en Liquibase. Adicionalmente, se conectó el adaptador de Documentos (SGDEA) utilizando la librería `azure-storage-blob`.

Para que todos los entornos (Locales, CI/CD) mantengan consistencia sin fallar o consumir nubes públicas innecesarias, necesitamos tu ayuda con los siguientes ajustes en Infra:

---

## 1. Actualización requerida en Docker Compose Local (`docker-compose.yml`)

Por favor, para emular la nube V1 (Azure Blob Storage) de manera gratuita y nativa en local, inyecta la imagen oficial de Microsoft **Azurite**. Esto evitará que los tests de integración y la codificación de los desarrolladores fallen por falta de conexión a Internet.

```yaml
  azurite:
    image: mcr.microsoft.com/azure-storage/azurite
    container_name: ibpms-azurite
    ports:
      - "10000:10000"
      - "10001:10001"
      - "10002:10002"
    volumes:
      - azurite_data:/data
```

---

## 2. Variables de Entorno de Configuración Spring Boot
`ibpms-core` espera las siguientes variables de entorno. Puedes mapearlas dinámicamente o dejarlas con el "Fallback" hacia Azurite que ya codificamos.

### Requerimiento Mínimo para Local
| Variable | Significado | Valor Recomendado (Local) |
|---|---|---|
| `AZURE_STORAGE_CONNECTION_STRING` | Cadena Azure oficial. | `DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;` |
| `AZURE_STORAGE_CONTAINER` | Nombre de la boveda. | `ibpms-documents` |

**Nota Azure Cloud:** En el pipeline de AKS / Azure Web App para los entornos reales (QA/PROD), usar Shared Key Access o Managed Identity Connection Strings oficiales.

---

## 3. Comportamiento en CI/CD y DB Migration
- **Liquibase está activo.** Todo pipeline que ejecute `mvn spring-boot:run` o los Tests (vía `Testcontainers`) aplicará el changelog principal sobre la BD detectada (`001-initial-schema.xml`).
- Ningún miembro está facultado a hacer `UPDATE` estructural en MySQL sin versión DDL controlada por el repositorio de Liquibase. 

**Respetuosamente,**
Agente Backend.
