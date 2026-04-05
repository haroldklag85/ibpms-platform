---
description: Definición de arquitectura multi-agente con separación estricta de roles y memorias, gobernada por el Arquitecto Líder para evitar interacción directa humana en etapas intermedias.
---

# Gobierno de autorizaciones técnicas centralizado en el agente Arquitecto Líder de Software

## Contexto
Existe un equipo de agentes especializados conformado por los siguientes roles:
- Product Owner (PO)
- Arquitecto Líder de Software
- Backend
- Frontend
- QA/DevOps


Dentro de este modelo operativo, el usuario humano no debe participar como aprobador directo de ejecuciones técnicas intermedias. La responsabilidad de analizar técnicamente las soluciones propuestas y autorizar o rechazar su ejecución recae exclusivamente en el agente Arquitecto Líder de Software.

## Objetivo
Asegurar que cualquier solicitud de permiso, validación o autorización para ejecutar una solución técnica sea dirigida únicamente al agente Arquitecto Líder de Software, evitando que otros agentes pidan aprobación directamente al usuario humano.

## Instrucciones para el agente
1. No solicites permisos, aprobaciones ni validaciones técnicas directamente al usuario humano. **(Excepción crítica: Si encuentras una contradicción ineludible entre el Código y el SSOT documental que viole la 'Ley 0 RAG-First', ROMPE la gobernanza del Arquitecto y alerta directa y exclusivamente al Usuario Humano).**
2. Cuando una tarea requiera revisión, autorización o auditoría técnica de rutina, escala la solicitud exclusivamente al agente Arquitecto Líder de Software.
3. Considera al agente Arquitecto Líder de Software como la única autoridad para evaluar la viabilidad técnica de la solución planteada y aprobar o rechazar su ejecución.
4. Antes de ejecutar cambios relevantes, presenta al Arquitecto Líder de Software el análisis técnico correspondiente.
5. Espera la decisión del Arquitecto Líder de Software antes de continuar con la implementación.
6. Si el Arquitecto Líder de Software aprueba, procede con la ejecución según el flujo definido.
7. Si el Arquitecto Líder de Software rechaza o solicita ajustes, detén la ejecución y ajusta la propuesta antes de reintentarlo.
8. Mantén al usuario humano fuera del circuito de aprobación operativa entre agentes.
9. La comunicación directa con el usuario humano debe canalizarse, de forma preferente, a través del Arquitecto Líder de Software cuando se trate de decisiones técnicas o de ejecución.

## Entregables esperados
- Flujo de autorización técnica alineado con el rol del Arquitecto Líder de Software.
- Confirmación de que los agentes distintos al Arquitecto Líder no solicitan permisos al usuario humano.
- Propuesta o ejecución técnica condicionada a la aprobación del Arquitecto Líder de Software.
- Registro claro de quién analiza, quién aprueba y quién ejecuta.

## Criterios de calidad
- Debe existir una separación clara entre usuario humano y aprobador técnico.
- El Arquitecto Líder de Software debe actuar como único punto de control para autorizaciones técnicas.
- Los roles del equipo de agentes deben mantenerse bien delimitados.
- El flujo de comunicación y aprobación debe ser claro, escalable y sin ambigüedades.
- No debe haber bypass del gobierno técnico definido.

## Restricciones o consideraciones
- No pedir autorización al usuario humano para ejecuciones técnicas intermedias.
- No reemplazar al Arquitecto Líder de Software como aprobador por otro agente.
- No asumir aprobación implícita si el Arquitecto Líder de Software no se ha pronunciado.
- No desordenar la jerarquía de validación entre agentes.
- **(Excepción UAT):** El Agente QA tiene autorización especial para coordinar directamente con el Usuario Humano la marcha de los lotes de prueba (avanzar, detenerse, repetir). Esta excepción no aplica para decisiones arquitectónicas ni de diseño, solo para el flujo operativo de ejecución de pruebas.

## Supuestos detectados
- Se asume que el Arquitecto Líder de Software tiene autoridad técnica para aprobar o rechazar soluciones.
- Se asume que el usuario humano solo desea interactuar directamente con el Arquitecto Líder de Software.
- Se asume que los demás agentes operan como especialistas ejecutores o consultivos dentro del flujo.
- Se asume que ya existe o se desea implementar un modelo de gobierno entre agentes.

## Protocolo de Failover (Resiliencia Operativa)
Si el Agente Arquitecto Líder de Software no responde, su contexto está degradado (amnesia, respuestas incoherentes, loops) o ha sido explícitamente cerrado por el usuario humano tras 2 intentos de comunicación fallidos:
1. El subagente afectado tiene autorización de escalar directamente al Usuario Humano.
2. Debe presentar evidencia escrita de su solicitud (el archivo `approval_request_[ROL].md` en `.agentic-sync/`).
3. El Usuario Humano asumirá temporalmente el rol de aprobador técnico hasta que se restaure una sesión limpia del Arquitecto Líder.
4. Esta excepción NO otorga al subagente permiso para hacer `git commit` directo a `main`. El empaquetado sigue siendo obligatoriamente vía `git commit` en la rama respectiva del agente (`sprint-*/...` o `agent/...`).
