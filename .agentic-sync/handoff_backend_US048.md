# Handoff Backend - Iteración 35 (US-048: CA-1 a CA-7)

## Propósito
Implementar la arquitectura base del Módulo Gestor Propio de Identidades (Internal IdP). El Backend debe proveer las APIs robustas para gestionar Usuarios, Roles Dinámicos y el ciclo de vida de las Credenciales, aplicando estrictas políticas de seguridad.

## Criterios de Aceptación Cubiertos (Backend)
* **CA-1 (CRUD Centralizado):** APIs REST para Crear, Leer, Actualizar y Listar Usuarios (`/api/v1/admin/users`).
* **CA-2 (Gobernanza de Claves):** Validación en el DTO de creación/actualización: Mínimo 8 caracteres, 1 Mayúscula, 1 Número, 1 Símbolo.
* **CA-3 (Reset Manual):** Endpoint `/api/v1/admin/users/{id}/reset-password` que genere una clave temporal aleatoria (cumpliendo CA-2), hashee y guarde la nueva clave, y devuelva la clave en texto plano *solo en esa respuesta HTTP* para que el Admin la copie.
* **CA-4 & CA-6 (Roles Dinámicos e Híbridos):** CRUD de Roles (`/api/v1/admin/roles`). Soporte JPA para relación Muchos-a-Muchos entre Usuarios y Roles.
* **CA-5 (Kill Switch):** Endpoint para cambiar estado a Inactivo. **CRÍTICO:** Debe incluir lógica para invalidar el token actual del usuario (Ej. lista negra en caché / Redis o manipulación del SecurityContext si aplica localmente).
* **CA-7 (Modo Híbrido):** Retornar en el perfil del usuario un flag `isExternalIdp` para que el Frontend sepa si debe ocultar los campos de contraseña.

## Tareas Java (Prioridad 1)
1. Crear Entidades JPA (`UserEntity`, `RoleEntity`) y DTOs correspondientes.
2. Implementar `UserController` y `RoleController` con Mapeos REST estándar.
3. Configurar validadores de Jakarta (`@Pattern` para claves) y servicios transaccionales.
4. Diseñar mecanismo de Inoperancia (Kill-Switch) invalidando sesiones activas.
