package com.ibpms.poc.application.usecase.dmn;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CA-11: Simulador DMN Estéril (Dry-Run).
 * Ejecuta el XML DMN en una "Caja de Arena" (Sandbox) de memoria RAM independiente,
 * sin impactar la base de datos ni el histórico de Camunda.
 */
@Service
public class DmnSimulatorUseCase {

    private static final Logger log = LoggerFactory.getLogger(DmnSimulatorUseCase.class);

    // Motor aislado sin configuración de Base de Datos
    private final DmnEngine standaloneEngine;

    public DmnSimulatorUseCase() {
        this.standaloneEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
    }

    /**
     * Parsea el XML crudo entrante y lo somete al DmnEngine usando variables dummy.
     */
    public Object simulateDmnEvaluation(String rawXml, Map<String, Object> dummyVariables) {
        log.info("[SRE-SIMULATOR] Iniciando Evaluación Dry-Run sobre Motor Camunda Aislado");
        
        try (InputStream dmnStream = new ByteArrayInputStream(rawXml.getBytes(StandardCharsets.UTF_8))) {
            List<DmnDecision> decisions = standaloneEngine.parseDecisions(dmnStream);
            
            if (decisions.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El XML no contiene ningún Tag <decision> válido.");
            }

            // Para la PoC, evaluamos la primera decisión encontrada
            DmnDecision targetDecision = decisions.get(0);
            
            // Evaluar los inputs vs la lógica de la tabla
            DmnDecisionTableResult evaluationResult = standaloneEngine.evaluateDecisionTable(targetDecision, dummyVariables);
            
            log.info("[SRE-SIMULATOR] Simulación Exitosa. Result: {}", evaluationResult.getResultList());

            // Devolver las salidas crudas (Reglas matched u outputs) al FrontEnd
            return evaluationResult.getResultList();

        } catch (Exception e) {
            log.error("[SRE-SIMULATOR] Error durante el Dry-Run: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulación Fallida: " + e.getMessage());
        }
    }
}
