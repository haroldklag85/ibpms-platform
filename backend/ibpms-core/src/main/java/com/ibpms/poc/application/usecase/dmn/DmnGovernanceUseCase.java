package com.ibpms.poc.application.usecase.dmn;

import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@SuppressWarnings("null")
public class DmnGovernanceUseCase {

    private static final Logger log = LoggerFactory.getLogger(DmnGovernanceUseCase.class);
    private final DmnModelRepository dmnRepository;

    public DmnGovernanceUseCase(DmnModelRepository dmnRepository) {
        this.dmnRepository = dmnRepository;
    }

    /**
     * CA-06: Inmutabilidad DMN BOLA/IDOR Protection.
     * Actualiza el XML de un DMN, SOLO si no está SELLADO y SOLO si el Tenant coincide.
     */
    @Transactional
    public DmnModelEntity updateDmnContent(String dmnId, String newXml, String invokerTenantId) {
        DmnModelEntity dmn = dmnRepository.findById(dmnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DMN Model not found"));

        if (!dmn.getTenantId().equals(invokerTenantId)) {
            log.error("[APPSEC-BOLA] Tenant Mismatch. {} intentó vulnerar o editar DMN de Tenant {}.", invokerTenantId, dmn.getTenantId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso Cruzado BOLA (Broken Object Level Authorization) detectado y bloqueado.");
        }

        if ("SEALED".equals(dmn.getStatus())) {
            log.warn("[APPSEC-IMMUTABLE] Intento de alteración (PUT/PATCH) sobre un DMN '{}' en estado SEALED.", dmnId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El DMN se encuentra Aprobado (SEALED). Es técnicamente inmutable. Para aplicar cambios, emita una Versión 2.");
        }

        dmn.setXmlContent(newXml);
        return dmnRepository.save(dmn);
    }

    /**
     * CA-12: Rollback Efímero del Copiloto AI.
     * Si la IA o el humano arruinan la tabla en estado DRAFT, este método aborta el estado y revierte.
     */
    @Transactional
    public void rollbackDraft(String dmnId, String invokerTenantId) {
        DmnModelEntity dmn = dmnRepository.findById(dmnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DMN Model not found"));

        if (!dmn.getTenantId().equals(invokerTenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant Mismatch.");
        }

        if ("SEALED".equals(dmn.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede hacer rollback de un modelo SEALED/Activo.");
        }

        log.info("[SRE-ROLLBACK] Eliminando el Draft {} y revirtiendo el canvas al estado estable V1.", dmnId);
        // Estrategia: Destruir la fila DRAFT. El frontend cargará de nuevo el SEALED con el id padre.
        // Asumiendo que V2_DRAFT es una fila aparte. O en este caso (V1 MOC) simplemente lo borramos.
        dmnRepository.delete(dmn);
    }

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${camunda.bpm.client.base-url:http://localhost:8080/engine-rest}")
    private String camundaBaseUrl;

    /**
     * Consulta la API REST de Camunda 7 (/engine-rest/decision-definition)
     * filtrando por tenant y retornando id, key, name, version, deploymentId.
     */
    public java.util.List<com.ibpms.poc.application.dto.DmnDefinitionDto> listDeployedDecisionDefinitions(String tenantId) {
        String url = camundaBaseUrl + "/decision-definition?tenantIdIn=" + tenantId + "&latestVersion=true";
        try {
            // Se asume el contrato Array JSON nativo de Camunda 7 REST API
            com.ibpms.poc.application.dto.DmnDefinitionDto[] definitions = restTemplate.getForObject(url, com.ibpms.poc.application.dto.DmnDefinitionDto[].class);
            if (definitions != null) {
                return java.util.Arrays.asList(definitions);
            }
        } catch (Exception e) {
            log.error("[DMN CATALOG ERROR] Error invocando al engine-rest: {}", e.getMessage());
        }
        return java.util.Collections.emptyList();
    }
}
