package com.ibpms.poc.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Tópicos a los que los clientes Vue se van a suscribir (ej. /topic/workdesk/{tenantId})
        config.enableSimpleBroker("/topic");
        // Prefijo para enviar mensajes DESDE el cliente hacia el backend (aunque en Workdesk es 90% broadcast de servidor)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint que SockJS / stompjs va a golpear
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns("*") // Permisivo en config E2E. En prod cambiar a FRONTEND_URL
                .withSockJS();
    }
}
