---
description: Protocolo obligatorio de Test-Driven Development para el desarrollo. Obliga a que la prueba preceda al código.
---

# Test-Driven Development (TDD-First) Protocol

> ⚠️ **REGLA DE ORO:** Se te prohíbe escribir lógica de producción (implementación) antes de haber escrito y ejecutado pruebas que evalúen esa lógica y fallen de forma predecible.

Este skill obliga a la adopción del ciclo **Red -> Green -> Refactor**. 

## EL CICLO ESTRICTO

### 1. RED (Prueba Fallida)
A partir de los Criterios de Aceptación y el DTO/Contrato esperado:
1. Crea o modifica la clase de test correspondiente (`*Test.java` o `*.spec.ts`).
2. Escribe la aserción exacta que prueba la lógica esperada.
3. Ejecuta la prueba mediante línea de comandos. 
4. **Verifica que la prueba FALLE** porque el código de producción aún no existe o no tiene la lógica requerida. Si compila mal porque falta un método, está bien (el compilador te está dando un test rojo).

### 2. GREEN (Código Mínimo para Aprobar)
1. Escribe EXCLUSIVAMENTE el código de producción estricto y necesario para que la prueba del paso anterior pase a verde.
2. No sobre-ingeneerices, no preveas casos de uso futuros.
3. Ejecuta de nuevo el test. 
4. **Verifica que pase a verde (`✅`)**.

### 3. REFACTOR (Mejorar Diseño sin Romper)
1. Una vez el test está verde, examina la implementación e higiene del código.
2. Limpia, extrae variables, aplica patrones de diseño si aplica.
3. Vuelve a ejecutar la prueba y verifica que siga verde.

## TIPOS DE PRUEBAS APLICABLES

* **Backend (Java):** Aplica la pirámide; prefiriendo siempre Tests Unitarios (`@ExtendWith(MockitoExtension.class)`) sobre clases de Dominio/Servicio puro, y Test de Integración con `Testcontainers` para los Repositorios y Endpoints.
* **Frontend (Vue/TS):** Usa `vitest` para lógica de estado (Pinia) y utils, y `@vue/test-utils` para renderizado de componentes. Siempre haz tests de las mutaciones y getters antes de implementarlos.

## INSTRUCCIONES OPERATIVAS

Si estás leyendo este skill en un Handoff, TUS PRÓXIMOS EVENTOS de desarrollo deben seguir este esquema:
`[TDD: RED] -> Escribiendo pruebas...`
`[TDD: EJECUCIÓN] -> Evidenciando fallo...`
`[TDD: GREEN] -> Implementando código mínimo...`
`[TDD: EJECUCIÓN] -> Evidenciando pase...`
`[TDD: REFACTOR] -> Reestructurando...`
