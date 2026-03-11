package org.bot.nullbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

// @Configuration
// @EnableWebSocket
// public class WebSocketConfig implements WebSocketConfigurer
// {
//     @Override
//     public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//         registry.addHandler(new WebSocketBroadcastHandler(), "/monitor")  // WebSocket 连接端点
//                 .setAllowedOrigins("*");  // 允许所有跨域请求 生产环境应限制
//     }
// }

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);  // 心跳通常只需一个线程
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();  // 可省略 Spring 会自动初始化
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理 并设置心跳 (服务端每10秒发送心跳 期望客户端每10秒发送)
        registry.enableSimpleBroker("/topic")  // 订阅地址前缀
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler());
        // 设置客户端发送消息的前缀 (如果需要从客户端接收消息)
        registry.setApplicationDestinationPrefixes("/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // WebSocket 连接端点
                .setAllowedOriginPatterns("*")
                // .withSockJS()  // 支持 SockJS 回退
        ;
    }
}
