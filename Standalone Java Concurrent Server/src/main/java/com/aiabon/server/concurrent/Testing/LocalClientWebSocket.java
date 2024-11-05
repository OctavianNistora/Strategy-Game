package com.aiabon.server.concurrent.Testing;

import com.google.gson.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.swing.*;
import java.net.URI;

public class LocalClientWebSocket extends WebSocketClient
{
    private final JTextArea textArea;
    private final Gson prettyGson;

    public LocalClientWebSocket(URI serverUri, JTextArea textArea)
    {
        super(serverUri);
        this.textArea = textArea;
        prettyGson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void onOpen(ServerHandshake handshake)
    {

    }

    @Override
    public void onMessage(String message)
    {
        try
        {
            JsonElement jsonElement = JsonParser.parseString(message);
            String prettyJson = prettyGson.toJson(jsonElement);
            textArea.setText(prettyJson);
        } catch (JsonParseException e)
        {
            textArea.setText(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {

    }

    @Override
    public void onError(Exception ex)
    {

    }
}
