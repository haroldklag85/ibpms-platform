# Handoff: Backend — Profilaxis Estructural GAPs (Sprint 1 / Code Freeze)

## 1. Metadatos y SSOT
- Sprint: S1 (Code Freeze activo — solo profilaxis, cero lógica de negocio nueva)
- US en scope: US-035, US-011, US-021, US-045 (andamiaje scaffolding — NO refinar)
- Hallazgo crítico real: Duplicidad SacMailbox (bifurcación poc vs core.sac)
- Propuesta aprobada: B — Architectural Fencing
- SSOT de referencia: `.agentic-sync/handoff_backend_gap_profilaxis.md`

## 2. Alineación Arquitectónica
- ADR-001 (Hexagonal): La purga de SacMailbox restaura la pureza del dominio.
  Capa `domain/` debe tener UNA sola entidad por agregado. Dos clases con
  el mismo nombre en paquetes distintos viola la inversión de dependencias.
- Ningún ADR nuevo se emite — esta es remediación, no decisión arquitectónica.

## 3. Rutas Exactas y Contexto

### Tarea 1 — UnsupportedOperationException en stubs
Archivos objetivo y métodos exactos:

| Archivo | Método(s) a modificar |
|---------|----------------------|
| `application/service/sgdea/SharePointAdapterService.java` | `uploadMassiveFileStream`, `createFolder`, `injectMetadata`, `searchFullText` |
| `infrastructure/adapter/out/MailboxPollingCron.java` (paquete core.sac) | El bloque que simula extracción de correos Graph (actualmente comentado) |

### Tarea 2 — HTTP 501 en controller huérfano
| Archivo | Acción |
|---------|--------|
| `infrastructure/web/intake/AllowedDomainAdminController.java` | Agregar `@Operation(hidden=true)` + ResponseStatusException 501 en todos los endpoints |

### Tarea 3 — Purga hexagonal SacMailbox
| Acción | Archivo |
|--------|---------|
| ELIMINAR | `com/ibpms/poc/domain/model/SacMailbox.java` |
| ELIMINAR (si existe) | `com/ibpms/poc/infrastructure/jpa/repository/SacMailboxRepository.java` |
| CORREGIR imports | Todo archivo en `core.sac.*` que importe del paquete `poc.domain.model.SacMailbox` |
| ÚNICO paquete válido | `com.ibpms.core.sac.domain.SacMailbox` |

## 4. Snippets Prescriptivos

### Snippet A — UnsupportedOperationException (aplicar en Tarea 1)
```java
@Override
public void uploadMassiveFileStream(String siteId, String fileName, Resource fileStream) {
    throw new UnsupportedOperationException(
        "GAP-1 [US-035]: SharePoint Graph API upload — pendiente refinamiento Sprint asignado."
    );
}
```
Replicar el mismo patrón en cada método stubbed. El mensaje debe identificar el GAP y la US.

### Snippet B — HTTP 501 en controller (aplicar en Tarea 2)
```java
@GetMapping
@Operation(hidden = true)
public ResponseEntity<Void> getAllowedDomains() {
    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
        "GAP-4 [US-045]: AllowedDomain Admin — pendiente refinamiento Sprint asignado.");
}
```
Aplicar en TODOS los endpoints del controller, no solo en uno.

## 5. Matriz de Verificación
| Tarea | Verificación | Criterio de Éxito |
|---|---|---|
| T1 SharePoint stubs | Grep: throw new UnsupportedOperationException en SharePointAdapterService | ≥ 4 ocurrencias |
| T1 MailboxPollingCron | Grep: UnsupportedOperationException en MailboxPollingCron | ≥ 1 ocurrencia |
| T2 Controller 501	| curl -X GET localhost:8080/admin/webhook/allowed-domains | HTTP 501 en respuesta |
| T2 OpenAPI | Swagger UI no muestra el endpoint | Ausente en /swagger-ui |
| T3 Purga | find . -path "*/poc/domain/model/SacMailbox.java" | 0 resultados |
| T3 Compilación | mvn clean compile | BUILD SUCCESS sin warnings de SacMailbox |

## 6. Mensaje de Despacho
Ejecutar la profilaxis en este orden: T3 → T1 → T2 (la purga primero para que T1 no compile
referencias al paquete poc).

Compilación obligatoria: Ejecutar el protocolo completo del
Backend SRE Compilation Audit SKILL:
`docker compose up -d --build ibpms-core`
`docker compose logs -f ibpms-core`
Validar: "Tomcat started on port(s): 8080"
Solo entonces: `git commit -m "chore(profilaxis): Architectural Fencing GAPs S1"`
