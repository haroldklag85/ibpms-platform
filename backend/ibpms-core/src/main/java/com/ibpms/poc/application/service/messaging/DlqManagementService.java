package com.ibpms.poc.application.service.messaging;

import com.ibpms.poc.infrastructure.messaging.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio de gestión de la Dead Letter Queue (DLQ) global.
 * Proporciona operaciones de monitoreo, reintento y purgado
 * para el Dashboard técnico de administración (CA-02).
 * <p>
 * @Traceability(US = "US-034", CA = "CA-02")
 */
@Service
public class DlqManagementService {

    private static final Logger log = LoggerFactory.getLogger(DlqManagementService.class);
    private static final int MAX_PEEK_MESSAGES = 50;

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    public DlqManagementService(RabbitTemplate rabbitTemplate, RabbitAdmin rabbitAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
    }

    /**
     * Obtiene un resumen del estado actual de la DLQ global.
     * Incluye el conteo total de mensajes y los más antiguos (hasta 50).
     *
     * @return Mapa con claves "totalMessages" y "oldestMessages".
     */
    public Map<String, Object> getDlqSummary() {
        Map<String, Object> summary = new HashMap<>();

        Properties queueProperties = rabbitAdmin.getQueueProperties(RabbitMQConfig.DLQ_GLOBAL);
        int messageCount = 0;
        if (queueProperties != null) {
            Object countObj = queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            if (countObj != null) {
                messageCount = Integer.parseInt(countObj.toString());
            }
        }
        summary.put("totalMessages", messageCount);

        List<Map<String, Object>> oldestMessages = new ArrayList<>();
        int peekCount = Math.min(messageCount, MAX_PEEK_MESSAGES);
        for (int i = 0; i < peekCount; i++) {
            Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_GLOBAL, 100);
            if (message == null) break;

            Map<String, Object> msgInfo = new HashMap<>();
            msgInfo.put("body", new String(message.getBody()));
            MessageProperties props = message.getMessageProperties();
            if (props != null) {
                msgInfo.put("messageId", props.getMessageId());
                msgInfo.put("receivedRoutingKey", props.getReceivedRoutingKey());
                msgInfo.put("timestamp", props.getTimestamp());
                msgInfo.put("xDeath", props.getXDeathHeader());
            }
            oldestMessages.add(msgInfo);

            // Re-encolar el mensaje para no perderlo durante el peek
            rabbitTemplate.send(RabbitMQConfig.DLQ_GLOBAL, message);
        }
        summary.put("oldestMessages", oldestMessages);
        log.info("[DLQ] Summary: {} mensajes en DLQ global.", messageCount);
        return summary;
    }

    /**
     * Reintenta el procesamiento de mensajes en la DLQ.
     * Los extrae de la DLQ y los reenvía al Exchange original
     * utilizando la información de x-death.
     *
     * @param maxMessages Máximo de mensajes a reintentar. -1 para todos.
     * @return Cantidad de mensajes reenviados.
     */
    public int retryMessages(int maxMessages) {
        int retried = 0;
        int limit = maxMessages <= 0 ? Integer.MAX_VALUE : maxMessages;

        while (retried < limit) {
            Message message = rabbitTemplate.receive(RabbitMQConfig.DLQ_GLOBAL, 100);
            if (message == null) break;

            String originalExchange = extractOriginalExchange(message);
            String originalRoutingKey = extractOriginalRoutingKey(message);

            rabbitTemplate.send(originalExchange, originalRoutingKey, message);
            retried++;
            log.info("[DLQ] Retry #{}: reenvío a exchange={}, routingKey={}", retried, originalExchange, originalRoutingKey);
        }

        log.info("[DLQ] Retry completado. Total reenviados: {}", retried);
        return retried;
    }

    /**
     * Purga todos los mensajes de la DLQ global.
     * Operación destructiva — requiere rol ADMIN_IT o SUPER_ADMIN.
     *
     * @return Cantidad de mensajes purgados (o null si la cola no existía).
     */
    public int purge() {
        rabbitAdmin.purgeQueue(RabbitMQConfig.DLQ_GLOBAL);
        log.warn("[DLQ] Cola {} purgada completamente.", RabbitMQConfig.DLQ_GLOBAL);
        return 0;
    }

    private String extractOriginalExchange(Message message) {
        MessageProperties props = message.getMessageProperties();
        if (props != null && props.getXDeathHeader() != null && !props.getXDeathHeader().isEmpty()) {
            Map<String, ?> xDeath = props.getXDeathHeader().get(0);
            Object exchange = xDeath.get("exchange");
            if (exchange != null) return exchange.toString();
        }
        return RabbitMQConfig.TOPIC_EXCHANGE;
    }

    private String extractOriginalRoutingKey(Message message) {
        MessageProperties props = message.getMessageProperties();
        if (props != null && props.getXDeathHeader() != null && !props.getXDeathHeader().isEmpty()) {
            Map<String, ?> xDeath = props.getXDeathHeader().get(0);
            Object routingKeys = xDeath.get("routing-keys");
            if (routingKeys instanceof List<?> keys && !keys.isEmpty()) {
                return keys.get(0).toString();
            }
        }
        return "";
    }
}
