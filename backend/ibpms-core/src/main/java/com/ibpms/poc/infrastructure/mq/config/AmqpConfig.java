package com.ibpms.poc.infrastructure.mq.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class AmqpConfig {

    @Bean
    public ConditionalRejectingErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(
            new ConditionalRejectingErrorHandler.DefaultExceptionStrategy() {
                @Override
                protected boolean isUserCauseFatal(Throwable cause) {
                    // CA-7: Clasificación Permanente vs Transitorio. (Rechazar permanentes al DLQ sin reintento)
                    if (cause instanceof IllegalArgumentException || cause instanceof IllegalStateException || cause instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
                        return true;
                    }
                    return super.isUserCauseFatal(cause);
                }
            }
        );
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(4)
                .backOffOptions(1000, 5.0, 120000) // Initial 1s, multiplier 5, max 2m (e.g. 1s -> 5s -> 25s -> ~120s)
                .recoverer(new RejectAndDontRequeueRecoverer()) // Rejected to DLQ
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory p1ContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1); // CA-6: Latencia Crítica
        factory.setAdviceChain(retryInterceptor()); 
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory p2ContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(10); // CA-6: Trafico Normal
        factory.setAdviceChain(retryInterceptor());
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory p3ContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(50); // CA-6: Batch AI/Background
        factory.setAdviceChain(retryInterceptor()); 
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(retryInterceptor()); 
        factory.setErrorHandler(errorHandler());
        return factory;
    }
}
