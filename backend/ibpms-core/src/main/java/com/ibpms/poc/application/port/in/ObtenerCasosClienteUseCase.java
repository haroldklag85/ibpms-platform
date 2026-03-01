package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.Customer360DTO;

/**
 * Puerto de Entrada: Consultar casos consolidados 360 de un cliente.
 */
public interface ObtenerCasosClienteUseCase {
    Customer360DTO obtenerVista360(String crmId);
}
