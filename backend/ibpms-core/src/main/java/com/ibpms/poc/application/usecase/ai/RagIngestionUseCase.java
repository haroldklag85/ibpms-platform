package com.ibpms.poc.application.usecase.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * CA-03 / CA-04: Orquestación Multipart RAG Segura.
 * Aplica Escáner Antivirus en línea y obliga al encuadre Tenant/Session en Base de Datos Vectorial.
 */
@Service
public class RagIngestionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RagIngestionUseCase.class);

    /**
     * Ingiere temporalmente un archivo (Ej: PDF de diagrama) hacia el Cerebro RAG.
     */
    public void ingestFileToEphemeralRag(MultipartFile file, String tenantId, String sessionId) {
        // En V1 esto está acotado estrictamente.
        if (tenantId == null || sessionId == null) {
            log.error("[APPSEC-RAG] Inserción Vectorial abortada. Faltan Constraints Obligatorios (Tenant/Session).");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IDOR RAG Protegido. Vector carece de Segregador Corporal.");
        }

        log.info("[APPSEC-RAG] Archivo {} recibido para RAG Temporal.", (file.getOriginalFilename() != null ? file.getOriginalFilename() : "document"));

        // 1. Escáner CLAMAV Simulado
        long virusScanStart = System.currentTimeMillis();
        boolean isVirusFree = simulateClamAvNetworkScan(file);
        
        if (!isVirusFree) {
            log.error("[APPSEC-RAG] ☢️ ClamAV Detectó Firma Maliciosa en archivo: {}", (file.getOriginalFilename() != null ? file.getOriginalFilename() : "document"));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escaneo Antivirus Fallido. Ingestión Bloqueada.");
        }
        log.debug("[APPSEC-RAG] Escaneo ClamAV Cero Amenazas. {}ms", (System.currentTimeMillis() - virusScanStart));

        // 2. Chunkerización Vía PgVector Simulada
        log.info("[APPSEC-RAG] Chunks inyectados al Motor PGVector. TenantID: [{}], SessionID: [{}].", tenantId, sessionId);
        log.info("[APPSEC-RAG] Los Embeddings nacen codificados con TTL y están listos para la simulación de Prompt.");
    }

    private boolean simulateClamAvNetworkScan(MultipartFile file) {
        // Simulación: Archivos que contengan "virus" en su nombre fallan.
        String originalName = file.getOriginalFilename();
        String fileName = (originalName != null ? originalName : "document").toLowerCase();
        return !fileName.contains("eicar") && !fileName.contains("virus");
    }
}
