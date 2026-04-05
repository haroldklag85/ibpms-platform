# Análisis Funcional Definitivo: US-035 (Integración SharePoint y Auditoría Documental)

## 1. Resumen del Entendimiento
La US-035 decreta que la Plataforma iBPMS No es un Gestor Documental de Carga, sino un "Pasarela Inteligente" que delega toda la contención estática legal de PDFs hacia el SGDEA oficial corporativo (Microsoft SharePoint) usando Service Accounts silenciosos. Funciona como un orquestador Zero-Storage.

## 2. Objetivo Principal
Ahorrar miles de dólares en discos EC2/S3 (Amazon) impidiendo que iBPMS asimile archivos pesados, empujándolo todo hacia ecosistema Microsoft (ya licenciado), mientras retiene localmente el Hasheo (SHA-256) logrando "Confianza Cero" (Zero-Trust) para detectar manipulaciones en la nube por parte de empleados intrusos.

## 3. Alcance Funcional Definido
**Inicia:** Con la invocación del Uploading Component Dropzone.
**Termina:** Con la inserción en M365 (O365) en carpeta enrutada, y guardado de "URI URL" y Checksum en BD Postgres.

## 4. Lista de Funcionalidades Incluidas
- **Delegación Táctica "URL" (CA-1):** BD Postgres conserva solo Links hacia SP.
- **Topología Automatizada (CA-2):** Creación autómata de Folders de Expediente "`/CasoX`" mediante MS Graph API.
- **Evadir Perímetro OAuth (CA-3):** Los usuarios externos leen archivos vía la *App Registration* del servidor, no con su Microsoft Auth.
- **Destrucción Legal Retención TRD (CA-4):** Integración nativa de Expiraciones documentales.
- **Inmutabilidad Incremento (CA-5):** Prohíbe destruir borradores anteriores (Versiones SPO), solo adjunta revisiones nuevas.
- **Visor Embebido Nocivo Cargas (CA-7):** Iframe visualiza SP sin forzar descargas físicas O.S. (Prevención fuga datos).
- **Lista Blanca Paranoica y Límite Peso (CA-11/12):** Cortafuego explícito de extensiones ejecutables.
- **RAG Ingestión IA (CA-17):** Disparador hacia cerebro IA (Cola).
- **Cronjob Purga Storage (CA-18):** Destrucción nocturna (Deep-Clean) de Uploads "huérfanos" (Max 24h) residentes en `/upload-temp` antes de anclarse a SharePoint.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Contradicción Masacre Documental de Borradores (⚠️ FATAL SRE CA-18 vs US-029):** El Cronjob estipula borrar sin miramientos archivos de `/upload-temp` con 24 hrs asumiendo error de usuario en un "Upload-First". Sin embargo, durante Iteración 2 constatamos en la US-029 la existencia del "Autoguardado Permisivo", permitiendo al usuario archivar "Para continuar mañana". Si el CA-18 purga cada noche la rama Temp, destrozará toda la evidencia subida legítimamente por usuarios de Trámites largos. Fallo sistémico severo.
- **Choque Normativo Anti-Malware (⚠️ CA-15 vs US-027):** Este archivo asume ciegamente confiar en los "Defensores de Microsoft SharePoint" y explícitamente exonera a la aplicación de utilizar un Anti-Malware pasante para subidas. Pero la US-027 (Copilot IA) exige taxativamente en el CA-03 que "El archivo pasará por ClamAV Cloud en milisegundos". La arquitectura de iBPMS o confía en ClamAV para todos los *Stream Inputs*, o confía en SharePoint Async Scanner. Mezclar filosofías de seguridad perimetral genera puntos ciegos. Si SharePoint descubre un malware "después" del upload asíncrono, iBPMS no es capaz de notificar a Camunda transaccionalmente.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Lector Óptico Zonal OCR (No Neuronal hasta V2 - Queda como Binary Blob CA-9).
- Anotadores PDF Nativos editables encima de la interface (Diferidos V1.2).

## 7. Observaciones de Alineación o Riesgos
**Riesgo Activo de Contaminación:** Las asimetrías de ciberseguridad en subida de ficheros (ClamAV selectivo) y el Cronjob agresivo causarán que la herramienta falle las auditorías de usabilidad y los reportes de calidad del Ethical Hacking en un mes. Urge rediseñar la política global I/O unificada.
