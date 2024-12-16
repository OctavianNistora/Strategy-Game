package com.example.api.communication;

import com.example.api.data.access.repositories.PlayerRepository;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@NonNullApi
public class WebSocketHandler extends TextWebSocketHandler {
    private final PlayerRepository playerRepository;

    public WebSocketHandler(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession apiClientSession) throws Exception
    {
        TextWebSocketHandler ApiToConcurrentServer = new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception
            {
                System.out.println("Connection between api-client" + apiClientSession.getId() + " and api-server" + session.getId() + " established");
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
            {
                System.out.println("Received message from api-client" + message.getPayload());
                apiClientSession.sendMessage(message);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
            {
                apiClientSession.close();
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
            {
                apiClientSession.close();
            }
        };

        WebSocketClient client = new StandardWebSocketClient();
        WebSocketSession serverSession = client.execute(ApiToConcurrentServer, "ws://localhost:7676").get();

        if (serverSession != null && serverSession.isOpen())
        {
            apiClientSession.getAttributes().put("serverSession", serverSession);
            serverSession.sendMessage(new TextMessage("{\"command\":\"login\",\"data\":\"{\\\"playerId\\\":" + playerRepository.findByUsername(apiClientSession.getPrincipal().getName()).get().getId() + ",\\\"playerName\\\":\\\"" + apiClientSession.getPrincipal().getName() + "\\\"}\"}"));
        }
        else
        {
            apiClientSession.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception
    {
        WebSocketSession serverSession = (WebSocketSession) session.getAttributes().get("serverSession");
        if (serverSession != null)
        {
            System.out.println("Received message from api-server" + message.getPayload());
            serverSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        WebSocketSession serverSession = (WebSocketSession) session.getAttributes().get("serverSession");
        if (serverSession != null)
        {
            serverSession.close();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
    {
        WebSocketSession serverSession = (WebSocketSession) session.getAttributes().get("serverSession");
        if (serverSession != null)
        {
            serverSession.close();
        }
    }
}