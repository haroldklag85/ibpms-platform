import { describe, it, expect } from 'vitest';
import { sanitizeDmnXml } from '@/utils/security';

describe('Sanitización XSS en DMN XML (CA-4)', () => {

    it('Mantiene etiquetas válidas de DMN/BPMN intactas', () => {
        const rawXml = `<?xml version="1.0" encoding="UTF-8"?>
        <definitions id="mock">
           <decision id="decision1">
              <input id="in1">
                 <text>true</text>
              </input>
           </decision>
        </definitions>`;
        
        const sanitized = sanitizeDmnXml(rawXml);
        expect(sanitized).toContain('<definitions id="mock">');
        expect(sanitized).toContain('<decision id="decision1">');
        expect(sanitized).toContain('<input id="in1">');
        expect(sanitized).toContain('<text>true</text>');
    });

    it('Elimina etiquetas de script maliciosas', () => {
        const rawXml = `<?xml version="1.0" encoding="UTF-8"?>
        <definitions id="mock">
           <script>alert("XSS")</script>
        </definitions>`;
        
        const sanitized = sanitizeDmnXml(rawXml);
        expect(sanitized).not.toContain('<script>');
        expect(sanitized).not.toContain('alert');
    });

    it('Purga atributos con handlers de eventos (onerror, onclick)', () => {
        const rawXml = `<?xml version="1.0" encoding="UTF-8"?>
        <definitions id="mock" onerror="alert(1)" onclick="stealToken()">
           <decision id="decision1"></decision>
        </definitions>`;
        
        const sanitized = sanitizeDmnXml(rawXml);
        expect(sanitized).not.toContain('onerror');
        expect(sanitized).not.toContain('onclick');
        expect(sanitized).toContain('<definitions id="mock">');
    });

    it('Sanitiza inyecciones URI javascript:', () => {
        const rawXml = `<?xml version="1.0" encoding="UTF-8"?>
        <definitions id="mock" href="javascript:alert('XSS')">
           <decision id="decision1"></decision>
        </definitions>`;
        
        const sanitized = sanitizeDmnXml(rawXml);
        // Debe eliminar o desarmar el javascript:
        expect(sanitized).not.toContain('javascript:alert');
    });
});
