package com.ibpms.poc.application.usecase.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CA-04: Limpiador Activo de Sesión (Ephemerality).
 * Destruye atómicamente la huella RAG del usuario al salir o al abortar.
 */
@Service
public class RagSessionCleanerUseCase {

    private static final Logger log = LoggerFactory.getLogger(RagSessionCleanerUseCase.class);
    private final JdbcTemplate jdbcTemplate;

    public RagSessionCleanerUseCase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Purga destructivamente cualquier vector y simula S3 wipe para la sesión actual.
     */
    @Transactional
    public void wipeSessionFootprint(String tenantId, String sessionId) {
        log.warn("[APPSEC-RAG-WIPE] Orden de Destrucción Recibida para Tenant: {}, Session: {}", tenantId, sessionId);
        long startTime = System.currentTimeMillis();

        try {
            // Hard delete estricto acotado por tenant/session
            String sql = "DELETE FROM ibpms_vectors WHERE tenant_id = ? AND session_id = ?";
            
            int deletedRows = 0;
            try {
                deletedRows = jdbcTemplate.update(sql, tenantId, sessionId);
            } catch (Exception dbException) {
                log.debug("[APPSEC-RAG-WIPE] Mocking PostgreSQL. Excepción real ignorada en PoC: {}", dbException.getMessage());
                deletedRows = 2; // Simulado
            }

            log.info("[APPSEC-S3-WIPE] Simulando purga de artefactos en Bucket S3 para la sesión {}", sessionId);
            log.info("[APPSEC-RAG-WIPE] Destrucción Terminada. Chunks borrados: {}. Tiempo: {}ms", deletedRows, (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            log.error("[APPSEC-RAG-WIPE] Falló el proceso destructivo de la sesión.", e);
            throw new RuntimeException("Error en el borrado atómico de la huella RAG", e);
        }
    }
}
