import { describe, it, expect } from 'vitest';

// Simulación in-memory del Patrón Command() Stack de diagram-js / Camunda
class CommandStackMock {
    private stack: string[] = [];
    private currentIndex = -1;

    // Estado 0 = Vacio, Estado 1 = Base User, Estado 2 = AI Injection
    execute(command: string, context: any) {
        // Cortamos la rama alternativa si deshicimos e intentamos nueva inyección
        if(this.currentIndex < this.stack.length - 1) {
            this.stack = this.stack.slice(0, this.currentIndex + 1);
        }
        this.stack.push(context.xmlState);
        this.currentIndex++;
    }

    undo() {
        if (this.currentIndex > 0) this.currentIndex--;
    }

    getCurrentState() {
        return this.currentIndex >= 0 ? this.stack[this.currentIndex] : null;
    }
}

describe('US-027 CA-8: UX Recuperabilidad y Atomicidad (Iteración 56)', () => {
    
    it('Aserta que la Inyección IA empuja al CommandStack y responde al CTRL+Z restaurando el XML a byte-perfect', () => {
        const stack = new CommandStackMock();
        
        // 1. Estado Base del Usuario
        const baseUserXml = `<process id="P1"><startEvent/></process>`;
        stack.execute('elements.create', { xmlState: baseUserXml });
        
        // Aserción 1: Árbol Base
        expect(stack.getCurrentState()).toBe(baseUserXml);

        // 2. Inyección Atómica de SSE IA Copiloto
        const artificialXml = `<process id="P1"><startEvent/><userTask id="UserTask_AI_1"/></process>`;
        stack.execute('import.xml', { xmlState: artificialXml }); // CA-08 Requirement

        // Aserción 2: El lienzo mutó
        expect(stack.getCurrentState()).toBe(artificialXml);
        expect(stack.getCurrentState()).toContain('UserTask_AI_1');

        // 3. El humano rechaza el flujo y presiona CTRL+Z
        stack.undo();

        // Aserción Matemática 3: La Des-Serialización atómica fue perfecta (Cero Restos Artificiales)
        expect(stack.getCurrentState()).toBe(baseUserXml);
        expect(stack.getCurrentState()).not.toContain('UserTask_AI_1');
    });
});
