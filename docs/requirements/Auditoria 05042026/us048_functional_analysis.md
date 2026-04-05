# Análisis Funcional Definitivo: US-048 (Módulo Gestor Propio de Identidades Internal-IdP)

## 1. Resumen del Entendimiento
La US-048 es el Salvavidas (Fallback Standalone) para el Core Identity. Cubre clientes PyMEs u On-Premise Desconectados que no poseen Microsoft EntraID/SSO. Contempla una DB de Identidades Internas regida por el iBPMS 100% aislada. 

## 2. Objetivo Principal
Un Micro-IdP que crea credenciales en Hash Bcrypt, administra usuarios (alta/baja), asocia Roles (Internal CRUD), resetea claves oralmente (Emergency Code) híbrida la interfaz.

## 3. Alcance Funcional Definido
**Inicia:** Admin da de alta "Usuario Local" en Pantalla 14.
**Termina:** El usuario se autentica contra DB iBPMS Local superando Entropía Strong Password.

## 4. Lista de Funcionalidades Incluidas
- **Creation By Invitation Admin-Only (CA-3989):** Nada de auto-registro en MVP 1. Absoluto control piramidal.
- **Entropía Local Estricta (CA-3994):** Minúsculas, Mayúsculas, Num y Special (Vía regex frontend y Validador Java anotation `@StrongPassword`).
- **One-Time Pass-Code Verbal (CA-4000):** Admin oprime Re-set, devuelve Token Random Crudo, el Admin lo dice oralmente, usuario cambia forzosamente. 
- **Ocultamiento por Modo SSO (Híbrido) (CA-4021):** Si el servidor detecta OIDC Auth (Modo Múltiple Enterprise), enmascara campos nativos "Change Pass", evitando que los administradores intenten sobre-escribir identidades que viven el Microsoft Cloud Nube externa.
- **UI Multi-Select Limitado (CA-4016):** Permite hibridación de Roles Dinámicos sin duplicar entradas visualmente.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Kill-Switch Engañoso VS Fail-Open (⚠️ CA-4011):** Declara: `"al apagarlo (User Inactivo), el Backend destruye sesiones (JWT/Redis)... expulsándolo instantáneamente"`.  **El FALSO ABSOLUTO JWT:** Los JWT Access Tokens no se pueden "destruir", residen asíncronamente en el Storage/Memoria PWA del dispositivo final de Juan Pérez Offline. La única forma de "revocarlos" es añadirlos a una Lista Negra (Redis BlackList Validation Filter Spring Security). **GAP Funcional Oculto:** En la US-038 (CA-3873) el arquitecto impone un "Redis Fail-Open Policy" (Si Redis muere el iBPMS ignora los vetos). Por ende, si el "Super Admin" lo despide y Redis está colapsado sin saberlo, Juan Pérez ESTARÍA DENTRO consumiendo Base Data con su JWT nativo. La US-048 debe establecer "Expiración Corta Transaccional (JWT 15 Mins TTL) + Rotación Continua" en lugar de delegar todo el poder letal del despido inmediato a un Caché Redis Volátil.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Gestor Público de Reestablecer Identidad SMTP Libre (Desestimado, Forzado verbal a IT Support).

## 7. Observaciones de Alineación o Riesgos
El encapsulamiento "By Admin Only" aniquila una inmensa capa de complejidad técnica asociada al flujo de confirmación (Email Confirm links, OTP, Captchas Anti-Bot de Auth0). Reducción drástica inteligente del perímetro de ataque del SaaS local.
