package com.aiabon.server.concurrent.Testing;

import com.aiabon.server.concurrent.DTOs.PlayerCommandDTO;
import com.aiabon.server.concurrent.DTOs.PlayerDataLoginDTO;
import com.aiabon.server.concurrent.DTOs.PlayerDataMoveDTO;
import com.aiabon.server.concurrent.DTOs.PlayerDataStructureDTO;
import com.google.gson.*;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;

public class LocalClient
{
    private JPanel MainPanel;
    private JTextArea textArea;
    private JTextField textField;
    private JScrollPane scrollPane;
    private static LocalClientWebSocket client;
    private Gson gson;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Local Client");
        LocalClient ui = new LocalClient();
        frame.setContentPane(ui.MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        try
        {
            client = new LocalClientWebSocket(new URI("ws://localhost:8080/websocket"), ui.textArea);
        } catch (URISyntaxException e)
        {
            System.out.println("URI error: " + e);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            return;
        }
        client.connect();
        ui.textArea.setText("Connected to server\n");

        ui.gson = new Gson();
        ui.textField.addActionListener((al) ->
        {
            String command = ui.textField.getText();
            String[] commandParts = command.split(" ");

            switch (commandParts[0])
            {
                case "help":
                    ui.help();
                    break;
                case "login":
                    ui.login(commandParts);
                    break;
                case "listleaderboard":
                    ui.listLeaderboard(commandParts);
                    break;
                case "listgames":
                    ui.listGames(commandParts);
                    break;
                case "startgame":
                    ui.startGame(commandParts);
                    break;
                case "joingame":
                    ui.joinGame(commandParts);
                    break;
                case "leavegame":
                    ui.leaveGame(commandParts);
                    break;
                case "ready":
                    ui.ready(commandParts);
                    break;
                case "unready":
                    ui.unready(commandParts);
                    break;
                case "move":
                    ui.move(commandParts);
                    break;
                case "pickup":
                    ui.pickup(commandParts);
                    break;
                case "trash":
                    ui.trash(commandParts);
                    break;
                case "store":
                    ui.store(commandParts);
                    break;
                case "steal":
                    ui.steal(commandParts);
                    break;
                default:
                    ui.textArea.append("Invalid command\n");
            }
            ui.textField.setText("");
        });
    }

    private void help()
    {
        textArea.append("Commands:\n");
        textArea.append("login [player ID] [player name]\n");
        textArea.append("listleaderboard\n");
        textArea.append("listgames\n");
        textArea.append("startgame [game name]\n");
        textArea.append("joingame [game ID]\n");
        textArea.append("leavegame\n");
        textArea.append("ready\n");
        textArea.append("unready\n");
        textArea.append("move [x] [y]\n");
        textArea.append("pickup [material ID]\n");
        textArea.append("trash [material type]\n");
        textArea.append("store [material type] [structure ID]\n");
        textArea.append("steal [material type] [structure ID]\n");
    }

    private void login(String[] commandData)
    {
        if (commandData.length != 3)
        {
            textArea.append("Correct usage: login [player ID] [player name]\n");
            return;
        }

        int playerID;
        try
        {
            playerID = Integer.parseInt(commandData[1]);

        } catch (NumberFormatException e)
        {
            textArea.append("com.aiabon.server.concurrent.ServerClasses.Player ID must be an integer\n");
            return;
        }

        PlayerDataLoginDTO playerDataLoginDTO = new PlayerDataLoginDTO(playerID, commandData[2]);
        String dataJson = gson.toJson(playerDataLoginDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("login", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void listLeaderboard(String[] commandData)
    {
        if (commandData.length != 1)
        {
            textArea.append("Correct usage: listleaderboard\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("listleaderboard", "");
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void listGames(String[] commandData)
    {
        if (commandData.length != 1)
        {
            textArea.append("Correct usage: listgames\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("listgames", "");
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void startGame(String[] commandData)
    {
        if (commandData.length != 2)
        {
            textArea.append("Correct usage: startgame [game name]\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("startgame", commandData[1]);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void joinGame(String[] commandData)
    {
        if (commandData.length != 2)
        {
            textArea.append("Correct usage: joingame [game ID]\n");
            return;
        }

        try
        {
            Integer.parseInt(commandData[1]);
        } catch (NumberFormatException e)
        {
            textArea.append("Game ID must be an integer\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("joingame", commandData[1]);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void leaveGame(String[] commandData)
    {
        if (commandData.length != 1)
        {
            textArea.append("Correct usage: leavegame\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("leavegame", "");
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void ready(String[] commandData)
    {
        if (commandData.length != 1)
        {
            textArea.append("Correct usage: ready\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("ready", "");
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void unready(String[] commandData)
    {
        if (commandData.length != 1)
        {
            textArea.append("Correct usage: unready\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("unready", "");
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void move(String[] commandData)
    {
        if (commandData.length != 3)
        {
            textArea.append("Correct usage: move [x] [y]\n");
            return;
        }

        double x, y;
        try
        {
            x = Double.parseDouble(commandData[1]);
            y = Double.parseDouble(commandData[2]);
        } catch (NumberFormatException e)
        {
            textArea.append("Coordinates must be doubles\n");
            return;
        }

        PlayerDataMoveDTO playerDataMoveDTO = new PlayerDataMoveDTO(x, y);
        String dataJson = gson.toJson(playerDataMoveDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("move", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void pickup(String[] commandData)
    {
        if (commandData.length != 2)
        {
            textArea.append("Correct usage: pickup [material ID]\n");
            return;
        }

        try
        {
            Integer.parseInt(commandData[1]);
        } catch (NumberFormatException e)
        {
            textArea.append("Material ID must be an integer\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("pickup", commandData[1]);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void trash(String[] commandData)
    {
        if (commandData.length != 2)
        {
            textArea.append("Correct usage: trash [material type]\n");
            return;
        }

        try
        {
            Integer.parseInt(commandData[1]);
        } catch (NumberFormatException e)
        {
            textArea.append("Material type must be an integer\n");
            return;
        }

        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("trash", commandData[1]);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void store(String[] commandData)
    {
        if (commandData.length != 3)
        {
            textArea.append("Correct usage: store [material type] [structure ID]\n");
            return;
        }

        try
        {
            Integer.parseInt(commandData[1]);
        } catch (NumberFormatException e)
        {
            textArea.append("Material type must be an integer\n");
            return;
        }
        try
        {
            Integer.parseInt(commandData[2]);
        } catch (NumberFormatException e)
        {
            textArea.append("com.aiabon.server.concurrent.ServerClasses.Structure ID must be an integer\n");
            return;
        }

        PlayerDataStructureDTO playerDataStructureDTO = new PlayerDataStructureDTO(Integer.parseInt(commandData[1]), Integer.parseInt(commandData[2]));
        String dataJson = gson.toJson(playerDataStructureDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("store", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }

    private void steal(String[] commandData)
    {
        if (commandData.length != 3)
        {
            textArea.append("Correct usage: steal [material type] [structure ID]\n");
            return;
        }

        try
        {
            Integer.parseInt(commandData[1]);
        } catch (NumberFormatException e)
        {
            textArea.append("Material type must be an integer\n");
            return;
        }
        try
        {
            Integer.parseInt(commandData[2]);
        } catch (NumberFormatException e)
        {
            textArea.append("com.aiabon.server.concurrent.ServerClasses.Structure ID must be an integer\n");
            return;
        }

        PlayerDataStructureDTO playerDataStructureDTO = new PlayerDataStructureDTO(Integer.parseInt(commandData[1]), Integer.parseInt(commandData[2]));
        String dataJson = gson.toJson(playerDataStructureDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("steal", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);

        client.send(commandJson);
    }
}
