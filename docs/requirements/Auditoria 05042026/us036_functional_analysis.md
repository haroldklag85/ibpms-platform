# Análisis Funcional Definitivo: US-036 (Matriz de Control de Acceso Basado en Roles - RBAC)

## 1. Resumen del Entendimiento
La US-036 es la armadura de Gobernanza de Privilegios del sistema. Provee la Matriz ISO 27001 para que el Oficial de Ciberseguridad (CISO) asigne perfiles a Formularios / Flujos BPMN, garantizando Row-Level Security transaccional entre operadores (Nadie ve el trabajo de otro si no debe).

## 2. Objetivo Principal
Desacoplar la lógica de permisos del Front/Back, inyectando un esquema de "Super Admin", "Plantillas de Clonación" (Bulk assignment), "Break-Glass" M2M Tokens y Autogestión Temporal de Ausentismo (Delegación).

## 3. Alcance Funcional Definido
**Inicia:** SuperAdmin entra a la Pantalla 14 Matriz.
**Termina:** El usuario final entra interceptado, gozando solo de los Permisos "Iniciador", "Lector" o "Ejecutor" (Lanes DInámicos Camunda).

## 4. Lista de Funcionalidades Incluidas
- **Row-Level Security (Data Segregation CA-3784):** Oculta expedientes asíncronamente si el usuario no tiene la apropiación sobre él, limitando visualización.
- **Role Hierarchy / Inheritance (CA-3789):** Gerente = 100% Rol Base Analista. Aditivo y jerárquico.
- **Rango Fecha de Delegación (CA-3803):** Renuncia y Suplantación legal temporal automatizada para ausentismos / Vacaciones. Módulo autogestionado.
- **URL Anónima / Portal Público (CA-3834):** Excepción RBAC. By-pass explícito (Zero-Auth) para Formularios Externos de Ciudadanos sin usuario. Permit.
- **Cuentas de Servicio (M2M) (CA-3809):** API Keys de larga duración asilados de Humanos para uso exclusivo de ERPs e integraciones Inbound Webhooks.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Fatal Segregación de Funciones (Juez y Parte) (⚠️ CA-3849 contra CA-3915):** **GAP GRAVE SSOT**. Esta US-036 decreta literalmente: *"Para el MVP V1, el motor iBPMS NO frena estructuralmente a un humano si Camunda le enruta 'Crear' y 'Aprobar' al mismo tiempo. Asume este riesgo y lo difiere a V2"*.  Sin embargo, la inmediatamente siguiente US-038 (CA-3915) estipula: *"El sistema DEBE BLOQUEAR matemáticamente la transacción (Creator != Approver), detectando Conflicto de Segregación de Roles (Juez y Parte)."*. El Backlog está partido a la mitad con dos reglas mutuamente excluyentes para The V1 Scope. Debe dictaminarse si existirá Hard-Block SoD o Relajación temporal. Si el requerimiento técnico V1 carece de motor de reglas complejo, US-038 miente.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Segregación SoD offline conflict (Revisión de CA requerida).
- Ocultar campos individualmente vía RBAC Portal (Se delega a la Lógica Formularios US-038 / ProCode Vue Conditional rendering).

## 7. Observaciones de Alineación o Riesgos
La existencia de una cuenta inborrable `[Super_Administrador]` como Semilla BD es un patrón seguro si y solo si, la contraseña de inicialización se rota de Inmediato en Base de Datos de forma asimétrica (BCrypt) con salting. No debe subirse NUNCA a Github un Liquibase un `pass=admin123` en texto plano en la migración de Seed.
