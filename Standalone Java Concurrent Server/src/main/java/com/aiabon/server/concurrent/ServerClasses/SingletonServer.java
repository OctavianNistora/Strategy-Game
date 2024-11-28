package com.aiabon.server.concurrent.ServerClasses;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;

public class SingletonServer
{
    private static Server server = null;
    private static final int port = 7676;

    public static Server getServer()
    {
        if (server == null)
        {
            server = new Server(port);
        }
        return server;
    }
}
