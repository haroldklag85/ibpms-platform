package com.ibpms.poc.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CA-15: DTO que acompaña la respuesta del Workdesk en modo Delegación.
 * Permite al Frontend renderizar el Banner de contexto delegado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelegationContextDTO {
    /** ID del usuario cuyas tareas se están visualizando */
    private String delegatedUserId;

    /** Nombre visible del usuario delegado (para el Banner) */
    private String delegatedUserDisplayName;

    /** Indica si el modo delegación está activo en esta respuesta */
    private boolean delegationActive;
}
