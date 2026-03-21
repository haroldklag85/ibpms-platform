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
}
