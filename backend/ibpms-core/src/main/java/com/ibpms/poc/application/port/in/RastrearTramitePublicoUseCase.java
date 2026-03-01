package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.PublicTrackingDTO;

/**
 * Puerto de Entrada: Rastrear el estado de un trámite de forma pública y
 * anónima.
 */
public interface RastrearTramitePublicoUseCase {

    /**
     * Consulta el estado de un expediente utilizando su código de rastreo (Business
     * Key).
     * 
     * @param trackingCode El código de rastreo entregado al ciudadano/cliente.
     * @return DTO limpio sin revelar estructura interna.
     */
    PublicTrackingDTO rastrear(String trackingCode);
}
