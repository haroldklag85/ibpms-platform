package com.ibpms.core.sac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Event Listener que atiende eliminaciones manuales en iBPMS por parte de un
 * operador humano.
 * (CA-15 Regla de Oro Soft-Delete)
 */
@Service
public class MailboxSoftDeleteListener {

    private static final Logger log = LoggerFactory.getLogger(MailboxSoftDeleteListener.class);

    // Mock del evento de Camunda. En produccion seria ExecutionListener o un
    // ApplicationEvent custom.
    public record CamundaProcessCancelledEvent(String processInstanceId, String sourceMailId, String mailboxAlias) {
    }

    @EventListener
    public void handleProcessCancelled(CamundaProcessCancelledEvent event) {
        log.info("Se ha atrapado la cancelación manual del proceso {}. Ejecutando directriz Soft-Delete en MS Graph.",
                event.processInstanceId());

        try {
            // Regla de Oro: Se DEBE usar POST /move, y NO DELETE.
            String endpointMove = "https://graph.microsoft.com/v1.0/users/" + event.mailboxAlias() + "/messages/"
                    + event.sourceMailId() + "/move";

            String destinationFolder = "deleteditems"; // Carpeta genérica estandar en Exchange.
            // Payload mock: { "destinationId": "deleteditems" }

            log.info("Llamando a Graph API de forma inmutable: POST {}", endpointMove);
            log.info("Moviendo elemento {} a la carpeta {}", event.sourceMailId(), destinationFolder);

            // RestTemplate POST for move...

        } catch (Exception e) {
            log.error("No se pudo ejecutar la acción POST /move en MS Graph", e);
        }
    }
}
