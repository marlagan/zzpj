package com.zzpj.purrsuit.notificationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notification")
                .setAllowedOriginPatterns("*"); // Usunąłem .withSockJS()
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Dodajemy "/queue" dla wiadomości prywatnych kierowanych do usera
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        // Ustawienie prefixu dla wiadomości do konkretnego użytkownika
        registry.setUserDestinationPrefix("/user");
    }

    // Ten fragment przechwytuje moment nawiązywania połączenia przez STOMP
    // i wyciąga token JWT z nagłówka, przypisując użytkownika do sesji.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Pobieramy nagłówek "Authorization" podany we frontendzie
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            // Dekodujemy JWT i ustawiamy jako użytkownika sesji
                            Jwt jwt = jwtDecoder.decode(token);
                            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                            accessor.setUser(authentication);
                        } catch (Exception e) {
                            // Logowanie błędu, jeśli token jest niepoprawny lub wygasł
                            System.err.println("Błąd weryfikacji tokenu WebSocket: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}