package com.ibpms.poc.application.port.out;

/**
 * Puerto de Salida: Cliente CRM (ej: Dynamics 365 / Salesforce).
 * Expone los metadatos de cliente que el dominio necesita para enriquecer
 * el expediente. La implementación usará WebClient (HTTP/REST).
 * Indicado en el handoff como requerimiento del Copilot M365.
 */
public interface CrmClientPort {

    /**
     * Obtiene el nombre completo del cliente desde el CRM.
     * 
     * @param clienteId identificador externo del cliente
     * @return nombre completo o null si no existe
     */
    String obtenerNombreCliente(String clienteId);

    /**
     * Obtiene metadatos completos del cliente (email, teléfono, segmento, etc.).
     * 
     * @param clienteId identificador externo del cliente
     * @return mapa de metadatos; puede estar vacío pero nunca null
     */
    java.util.Map<String, Object> obtenerMetadatosCliente(String clienteId);

    /**
     * Valida mediante el correo electrónico o dominio si el cliente
     * pertenece a la categoría VIP, dictaminando rutas prioritarias.
     * 
     * @param email Correo electrónico extraído del Inbound Webhook
     * @return true si es VIP, false si es cliente estándar
     */
    boolean checkVipStatus(String email);
}
