package com.aiabon.server.concurrent.ServerClasses;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;

public class SingletonServer
{
    private static Server server = null;
    private static final int port = 7676;

    private static final String springServerUrl = "ws://localhost:8080/websocket";
    private static WebSocketClient springWebSocketClient = null;

    public static Server getServer()
    {
        if (server == null)
        {
            server = new Server(port);
        }
        return server;
    }

    public static WebSocketClient getSpringWebSocketClient() {
        if (springWebSocketClient == null) {
            springWebSocketClient = createSpringWebSocketClient();
        }
        return springWebSocketClient;
    }

    private static WebSocketClient createSpringWebSocketClient() {
        WebSocketClient client = new WebSocketClient(URI.create(springServerUrl)) {
            @Override
            public void onOpen(org.java_websocket.handshake.ServerHandshake handshake) {
                System.out.println("Connected to Spring WebSocket server.");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("Message received from Spring WebSocket server: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Disconnected from Spring WebSocket server: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return client;
    }
}
