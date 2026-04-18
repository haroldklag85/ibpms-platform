# Análisis Funcional Definitivo: US-037 (CRUD de Orígenes Inbound Email / Intake API)

## 1. Resumen del Entendimiento
La US-037 dictamina cómo el motor extrae información no estructurada desde el ecosistema on-premise/cloud del cliente corporativo (Microsoft Office 365, Google Workspace, y plataformas SMTP legadas).

## 2. Objetivo Principal
Automatizar y centralizar el ciclo vital de un "Buzón de Atención", abstrayendo los protocolos de interconexión para el resto de capas MLOps e Intake, dictando de entrada una salida pre-aprobada obligatoria si la algoritmia falla.

## 3. Alcance Funcional Definido
**Inicia:** Un Súper Admin carga las credenciales Graph OAuth/IMAP en la Pantalla 15.
**Termina:** El Cron Poller de ingesta despierta y comienza a mapear el buzón hacia la bandeja interna Docketing (US-011).

## 4. Lista de Funcionalidades Incluidas
- **Ingesta Híbrida (CA-1):** Tolerancia OAuth (Modern) y Simple Auth (IMAP/Legacy).
- **Enrutamiento de Emergencia (CA-3):** Asignar un `Defaut_BPMN_Process` por buzón de manera compulsiva para atajar correos que el LLM no supo resolver y quedaron como Intake Huérfanos.
- **Botón de Enfriamiento Rápido y Polling (CA-4, CA-8):** Fetch periódico cada 5 min + Botón de parada de emergencia para cortar el torrente en caso de ataque Spam.
- **Réplica Segura (Soft-Delete CA-6):** iBPMS nunca da Hard Delete a Microsoft O365, aplica flags de Soft Delete al correo para no engranar responsabilidad Forense final.
- **Excepciones Funcionales (CA-9):** Override de Máx Payload adjuntado por canal de ingesta.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Riesgo Severo de Seguridad Perimetral OAuth (⚠️ CA-6):** Dicta que "Si un Super Admin decide borrar el caso en el iBPMS, el motor envía instrucción de Soft-Delete hacia Microsoft O365 (Papelera)". **GAP:** Lograr que una App Externa borre correos de Microsoft exige solicitar permisos Graph `Mail.ReadWrite` o `Mail.ReadWrite.All` en el Tenant corporativo. La mayoría de los CISO bancarios o de gobierno denegarán rotundamente entregar tokens de ESCRITURA GLOBAL a un proveedor sobre sus buzones de reclamo. Este CA sube groseramente la superficie de exposición para una ventaja menor. "Solo lectura" (`Mail.Read`) debería ser el imperativo de seguridad V1 en inbound corporativo para evitar auditorias extenuantes.
- **Sincronía Mítica (CA-5):** Hacer ping HTTP a Auth en vivo es válido, pero un 200 OK no verifica autorización IMAP subyacente. Puede entrar a Oauth y fallar en lectura Folder.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Whitelisting/Blacklisting. Ocurre a nivel de firewall/regla O365, el iBPMS es pasivo (Mastico lo que me llegue a Inbox limpio).

## 7. Observaciones de Alineación o Riesgos
**Fricción de Volumetría:** El "Default BPMN Process" (CA-3) como sumidero puede causar que si un bot envía 10k SPAMS chinos a "contacto@empresa", y pasan el O365, el LLM declarará incompetencia... ¡pero el buzón disparará la instanciación automática de 10,000 Camunda Process Instances "Por Defecto"! Esto crashearía el disco en 3 horas por culpa del CA-3. Los Intakes no resueltos NO deben instanciarse automáticamente en Camunda jamás, deben quedarse en la Cola de Intake de la Bandeja 1B para descarte manual en masa por humanos.
