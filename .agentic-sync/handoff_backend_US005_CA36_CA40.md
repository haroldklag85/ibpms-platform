# Contrato de Arquitectura Backend (Iteración 23 | US-005: CA-39)

**Rol:** Desarrollador Backend Java/Spring Boot.
**Objetivo:** Tender el puente entre el dominio del Process Engine (US-005) y el Form Builder Engine (US-028), habilitando la consulta transversal de Formularios.

## 📋 Contexto y Criterios de Implementación:

*(Nota: CA-36 y CA-40 son netamente lógicas de XML en el Frontend. CA-37 y CA-38 están diferidos a V2).*

### Tarea 1: API de Catálogo de Formularios (CA-39)
Para que el Frontend construya su Dropdown restrictivo en el Panel de Propiedades, debes proporcionarle el directorio de formularios vivos.
*   Crea el Controlador Mock `FormCatalogController.java` (o intégralo en uno existente).
*   Expón el Endpoint `GET /api/v1/forms`.
*   Retorna un arreglo DTO que simula la base de datos de Formularios. 
    Ejemplo de JSON: 
    ```json
    [
      { "id": "frm_aprobacion", "name": "Formulario Aprobación", "type": "SIMPLE" },
      { "id": "frm_onboarding_master", "name": "Onboarding Integral", "type": "MASTER", "stages": 4 }
    ]
    ```

## 🛑 MANDATO LOCAL GATEKEEPER
Tienes **ESTRICTAMENTE PROHIBIDO** hacer `git commit` a principal.
Despliega el Endpoint de Servicio, lánzalo y encampsula tu código en Git:
`git stash save "temp-backend-US005-ca36-ca40"`

Informa textualmente la confirmación del guardado.
