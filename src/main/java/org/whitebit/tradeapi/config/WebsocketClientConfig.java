package org.whitebit.tradeapi.config;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebsocketClientConfig {
    @Bean
    public WebSocketContainer webSocketContainer() {
        return ContainerProvider.getWebSocketContainer();
    }
}
