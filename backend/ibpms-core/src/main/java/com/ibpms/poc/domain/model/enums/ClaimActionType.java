package com.ibpms.poc.domain.model.enums;

/**​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
 * CA-20: Tipos de acción de auditoría enriquecidos para Claim/Unclaim.
 * Cada valor representa un evento distinto en el ciclo de vida del claim.
 *
 * <p>Uso: Pasar {@code ClaimActionType.CLAIMED.name()} como String al
 * {@link com.ibpms.poc.application.service.ClaimAuditService#audit} para typesafety
 * sin cambiar la firma del servicio.</p>
 *
 * @see com.ibpms.poc.application.service.ClaimAuditService
 */
public enum ClaimActionType {

    /** Reclamo voluntario individual (CA-01). */
    CLAIMED,

    /** Liberado voluntariamente por el operario (CA-04). Normaliza el antiguo "UNCLAIMED". */
    RELEASED,

    /** Despojo forzoso por supervisor (CA-08/CA-13). Normaliza "FORCE_UNCLAIM" → "FORCE_UNCLAIMED". */
    FORCE_UNCLAIMED,

    /** Auto-unclaim por inactividad (CA-06/CA-15). */
    AUTO_UNCLAIMED,

    /** Extensión de timeout solicitada por el asignado (CA-19). */
    TIMEOUT_EXTENDED,

    /** Reclamada como parte de lote masivo (CA-02/CA-20). */
    BULK_CLAIMED,

    /** Acción denegada por permisos insuficientes (CA-13). */
    DENIED
}
