# Análisis Funcional Definitivo: US-017 (Ejecución y Persistencia Inmutable CQRS)

## 1. Resumen del Entendimiento
La US-017 es la columna vertebral de la captura de datos (Digital Intake & Execution). En lugar de enviar un JSON gigante a Camunda (ahogándolo en bytearrays `ACT_RU_VARIABLE`), delega el 100% de la carga pesada al Sistema Relacional CQRS Híbrido, guardando históricos en forma de eventos y enviando solo minúsculas banderas semánticas a Zeebe/Camunda 7.  

## 2. Objetivo Principal
Aislar el Motor BPM de la responsabilidad de ser "La Base de Datos del Negocio". Implementar Cero Confianza Gnoseológica (Zero Trust Validation Bidireccional de Zod Schema), Idempotencia por fallos de red humana, y Uploads Pre-Submit IDOR Secured para PII documentacional.

## 3. Alcance Funcional Definido
**Inicia:** El Backend (BFF) consolida un esquema Zod + JSON Histórico (prefillData) y lo envía a la UI (Pantalla 2).
**Termina:** El Worker confirma el JSON Schema, Guarda el Evento, lanza la asincronía de proyecciones planas, emite avance a Camunda y devuelve 200 HTTP al Front que borra su Pinia.

## 4. Lista de Funcionalidades Incluidas
- **BFF y Zod Isomórfico (CA-4568 / CA-4621):** El Backend inyecta el megacontexto a Vue. Al regresar, el Backend REST VUELVE A VALIDAR bajo la misma rígida estructura sintáctica para truncar hackeos API REST (Scraping).
- **Consistencia Lazy Patching (CA-4575):** Si hay nuevas reglas obligatorias en un Draft viejo, el usuario recibe barreras ROJAS exigiendo actualizar la data. 
- **Upload First PII / Anti-IDOR (CA-4585):** Archivos a Bóvedas S3/SharePoint ANTES de Enviar form. Devuelve UIUD. El usuario envía Array de UUIDs a Camunda bajo protección IDOR (pertenencia relacional al Task y User).
- **AES Draft LocalStorage (CA-4598):** Work in progress es guardado a nivel local con AES encryption para PII, liberándose al Backend si hay debounce pasivo de 10 seg.
- **Idempotencia (CA-4611):** Header `Idempotency-Key` ante fallos de red corta transacciones colisionadas (Doble clic optimista).
- **Separación de Responsabilidades y Sagas Compensatorias (CA-4644/4656):** Guardar Evento Inmutable -> Empujar a Vistas. Si falla Camunda API de avanzar el Token -> Rollback reverso a BD CQRS anulando la aprobación.
- **Claim Implícito (CA-4666):** Ignora el botón "Assign Me" (Típico Tasklist) si el operario abre y manda data de una vez, absorbiendo auto-asignación just in time.
- **Rejection Forensics (CA-4673):** Inyección Reactiva en UI del historial de rechazos al devolver tareas sobre el canvas del formulario.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **GAP de Transpilación de Zod (⚠️ CA-4621 Riesgo de Políglota Backend):** Zod es una gema pura en TypeScript (Node/Deno). El Backend de este ecosistema se documenta como "Spring Boot Java" en reiteradas arquitecturas. **Riesgo Crítico Isomórfico:** Un backend en Java no puede ejecutar librerías TypeScript Zod nativamente sin usar GraalVM (pesado) o Nashorn extinto. **Remediación Obliga:** Se debe transpilar automáticamente el esquema de Zod TypeScript a "JSONSchema" en el Build Time (o al vuelo con librerías), para que el validador JSONSchema Validator de Jackson en Java ejecute isomorfamente las reglas sin emuladores JavaScript en el Main Thread del backend.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Intervención directa al flujo por Websockets (Guardado colaborativo tipo Google Docs múltiple: Solo Lock unitario).

## 7. Observaciones de Alineación o Riesgos
Este es el pilar de la salud y escalabilidad relacional a más de 1 Millón de Procesos/Mes del sistema. Minimiza al máximo el impacto transaccional sobre la Base de Datos ZeebeRocksDB (que Camunda debe manejar solo con variables Booleanas y Keys) mandándole el peso ciego al CQRS. Un diseño excepcional de The Hexagonal Pattern.
