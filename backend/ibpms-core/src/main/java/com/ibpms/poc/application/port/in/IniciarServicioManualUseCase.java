package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.ManualStartDTO;
import java.util.UUID;

/**
 * Puerto de Entrada: Iniciar un Servicio o Expediente Manualmente (Intake
 * Admin).
 */
public interface IniciarServicioManualUseCase {

    /**
     * Orquesta la creación del expediente físico y su proceso asociado en Camunda
     * BPM.
     * 
     * @param request Datos de inicialización
     * @return El UUID del Expediente creado.
     */
    UUID iniciarServicio(ManualStartDTO request);
}
