# Sprint 3 — Retiro de Code Freeze (Bugfixing Residual y Retorno)

> **Sprint:** 3
> **Estrategia:** Transición a Desarrollo V2
> **Prerequisito:** Gate UAT Sprint 2 aprobado (>85% Pruebas Creadas/Aprobadas).
> **Objetivo:** Purgar los últimos bugs difíciles que dejaron los Sprints 1 y 2, y reactivar el "Feature Factory" gradualmente, expandiendo la plataforma hacia las historias CQRS pendientes.

---

## 1. Track de Estabilización (Días 1-2)
- Reescritura profunda y Refactor de arquitectura (si las pruebas demostraron que algún módulo de `ibpms-core` como los Formularios era muy monolítico o acoplado).
- Optimización V8 / Memory Leaks / Optimización SQL detectada durante el test E2E.
- Completar flujos o CAs de las 11 US iniciales que sufrieron mutilaciones necesarias para cumplir la fecha límite de UAT.

## 2. Track de Retorno al Desarrollo (Días 3-4)

Levantamiento de la prohibición de crear código funcional nuevo.
Iniciamos con las prioridades postergadas:
- **US-017 (Persistencia Hexagonal CQRS):** Event Sourcing para el procesamiento estricto del Formulario Genérico.
- **Expansión de Motores (US-004, US-007, US-030).**

El flujo operativo del equipo vuelve al estado híbrido estipulado originalmente, pero ahora todo agente debe seguir el "Test Driven Governance" (Ninguna historia entra sin su Unit Test y su UAT RTM).
