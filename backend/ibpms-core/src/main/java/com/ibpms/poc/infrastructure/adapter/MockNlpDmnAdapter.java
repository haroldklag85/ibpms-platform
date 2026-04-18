package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.dto.DmnXmlResponseDto;
import com.ibpms.poc.application.dto.NlpPromptRequestDto;
import com.ibpms.poc.application.port.out.AiDmnGeneratorPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * ADR: Mock Strategy.
 * Se utiliza @ConditionalOnProperty en lugar de @Profile para permitir dinamismo
 * en entornos controlados y aislar explícitamente el uso de IA. Si la propiedad
 * ibpms.ai.mock-enabled es true (o no se declara), se monta el Mock como fallback.
 */
@Service
@ConditionalOnProperty(name = "ibpms.ai.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockNlpDmnAdapter implements AiDmnGeneratorPort {

    @Override
    public DmnXmlResponseDto generateDmnFromPrompt(NlpPromptRequestDto request) {
        String mockXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions id="mock-dmn-01" name="Mock DMN" namespace="http://camunda.org/schema/1.0/dmn">
                  <decision id="mock_decision" name="Aprobacion Automatica">
                    <decisionTable id="decisionTable_mock">
                      <input id="input_1" label="Monto">
                        <inputExpression id="inputExpression_1" typeRef="integer">
                          <text>monto</text>
                        </inputExpression>
                      </input>
                      <output id="output_1" label="Resultado" name="resultado" typeRef="string" />
                      <rule id="DecisionRule_mock_1">
                        <inputEntry id="UnaryTests_mock_1">
                          <text>&lt; 1000</text>
                        </inputEntry>
                        <outputEntry id="LiteralExpression_mock_1">
                          <text>"APROBADO"</text>
                        </outputEntry>
                      </rule>
                    </decisionTable>
                  </decision>
                </definitions>
                """;

        return new DmnXmlResponseDto(mockXml, 0.95, "Generado vía Mock Regex Rule-based");
    }
}
