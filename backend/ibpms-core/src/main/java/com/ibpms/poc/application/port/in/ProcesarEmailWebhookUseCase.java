package com.ibpms.poc.application.port.in;

public interface ProcesarEmailWebhookUseCase {

    /**
     * Procesa un webhook simulado de Mensajería Push desde MS Graph,
     * determinando si se debe instanciar un BP PQR o Demanda, enriqueciendo
     * con IA y CRM.
     * 
     * @param subject     Asunto del correo
     * @param body        Cuerpo o texto enriquecido enviado
     * @param senderEmail Dirección remitente para VIP check
     */
    void procesarEmail(String subject, String body, String senderEmail);
}
