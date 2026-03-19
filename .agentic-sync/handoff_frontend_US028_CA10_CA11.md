# Handoff Frontend - Iteración 34 (US-028: CA-10 a CA-11)

## Propósito
Blindar la memoria del Editor QA contra borrados accidentales (refrescos F5) y conectar la prueba exitosa con el Backend para lograr el Sellado Criptográfico.

## Criterios de Aceptación Cubiertos (Frontend)
* **CA-10 (Persistencia Local de Sandbox):** Usar la directiva `@vueuse/core -> useLocalStorage` sobre el estado reactivo del `[📥 Payload Crudo]` manual (el texto del editor). Si el usuario refresca la página con `F5`, el JSON reconstruido a mano sobrevivirá. (Amnesia Cero).
* **CA-11 (Disparador de Certificación):** Renderizar condicionalmente un super-botón `[ 🏆 CERTIFICAR CONTRATO ZOD ]` que solo se ilumine y habilite cuando el panel de "Payload Parseado" esté en verde estricto sin ZodIssues.
* Al hacer click, consumirá `POST /api/v1/design/forms/{id}/certify` enviando el esquema y el payload usado para que el Backend genere el Hash SHA-256.

## Directrices V1 
Sin mutaciones de red exóticas. Invocación Axios clásica. 

## Tareas Frontend
1. Importar e implementar `useLocalStorage` para la variable atada al Monaco Editor del Sandbox.
2. Añadir el botón `CERTIFICAR`.
3. Conectar axios hacia el endpoint de certificación e informar con un Toast Verde el éxito del hash.
