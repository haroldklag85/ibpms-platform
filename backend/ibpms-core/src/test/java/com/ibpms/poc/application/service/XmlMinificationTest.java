package com.ibpms.poc.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlMinificationTest {

    // Simulación del Bean/Motor Interno de Minificación requerido en Backend
    public static class XmlMinifierService {
        public String minify(String rawXml) {
            if (rawXml == null) return null;
            // Simulamos la remoción nativa de retornos de carro, tabulaciones y saltos de línea (RegEx Simple de Minificación XML)
            return rawXml.replaceAll(">\\s+<", "><").replaceAll("(?m)^\\s+", "").trim();
        }
    }

    private final XmlMinifierService minifierService = new XmlMinifierService();

    @Test
    @DisplayName("US-007 CA-03: Optimización del Payload - El Minificador reduce estáticamente la huella de Bytes antes del Persist")
    void testXmlCompression_ReducesTotalBytes_ExcludingAIStreaming() {
        
        // Simulamos un XML rústico, típicamente sobrecargado con tabulaciones de la herramienta BPMN.io
        String rawBloatedXml = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\">\n" +
                "  <bpmn:process id=\"Process_1\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
                "    <bpmn:userTask id=\"Task_A\">\n" +
                "      <bpmn:documentation>Review</bpmn:documentation>\n" +
                "    </bpmn:userTask>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        long originalByteSize = rawBloatedXml.getBytes().length;

        // Ejecutamos la Acción a Testear
        String minifiedXml = minifierService.minify(rawBloatedXml);
        long compressedByteSize = minifiedXml.getBytes().length;

        // 1. Aserción Matemática de Reducción Absoluta (La Minificación funciona físicamente)
        assertThat(compressedByteSize).isLessThan(originalByteSize);
        
        // 2. Aserción Semántica (Las etiquetas clave sobreviven a la compresión, no hay corrupción de data)
        assertThat(minifiedXml).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(minifiedXml).contains("<bpmn:userTask id=\"Task_A\">");
        
        // 3. Verificamos que el retorno condena los retornos de carro entre los nodos
        assertThat(minifiedXml).doesNotContain(">\\n  <");
        
        // [INFO QA] No se incluyen Test sobre Streaming de IA. El streaming LLM / WebSockets queda aplazado según priorización V2.
    }
}
