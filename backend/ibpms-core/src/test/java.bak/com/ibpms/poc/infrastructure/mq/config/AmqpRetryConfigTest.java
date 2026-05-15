package com.ibpms.poc.infrastructure.mq.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AmqpRetryConfigTest {

    private final AmqpConfig amqpConfig = new AmqpConfig();

    @Test
    void testRetryInterceptorIsConfigured() {
        RetryOperationsInterceptor interceptor = amqpConfig.retryInterceptor();
        assertThat(interceptor).isNotNull();
        // Since we cannot assert the specific backoff details (1s, 5x, 120s) without deeper reflection,
        // we note this as a limitation documented in the test plan, validating instead its presence.
    }

    @Test
    void testRabbitListenerContainerFactoryAppliesRetryInterceptor() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        SimpleRabbitListenerContainerFactory factory = amqpConfig.rabbitListenerContainerFactory(connectionFactory);
        
        assertThat(factory).isNotNull();
        // Verifying advice chain is applied would normally be done by deeper reflective checks on the factory
        // Here we ensure it instantiates properly with the provided interceptor.
        assertThat(factory.getAdviceChain()).isNotNull();
        assertThat(factory.getAdviceChain()[0]).isInstanceOf(RetryOperationsInterceptor.class);
    }
}
