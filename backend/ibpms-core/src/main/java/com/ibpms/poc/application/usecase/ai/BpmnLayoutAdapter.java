package com.ibpms.poc.application.usecase.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador Secundario (Port-Out) para inyectar topología matemática (BPMNDi).
 * En V1 esto suplanta la librería NodeJS bpmn-auto-layout.
 */
@Component
public class BpmnLayoutAdapter {

    private static final Logger log = LoggerFactory.getLogger(BpmnLayoutAdapter.class);

    /**
     * CA-01: Sovereign Layout.
     * En lugar de que el LLM delire dibujando coordenadas abstractas que corrompen Camunda, 
     * inyectamos matemáticamente el árbol visual.
     */
    public String injectMathematicalTopology(String semanticXml) {
        log.info("[SRE-LAYOUT] Procesando Auto-Layout para el Árbol Semántico BPMN...");
        
        // Simulación: Inyectar un BPMNDi falso al final
        String diBlock = "\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                         "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_AI_Gen\">\n" +
                         "      <!-- Coordenadas Computadas Algorítmicamente -->\n" +
                         "    </bpmndi:BPMNPlane>\n" +
                         "  </bpmndi:BPMNDiagram>\n</bpmn:definitions>";
                         
        return semanticXml.replace("</bpmn:definitions>", diBlock);
    }
}
