package com.example.demo.config;

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
        // Nơi client đăng ký nhận tin nhắn (Ví dụ: /topic/room/123)
        config.enableSimpleBroker("/topic");
        // Tiền tố cho các request từ client gửi lên (Ví dụ: /app/move)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối socket
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}