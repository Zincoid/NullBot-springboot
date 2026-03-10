package org.bot.nullbot.config;

import org.bot.nullbot.websocket.WebSocketLogger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
{
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketLogger(), "/logs")
                .setAllowedOrigins("*");  // 允许所有跨域请求，生产环境应限制
    }
}
