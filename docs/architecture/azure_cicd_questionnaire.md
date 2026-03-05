# Requerimientos de Infraestructura para CI/CD (Azure Monolítico V1)

**Para:** Proveedor de Infraestructura (Azure Reseller / Administrador de Nube)
**De:** Equipo de Arquitectura iBPMS
**Objetivo:** Obtener los parámetros, credenciales y topología de red requeridos para construir y ejecutar los pipelines YAML de Azure DevOps (o GitHub Actions) apuntando a los ambientes de QA y Producción.

Estimado equipo de Infraestructura,

De acuerdo al diseño táctico (V1) de la plataforma iBPMS, requerimos que nos provean la siguiente información y nos habiliten los accesos descritos a continuación para poder automatizar el despliegue de nuestros artefactos (Frontend en Vue 3 y Backend en Spring Boot) hacia las Máquinas Virtuales (VMs) aprovisionadas.

Por favor, respondan el siguiente cuestionario técnico:

## 1. Autenticación y Permisos (Service Principals)
Para que nuestro orquestador de CI/CD pueda interactuar con los recursos de Azure sin intervención humana, requerimos una identidad gestionada:
*   [ ] ¿Han creado el **App Registration / Service Principal (SPN)** dedicado para nuestro pipeline?
*   [ ] Por favor proveer:
    *   `Tenant ID`
    *   `Subscription ID`
    *   `Client ID` (Application ID)
    *   `Client Secret` (o en su defecto, configurar la federación OIDC si usamos GitHub Actions/GitLab).
*   [ ] ¿El SPN tiene permisos de **Contributor** (o un RBAC custom equivalente) estrictamente limitados a los *Resource Groups* de QA y Producción?

## 2. Topología de Despliegue en VMs (IaaS)
Dado que el despliegue V1 es sobre Máquinas Virtuales Linux, necesitamos conocer el mecanismo de inyección del código:
*   [ ] **Mecanismo de Conexión:** ¿Las VMs exponen un puerto SSH (22) accesible desde el runner CI/CD, o debemos utilizar **Azure Run Command** / **Azure Bastion** para inyectar los scripts por restricciones de VNet?
*   [ ] Si la conexión es por SSH (Runner Auto-hospedado o IP Whitelisted), por favor proveer la **SSH Key** privada autorizada y el usuario (ej. `azureuser`) para conectar a cada VM.
*   [ ] **Service Management:** ¿Bajo qué sistema de demonios corren nuestras aplicaciones? (Ej: `systemd` para reiniciar el `.jar` de Spring Boot y `nginx`/`apache2` para servir la carpeta `/dist` de Vue 3). ¿El usuario de SSH tiene permisos `sudo` sin contraseña para reiniciar estos servicios?
*   [ ] **Rutas Absolutas:** Listar las rutas exactas en los discos de las VMs donde debemos depositar los artefactos:
    *   Ruta Backend (QA y Prod): `/opt/ibpms/...` ?
    *   Ruta Frontend (QA y Prod): `/var/www/html/...` ?

## 3. Base de Datos (Migraciones Liquibase/Flyway)
El pipeline backend debe ejecutar los scripts DDL/DML contra MySQL durante el despliegue.
*   [ ] ¿El runner de CI/CD tiene alcance de red (VNet Peering o Whitelist) al puerto `3306` del servidor MySQL? 
*   [ ] Si la BD MySQL está alojada en una **Azure Database for MySQL (Flexible Server)** pública restringida o es privada (Private Endpoint), ¿cómo recomiendan acceder desde el pipeline para correr las migraciones?

## 4. Gestión de Secretos (Azure Key Vault)
La arquitectura exige Zero-Trust y ofuscación de secretos (Ej: MS Graph Client Secret).
*   [ ] Por favor confirmar la URL/URI del **Azure Key Vault** para QA y Producción.
*   [ ] ¿Las VMs (Frontend/Backend) tienen asignada una **Managed Identity (System Assigned)** con permisos de *Key Vault Secrets User* para que la aplicación Java lea los secretos en caliente sin quemar contraseñas en el código?
*   [ ] ¿El Service Principal del pipeline (punto 1) tiene permisos para inyectar/actualizar nuevos secretos en el Key Vault durante la fase de release?

## 5. Control de Tráfico (APIM o Load Balancer)
Durante un despliegue en Producción (donde hay 3 VMs, asumiendo clusters u horizontalidad):
*   [ ] ¿Podemos invocar comandos por CLI al **Azure API Management** o al Load Balancer para drenar conexiones de una VM mientras la actualizamos (Blue/Green o Rolling Update), o el reinicio del servicio asumirá un downtime aceptado (Hard Restart)?

---
**Firmado:** Arquitectura iBPMS
