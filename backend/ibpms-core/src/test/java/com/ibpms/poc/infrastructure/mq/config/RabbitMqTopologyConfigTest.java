package com.ibpms.poc.infrastructure.mq.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMqTopologyConfigTest {

    private final RabbitMqTopologyConfig topologyConfig = new RabbitMqTopologyConfig();

    @Test
    void testPrimaryExchangesAreConfigured() {
        TopicExchange topicExchange = topologyConfig.primaryExchange();
        assertThat(topicExchange.getName()).isEqualTo(RabbitMqTopologyConfig.TOPIC_EXCHANGE);

        DirectExchange dlxExchange = topologyConfig.dlxExchange();
        assertThat(dlxExchange.getName()).isEqualTo(RabbitMqTopologyConfig.DLX_EXCHANGE);
    }

    @Test
    void testGlobalDlqHasCorrectTtl() {
        Queue dlqQueue = topologyConfig.dlqGlobalQueue();
        assertThat(dlqQueue.getName()).isEqualTo(RabbitMqTopologyConfig.DLQ_GLOBAL);
        assertThat(dlqQueue.getArguments()).containsEntry("x-message-ttl", 2592000000L);
    }

    @Test
    void testBusinessQueuesHaveDeadLetterExchange() {
        Queue notificationsQueue = topologyConfig.notificationsQueue();
        assertHasDlqConfig(notificationsQueue);

        Queue aiQueue = topologyConfig.aiGenerationQueue();
        assertHasDlqConfig(aiQueue);

        Queue integrationsQueue = topologyConfig.integrationsWebhookQueue();
        assertHasDlqConfig(integrationsQueue);

        Queue bpmnQueue = topologyConfig.bpmnEventsQueue();
        assertHasDlqConfig(bpmnQueue);

        Queue taskRescueQueue = topologyConfig.taskRescueNewQueue();
        assertHasDlqConfig(taskRescueQueue);
    }

    private void assertHasDlqConfig(Queue queue) {
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMqTopologyConfig.DLX_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", "dlq.route");
    }

    @Test
    void testTopologyDocumentationExists() {
        // Asumiendo que el test corre desde la raiz del microservicio o proyecto multi-modulo
        File docFile1 = new File("../../../docs/architecture/rabbitmq_topology.md");
        File docFile2 = new File("../../docs/architecture/rabbitmq_topology.md");
        File docFile3 = new File("../docs/architecture/rabbitmq_topology.md");
        File docFile4 = new File("docs/architecture/rabbitmq_topology.md");
        
        boolean exists = docFile1.exists() || docFile2.exists() || docFile3.exists() || docFile4.exists();
        assertThat(exists).as("El archivo rabbitmq_topology.md debe existir en docs/architecture/").isTrue();
    }
}
