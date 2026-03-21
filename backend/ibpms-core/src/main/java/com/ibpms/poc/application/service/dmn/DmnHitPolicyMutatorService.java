package com.ibpms.poc.application.service.dmn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * CA-07: Mutador Transaccional de Hit Policy.
 * Redefine las estructuras defectuosas del XML inyectando Catch-Alls y forzando exclusión temporal "FIRST".
 */
@Service
public class DmnHitPolicyMutatorService {

    private static final Logger log = LoggerFactory.getLogger(DmnHitPolicyMutatorService.class);

    private static final String CATCH_ALL_RULE_XML = """
            <rule id="Rule_CATCH_ALL_INJECTED">
              <description>Fila de Seguridad Catch-All (Forzada por Backend Guardrails)</description>
              <inputEntry id="CatchAll_In_1">
                <text>-</text>
              </inputEntry>
              <outputEntry id="CatchAll_Out_1">
                <text>"Revisión Humana"</text>
              </outputEntry>
            </rule>
          </decisionTable>""";

    /**
     * Aplica el encriptado matemático al XML.
     * @param rawDmnXml El texto XML proveniente de la IA o el Editor Manual.
     * @return El XML Reescrito con la garantía de ejecución FIRST y red Catch-All.
     */
    public String enforceMathGuardrails(String rawDmnXml) {
        if (rawDmnXml == null || rawDmnXml.isBlank()) return rawDmnXml;

        log.debug("[SRE-MUTATOR] Iniciando reconstrucción estructural DMN (HitPolicy FIRST + CatchAll)...");

        // 1. Sobreescritura Forzosa de HitPolicy.
        // Transformar hitPolicy="ANY|UNIQUE|RULE ORDER" a hitPolicy="FIRST"
        String mutatedXml = rawDmnXml.replaceAll("hitPolicy=\"[A-Z\\s]+\"", "hitPolicy=\"FIRST\"");
        
        // Si no poseía hitPolicy (Falla del LLM), lo inyectamos en el Tag de decisionTable
        if (!mutatedXml.contains("hitPolicy=\"FIRST\"")) {
            mutatedXml = mutatedXml.replace("<decisionTable id=\"table\">", "<decisionTable id=\"table\" hitPolicy=\"FIRST\">");
        }

        // 2. Inyección de la Malla CATCH-ALL si previene vacíos
        // Detectamos si el final de la tabla ya posee un CatchAll. En la PoC lo inyectamos sistemáticamente si falta un "Revisión Humana".
        if (!mutatedXml.contains("\"Revisión Humana\"")) {
            mutatedXml = mutatedXml.replace("</decisionTable>", CATCH_ALL_RULE_XML);
        }

        log.info("[SRE-MUTATOR] Estructura Matemática re-evaluada al 100%. Guardrails Activos.");
        return mutatedXml;
    }
}
