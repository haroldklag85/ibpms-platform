# Análisis Funcional Definitivo: US-050 (Identidad CIAM y Onboarding Zero-Public-Signup)

## 1. Resumen del Entendimiento
La US-050 sella todo el ecosistema perimetral para el Cliente Externo (B2C). Re-enrutamientos, enlaces y tokens seguros amarrados al ecosistema interno ONS, prohibiendo la entrada libre de cualquier identidad basura (Bot-net Signups).

## 2. Objetivo Principal
Blindar el iBPMS portal Externo (`portal.ibpms.com`) mediante estrategias `Magic Links` que atan identidades digitales recién nacidas al CRM_ID oficial, delegando el peso criptográfico a Módulos OIDC (Azure AD B2C / Cognito).

## 3. Alcance Funcional Definido
**Inicia:** BPMN o Humano gatilla evento [Invitar] (CA-3368).
**Termina:** El usuario consume MagicLink, digita Password Mínimo y sella su JWT Definitivo inmutable.

## 4. Lista de Funcionalidades Incluidas
- **Bloqueo Radial Endpoint (CA-3363):** Ausencia tajante de formularios de Alta Pública. Todo fluye "By Invitation Only" de flujos comerciales.
- **Magic Link de Ciclo Corto (CA-3369):** URL One-Time de 24 horas firmada criptográficamente (OIDC Flow).
- **Inmutabilidad del Custom Claim BOLA (CA-3379):** Enganchar a fuego el `CRM_ID` transaccional nativo de The ONS (`CUST-999`) *adentro* del `JWT Access Token`, imposibilitando visualizaciones cruzadas.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Trampas Mortales de Cierre (M&A y Claim Inmutabilidad) (⚠️ CA-3379):** El requerimiento aísla permanentemente la Identidad (Cognito UUID) grabándole un **Atributo Inmutable** (`crm_id = CUST-999`). **GAP Grave de Arquitectura SaaS B2B:** Si la empresa cliente se fusiona en el mundo real (M&A o cambio de NIT/TIN CUST-1004) o cambia representante legal corporativo, el iBPMS transaccional quedará roto porque la identidad nativa "juan@empresa.com" tiene pre-quemado para la eternidad el `CUST-999`. Los Custom Claims del JWT deben poder renovarse temporalmente (Time to Refresh < 1Hr) o consumir perimetralmente una Tabla Externa `Identity_Tenant_Mapping` en vez de Hardcodear en el token de IdP "inmutablemente" a perpetuidad, forzando a eliminar fisícamente y recrear usuarios (Con pérdida total de login History) ante el menor cambio de base de datos corporativa CRM.
- **Falla Práctica One-Time Use Magic Link (⚠️ CA-3376):** Si el enlace es estricto de uso único (Magic Link consumible) y la mamá del usuario traba Safari al definir la clave, el enlace muere. Al ser "Zero Signup" el cliente externo NO tiene cómo oprimir `/resend_link` nativamente sin antes llamar la atención de un Admin ONS u otro "Action Card" del embudo para un reenvío costoso, perdiéndose el auto-servicio. Se aconseja "Time-bound" pero multipropósito hasta consumir éxito absoluto, no "Tap to expire".

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Single Sign On (SSO Federado por Microsoft del Cliente Final) V1 solo usa Local Passwords tras el primer MagicLink (B2C Policy Standard).

## 7. Observaciones de Alineación o Riesgos
Excelente aplicación Anti-DDoS. No poseer página de `/signup` pública reduce masivamente el impacto bot-net script de fuerza bruta sobre inyección SQL en la creación de usuarios.
