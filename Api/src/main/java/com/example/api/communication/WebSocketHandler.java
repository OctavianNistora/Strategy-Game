package com.example.api.communication;

import com.example.api.contracts.DTOs.PlayerCommandDTO;
import com.example.api.contracts.DTOs.PlayerCommandResponseDTO;
import com.example.api.contracts.DTOs.PlayerDataLoginDTO;
import com.google.gson.Gson;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NonNullApi
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Map<Integer, WebSocketSession> playerSessions = new ConcurrentHashMap<>();
    private WebSocketSession concurrentServerSession;
    private Boolean connectedToConcurrentServer = false;

    public WebSocketHandler() throws InterruptedException {
        TryToEastablishConnectionToConcurrentServer();
    }

    private void TryToEastablishConnectionToConcurrentServer() throws InterruptedException {
        while (!connectedToConcurrentServer) {
            try {
                System.out.println("Trying to connect to concurrent server...");
                WebSocketClient client = new StandardWebSocketClient();
                client.execute(createConcurrentServerHandler(), "ws://localhost:7676");
                Thread.sleep(2000);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private TextWebSocketHandler createConcurrentServerHandler() {
        return new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                concurrentServerSession = session;
                System.out.println("Connected to concurrent server");
                connectedToConcurrentServer = true;
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                System.out.println("Received message from concurrent server: " + message.getPayload());

                Gson gson = new Gson();
                PlayerCommandResponseDTO playerCommandDTO = gson.fromJson(message.getPayload(), PlayerCommandResponseDTO.class);
                int playerId = playerCommandDTO.playerCommandResponse().playerId();
                sendToPlayerClient(playerId, message);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                connectedToConcurrentServer = false;
                TryToEastablishConnectionToConcurrentServer();
            }
        };
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer playerId = getPlayerIdFromSession(session);
        playerSessions.remove(playerId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Gson gson = new Gson();
        PlayerCommandDTO playerCommandDTO = gson.fromJson(payload, PlayerCommandDTO.class);

        if (playerCommandDTO.command().equals("login")) {
            playerSessions.put(playerCommandDTO.playerId(), session);
            if (concurrentServerSession != null) {
                concurrentServerSession.sendMessage(message);
            }
        } else {
            if (concurrentServerSession != null) {
                concurrentServerSession.sendMessage(message);
            }
        }
    }

    private void sendToPlayerClient(int playerId, TextMessage message) throws IOException {
        WebSocketSession session = playerSessions.get(playerId);
        if (session != null) {
            session.sendMessage(message);
        }
    }

    private Integer getPlayerIdFromSession(WebSocketSession session) {
        for (Map.Entry<Integer, WebSocketSession> entry : playerSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return -1;
    }
}