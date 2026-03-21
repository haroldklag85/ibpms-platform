package com.ibpms.poc.application.usecase.dmn;

import org.camunda.bpm.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Port-In (UseCase) SRE para gestionar la eficiencia del Parser DMN (CA-03).
 */
@Service
public class DmnSreOptimizationUseCase {

    private static final Logger log = LoggerFactory.getLogger(DmnSreOptimizationUseCase.class);
    private final RepositoryService camundaRepositoryService;

    public DmnSreOptimizationUseCase(RepositoryService camundaRepositoryService) {
        this.camundaRepositoryService = camundaRepositoryService;
    }

    /**
     * Minifica drásticamente el XML eliminando tabulaciones, saltos de línea y 
     * espacios inter-tag (`> <`) pre-Commit (Reducción de I/O de disco).
     */
    public String minifyDmnXml(String rawXml) {
        if (rawXml == null || rawXml.isBlank()) {
            return rawXml;
        }
        
        // Exterminar espacios en blanco y minificar el DOM
        String minified = rawXml.replaceAll("(?m)^\\s+|\\s+$", "") // Trim multiline
                                .replaceAll(">\\s+<", "><")        // Eliminar vacíos entre tags
                                .replaceAll("\\n", "")             // Remover retornos
                                .replaceAll("\\r", "");
                                
        log.info("[SRE-XML] Minificación DMN completada. Compresión aplicada.");
        return minified;
    }

    /**
     * Carga asíncronamente el DMN al árbol de caché AST (Abstract Syntax Tree) del Engine,
     * ahorrando milisegundos de latencia comercial (Cold Start) para la primera petición.
     */
    @Async
    public void triggerColdStartWarmUp(String decisionDefinitionId) {
        try {
            log.info("[SRE-CACHE] Iniciando Warm-Up Asíncrono para Decision ID: {}", decisionDefinitionId);
            
            // Obliga a Camunda a hacer parsing del XML y construir el Evaluator en RAM
            camundaRepositoryService.getDecisionModel(decisionDefinitionId);
            
            log.info("[SRE-CACHE] Warm-Up exitoso. DMN montado en memoria, latencia operativa mitigada.");
            
        } catch (Exception e) {
            log.error("[SRE-CACHE] Fallo al calentar la caché del DMN {}. Se armará On-Demand.", decisionDefinitionId, e);
        }
    }
}
