package com.ibpms.poc.application.port.out;

/**
 * Puerto de Salida: Cliente MS Graph API.
 * Permite generar borradores y comunicaciones en Microsoft 365 desde el
 * dominio.
 * La implementación usará WebClient + OAuth2 Client Credentials hacia Graph
 * API.
 * Requerimiento indicado en el handoff por Copilot M365.
 */
public interface MsGraphClientPort {

    /**
     * Genera un borrador de correo electrónico en el buzón de servicio.
     * 
     * @param destinatario dirección email del destinatario
     * @param asunto       línea de asunto del correo
     * @param cuerpoHtml   cuerpo en HTML del borrador
     * @return messageId de Graph API del borrador creado
     */
    String generarBorradorEmail(String destinatario, String asunto, String cuerpoHtml);

    /**
     * Crea un evento en el calendario de equipo asociado al expediente.
     * 
     * @param expedienteId ID del expediente (para el asunto del evento)
     * @param fechaIso     fecha en formato ISO-8601
     * @param descripcion  descripción del evento
     * @return eventId de Graph API del evento creado
     */
    String crearEventoCalendario(String expedienteId, String fechaIso, String descripcion);
}
