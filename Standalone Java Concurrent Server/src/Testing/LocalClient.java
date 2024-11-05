package Testing;

import DTOs.PlayerCommandDTO;
import DTOs.PlayerDataLoginDTO;
import DTOs.PlayerDataMoveDTO;
import DTOs.PlayerDataStructureDTO;
import com.google.gson.*;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class LocalClient
{
    private JPanel mainPanel;
    private JTextArea textArea;
    private JTextField textField;
    private JScrollPane scrollPane;
    private DataInputStream in;
    private DataOutputStream out;
    private Gson gson;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Local Client");
        LocalClient ui = new LocalClient();
        frame.setContentPane(ui.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);

        Socket socket;
        while (true)
        {
            try
            {
                //noinspection resource
                socket = new Socket("localhost", 7676);
                break;
            } catch (Exception e)
            {
                ui.textArea.append("Connection error: could not connect to server\nReattempting in 1 second\n");
            }
            try
            {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                System.out.println("Thread error: " + e);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                return;
            }
        }
        ui.textArea.setText("Connected to server\n");

        try
        {
            ui.in = new DataInputStream(socket.getInputStream());
            ui.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            return;
        }

        ui.gson = new Gson();
        ui.textField.addActionListener(_ ->
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

        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        while (true)
        {
            try
            {
                String response = ui.in.readUTF();
                try
                {
                    JsonElement jsonElement = JsonParser.parseString(response);
                    String prettyJson = prettyGson.toJson(jsonElement);
                    ui.textArea.setText(prettyJson);
                } catch (JsonParseException e)
                {
                    ui.textArea.setText(response);
                }
            } catch (EOFException e)
            {
                System.out.println("EOF error: " + e);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                return;
            } catch (IOException e)
            {
                System.out.println("I/O error: " + e);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                return;
            } catch (Exception e)
            {
                System.out.println("Error: " + e);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                return;
            }
        }
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
            textArea.append("ServerClasses.Player ID must be an integer\n");
            return;
        }

        PlayerDataLoginDTO playerDataLoginDTO = new PlayerDataLoginDTO(playerID, commandData[2]);
        String dataJson = gson.toJson(playerDataLoginDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("login", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
            textArea.append("ServerClasses.Structure ID must be an integer\n");
            return;
        }

        PlayerDataStructureDTO playerDataStructureDTO = new PlayerDataStructureDTO(Integer.parseInt(commandData[1]), Integer.parseInt(commandData[2]));
        String dataJson = gson.toJson(playerDataStructureDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("store", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
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
            textArea.append("ServerClasses.Structure ID must be an integer\n");
            return;
        }

        PlayerDataStructureDTO playerDataStructureDTO = new PlayerDataStructureDTO(Integer.parseInt(commandData[1]), Integer.parseInt(commandData[2]));
        String dataJson = gson.toJson(playerDataStructureDTO);
        PlayerCommandDTO playerCommandDTO = new PlayerCommandDTO("steal", dataJson);
        String commandJson = gson.toJson(playerCommandDTO);
        try
        {
            out.writeUTF(commandJson);
        } catch (IOException e)
        {
            System.out.println("I/O error: " + e);
        }
    }
}
