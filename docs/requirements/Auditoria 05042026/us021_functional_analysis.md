# Análisis Funcional Definitivo: US-021 (Mapeo de Variables y Tolerance Fricción Cero)

## 1. Resumen del Entendimiento
La US-021 actúa como "Traductor o Mapper" entre el mundo desordenado y externo del CRM (Catálogo comercial y Service Contracts) y el mundo estructurado/rígido de Motores de Workflow BPMS (Ej. Variables Requeridas para un Start Event en Camunda).

## 2. Objetivo Principal
Asegurar que las variables exógenas (Dato Comercial) se "transmuten" a variables endógenas (`execution.getVariable("")` en Java/Camunda), tolerando ausencias menores para evitar bloqueos HTTP 500 en las API del cliente.

## 3. Alcance Funcional Definido
**Inicia:** El Front o un webhook entra con el JSON payload del CRM.
**Termina:** El Payload es filtrado, mapeado a 'Mapping V_latest' y validado para instanciación.

## 4. Lista de Funcionalidades Incluidas
- **Versionado Inmutable de Configuraciones (CA-4):** Al publicar 'Mapping v2', los Casos Nuevos usan v2 pero los casos 'In Flight' que nacieron con V1 heredan su trazabilidad originaria.
- **Tolerancia a Catálogos Incompletos (CA-5):** Si el Payload Externo carece de campos "no-críticos" definidos, el iBPMS lo omite en silencio sin romper la transacción de instanciación del caso operativo.

## 5. Lista de Brechas, Gaps o Ambigüedades Detectadas
- **Crash Diferido en el Workflow (Bomba de Tiempo) (⚠️ CA-5):** El CA permite a la capa API "ignorar campos faltantes y seguir adelante con la instanciación de Camunda". **GAP:** Si un BPMN tiene un *Exclusive Gateway* (Rombo de Decisión) más adelante que evalúa lógicamente `if(URL_Imagen_Promocional != null)`, el motor crasheará en Runtime de forma asíncrona ("Incident") debido a la carencia de la variable omitida maliciosamente en la capa API. Tolerancia estricta a falta de Variables en Start Execution de Camunda 8/7 no puede ser "Olvidarlo y arrancar", el Mapper debe Inyectar y Forzar variables con tipo base (`null`, `""`, o `false`) para garantizar certidumbre en las evaluaciones condicionales JEXL/FEEL del motor de ejecución.

## 6. Lista de Exclusiones (Fuera de Alcance V1)
- Validaciones Cruzadas Complejas (Ej: Regex Mappers no declarados explícitamente, operan puramente Exists/Not Exists).

## 7. Observaciones de Alineación o Riesgos
**Desincronización QA:** Si el Mapeo de tolerancias permite arrancar esquemas corruptos, QA sufrirá pesadillas de reproducibilidad, simulando casos y obteniendo excepciones `VariableNotFoundException` en mitad del árbol transaccional lejos del inicio del embudo.
