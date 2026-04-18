# Análisis Funcional Definitivo: US-042 (DevPortal: API Keys, Cuentas M2M y Sandboxing)

## 1. Resumen del Entendimiento
La US-042 es el ecosistema de O-Auth (Cuentas de Servicio M2M) y la infraestructura "Componible" (V2 Ready). Permite a sistemas externos consumir APIs core o inyectar IFrames frontales de forma blindada bajo modelo Zero-Trust.

## 2. Objetivo Principal
Tercerizar o "enganchar" ERPs e integradores de terceros al corazón del iBPMS emitiendo credenciales rotativas con Sub-Scopes, evadiendo colisiones, caídas de DDoS y robos masivos de base de datos.

## 3. Alcance Funcional Definido
**Inicia:** Agente o Humano genera Cliente Secreto en DevPortal.
**Termina:** Third-party app inyecta postMessage en un Vue IFrame Sandboxeado o consulta el API O-Auth Gateway sin acceder a base maestra relacional.

## 4. Lista de Funcionalidades Incluidas
- **Ceguera Intencional y Sub-Scopes JWT (CA-4119):** Limita el poder de las Apps de manera binaria (`App_Read_Only`).
- **Autodestrucción de Secreto Visual (CA-4107):** Mecanismo estricto Zero-Trust (Solo se muestra 3 veces tras su creación).
- **Gateways APIM / DDoS y Sandboxing Frontend (CA-4124 / 4152):** Bloqueos de Rate Limit 429 y cuarentena modular para Plugins Vue IFrames que muten la interfaz nativa del portal, usando estricto `window.postMessage`.
- **Aislamiento Multitenant M2M y Audiencia Limitadora OIDC (CA-4158):** Claims custom en JWT `aud: ibpms...`.
- **Alerta Heurística de Falla 403 (CA-4143):** Auditoría real-time a correo CISO si el "Bot del CRM" intentó tocar un archivo para el que no fue conferido permiso. Trazabilidad inamovible (Culpa Compartida CA-4148).

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción en Revocación Inmediata de JWT (⚠️ CA-4134):** Vuelve el fantasma de Revocar de un botón a un Token JWT en memoria de internet en milisegundos. *"El token cae de inmediato"*. Similar al GAP detectado en la US-048, esto sólo es factible (sin latencia perimetral) si Kong / API Gateway evalúa una Redis Blacklist síncrona, rompiendo la naturaleza del Stateless, pero asumiendo el SRE Fail-Open.
- **Rendimiento forzado en Arquitectura Hexagonal "Inboard" (⚠️ CA-4164):** El CA estipula que los desarrolladores de "Súper Módulos" tienen **"Prohibición de Bypass JPA"** para modificar Data, obligando a usar un WebClient API REST, incluso estando compilados en el mismo nodo físico. Esto asegura el desacoplamiento sagrado del Hexágono, pero inflige The Network Penalty (Loopback HTTP Serialization Cost) para módulos In-house. Se asume como un precio aceptable en nombre del "Aislamiento Zero-Trust".

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Integraciones complejas con AWS EventBridge nativo o Webhook Pub-Sub asíncrono (Centrado en API GET/POST Síncrona REST para el MVP).

## 7. Observaciones de Alineación o Riesgos
La estrategia "Quarantine" de nuevos módulos y el uso de Base Sandbox (CA-4129) añade complejidad de despliegue: Obliga a tener en el Servidor Core de Producción dos Pooles de conexiones Hikari (`DB_PROD` y `DB_MIRROR_SANDBOX`), lo cual aumenta la carga de memoria RAM en un 20%.
