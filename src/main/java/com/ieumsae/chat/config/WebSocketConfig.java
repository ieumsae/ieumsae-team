package com.ieumsae.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-endpoint").withSockJS();
    }
    // WebSocket 연결을 위한 엔드포인트를 등록
    // AddEndpoint 메소드는 웹소켓 연결을 시작할 때 사용할 URL 경로
    // withSockJS()는 웹소켓을 지원하지 않는 환경에서도 실시간 통신이 가능하게 함 (버전이 낮은 브라우저)

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커를 구성합니다.

        config.enableSimpleBroker("/topic");
        // topic 주제에 대해 메시지 브로커를 사용하도록 설정
        // 클라이언트가 이러한 접두사로 시작하는 주제를 구독하면, 해당 주제로 전송된 메시지를 받을 수 있다.
        // 메시지를 받을 때

        config.setApplicationDestinationPrefixes("/app");
        // 메시지를 전송하면 자동으로 주소 앞에 /app이 붙는다. => @MessageMapping 이 적용된 컨트롤러 메소드로 라우팅
        // 클라이언트에서 서버로 메시지를 전송할 때 사용할 접두사를 지정
        // 메시지를 보낼 때

    }

}