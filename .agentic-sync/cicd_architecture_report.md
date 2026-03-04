# DevSecOps & CI/CD Architecture Report

## 1. Pipeline Automático Integrado (GitHub Actions)
La brecha operativa denominada "Efecto Embudo" ha sido remediada mediante la inyección del pipeline `ci-cd-pipeline.yml` en la raíz del monorepo (`.github/workflows/`), configurado bajo las siguientes dimensiones:
- **Build Paralelo**: Los jobs `backend-quality-gate` y `frontend-quality-gate` se ejecutan simultáneamente aislando el Monolito Spring Boot (Java 17) del MFE (Node 20).
- **Quality Gates Estrictos**: Integración continua directa. La rama `main` abortará operaciones e invalidará cualquier empuje de imagen a Azure Container Registry (ACR) en caso de que ocurra:
  - Error en compilación de Backend (`mvn clean verify` Exit != 0).
  - Alguna de las 29 pruebas unitarias del Frontend o el linter falle.
- **Push ACR y Azure (CD)**: Solo en un estado impecable o verde (Exit Code 0 combinado), la imagen de la Aplicación en Java se empaquetará, taggeará (hash de git), e ingresará a ACR (`az webapp config container set`). 

## 2. Estrategia "GitFlow Simplificado (No DEV)"
Se ha adoptado una cultura de ramas *Trunk-Based* acoplada a *Pull Requests*. Dada la falta de entorno DEV puro dictaminada:
1. Los ingenieros clonarán ramas efímeras (`feature/*` o `hotfix/*`).
2. Pasarán íntegramente la suite Vitest/JUnit local antes de empujar.
3. Consolidarán hacia `main` usando PRs.
4. El Quality Gate de GitHub Actions operará la CI, siendo el blindaje final que impide que código tóxico arruine el Azure Web Apps global.

## 3. Playbook de Contingencia (Estrategia de Rollback Azure)
Si un `main` pasa las puertas de calidad automatizadas pero fracasa por errores funcionales humanos en el entorno final de Azure (Web App u AKS):
- **Estrategia A (Container Tag Rollback)**: Azure Web App para backend rastrea la imagen `<ACR_SERVER>/ibpms-core:latest`. Para el proceso de reversión de emergencia:
  ```bash
  az webapp config container set --name ibpms-backend-prod --resource-group ibpms-rg --docker-custom-image-name <ACR_SERVER>/ibpms-core:<PREVIOUS_STABLE_HASH>
  ```
  Se inyectará el Hash de GitHub inalterado anterior e instantáneamente revivirá la imagen histórica funcional mientras se remedia el bug en el Trunk.

- **Estrategia B (Revertir el Merge)**: Usar `git revert <commit>` del Pull Request fatal desde GitHub y empujar una nueva pipeline correctiva natural si se involucraban scripts de bases de datos.

> **Status:** Misión de Remediación DevOps/SRE 100% Completada. 
