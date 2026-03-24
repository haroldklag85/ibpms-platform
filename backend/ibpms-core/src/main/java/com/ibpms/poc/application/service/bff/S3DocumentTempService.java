package com.ibpms.poc.application.service.bff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class S3DocumentTempService {

    private static final Logger log = LoggerFactory.getLogger(S3DocumentTempService.class);

    /**
     * CA-03: Desacoplamiento de Carga Binaria (Upload-First Pattern).
     * Simula la carga de un multipart a S3 y retorna el UUID temporal.
     */
    public String uploadTemporaryDocument() {
        String tempUuid = UUID.randomUUID().toString();
        log.info("S3 Upload-Temp (CA-03): Documento guardado en bóveda transitoria con ID {}", tempUuid);
        
        // Simulación I/O S3
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return tempUuid;
    }
}
