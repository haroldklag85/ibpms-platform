import DOMPurify from 'dompurify';

/**
 * CA-04: Transversal XML/DMN Sterilizer
 * Utiliza DOMPurify de forma quirúrgica sobre los atributos y nodos de texto
 * de un documento XML estructurado. Esto previene la inyección de vectores XSS (ej: tags <script>, atributos onerror)
 * generados por alucinaciones del Agente IA, sin destruir los namespaces propietarios de BPMN/DMN (camunda:, biodi:, etc).
 */
export const sanitizeDmnXml = (xmlString: string): string => {
    if (!xmlString) return '';

    try {
        const parser = new DOMParser();
        const doc = parser.parseFromString(xmlString, 'application/xml');

        // Check for XML parse errors
        const parseError = doc.querySelector('parsererror');
        if (parseError) {
            console.error("XML Parser Error during sanitization", parseError.textContent);
            // Return raw string sanitized completely via DOMPurify as fallback, which might destroy custom tags
            // but prevents XSS strictly at the cost of functionality if XML is malformed.
            return DOMPurify.sanitize(xmlString);
        }

        const sanitizeNode = (node: Node) => {
            if (node.nodeType === Node.ELEMENT_NODE) {
                const el = node as Element;
                
                // 1. Purge destructive HTML tags explicitly injected by rogue AI
                const tagName = el.tagName.toLowerCase();
                if (['script', 'iframe', 'object', 'embed', 'style', 'link', 'meta'].includes(tagName)) {
                    el.remove();
                    return;
                }
                
                // 2. Sterilize attributes using DOMPurify text-only profile
                // and explicitly removing event handlers/javascript payloads
                Array.from(el.attributes).forEach(attr => {
                    const attrName = attr.name.toLowerCase();
                    const attrVal = attr.value.toLowerCase();
                    
                    if (attrName.startsWith('on') || attrVal.includes('javascript:') || attrVal.includes('data:text/html')) {
                        el.removeAttribute(attr.name);
                    } else {
                        // Limpieza quirúrgica DOMPurify para el valor (Text-Only)
                        attr.value = DOMPurify.sanitize(attr.value, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
                    }
                });

                // Traverse deeper
                Array.from(el.childNodes).forEach(child => sanitizeNode(child));
            } else if (node.nodeType === Node.TEXT_NODE) {
                if (node.nodeValue) {
                    // Sanitize inner text content
                    node.nodeValue = DOMPurify.sanitize(node.nodeValue, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
                }
            } else if (node.nodeType === Node.CDATA_SECTION_NODE) {
                if (node.nodeValue) {
                    node.nodeValue = DOMPurify.sanitize(node.nodeValue, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
                }
            }
        };

        sanitizeNode(doc.documentElement);

        const serializer = new XMLSerializer();
        return serializer.serializeToString(doc);

    } catch (e) {
        console.warn("Fallo catastrófico en esterilizador XML. Aplicando barrera DOMPurify absoluta.", e);
        return DOMPurify.sanitize(xmlString);
    }
};
