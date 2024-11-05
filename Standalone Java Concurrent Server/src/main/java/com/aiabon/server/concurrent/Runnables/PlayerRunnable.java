package com.aiabon.server.concurrent.Runnables;

import com.aiabon.server.concurrent.DTOs.PlayerDataLoginDTO;
import com.aiabon.server.concurrent.DTOs.PlayerCommandDTO;
import com.aiabon.server.concurrent.DTOs.PlayerDataMoveDTO;
import com.aiabon.server.concurrent.DTOs.PlayerDataStructureDTO;
import com.aiabon.server.concurrent.RecordsEnums.GamesListRow;
import com.aiabon.server.concurrent.RecordsEnums.LeaderboardRow;
import com.aiabon.server.concurrent.RecordsEnums.MaterialEnum;
import com.aiabon.server.concurrent.ServerClasses.Player;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/// This class is responsible for handling the commands sent by the client in the form of JSON strings.
public class PlayerRunnable implements Runnable
{
    private final int id;
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Player player;

    public PlayerRunnable(int id, Socket socket)
    {
        this.id = id;
        this.socket = socket;
        this.in = null;
        this.out = null;
        this.player = null;
    }

    @Override
    public void run()
    {
        try
        {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e)
        {
            System.out.println("Thread " + id + " I/O error: " + e);
            return;
        }

        Gson gson = new Gson();
        while (true)
        {
            try
            {
                String playerName = in.readUTF();
                try
                {
                    PlayerCommandDTO playerCommandDTO = gson.fromJson(playerName, PlayerCommandDTO.class);
                    if (playerCommandDTO.command().equals("login"))
                    {
                        PlayerDataLoginDTO playerDataLoginDTO = gson.fromJson(playerCommandDTO.data(), PlayerDataLoginDTO.class);
                        player = new Player(this, playerDataLoginDTO.id(), playerDataLoginDTO.name());
                        out.writeUTF("Successfully logged in player");
                        break;
                    } else
                    {
                        out.writeUTF("Invalid player data format");
                    }
                } catch (JsonSyntaxException e)
                {
                    out.writeUTF("Invalid player data format");
                }
            } catch (EOFException e)
            {
                System.out.println("Thread " + id + ": Client disconnected");
                return;
            } catch (IOException e)
            {
                System.out.println("Thread " + id + " I/O error: " + e);
                return;
            } catch (Exception e)
            {
                System.out.println("Thread " + id + " error: " + e);
                return;
            }
        }

        while (true)
        {
            try
            {
                String command = in.readUTF();
                PlayerCommandDTO playerCommandDTO = gson.fromJson(command, PlayerCommandDTO.class);
                switch (playerCommandDTO.command())
                {
                    case "listleaderboard":
                        LeaderboardRow[] leaderboard = player.getLeaderboard();
                        out.writeUTF(gson.toJson(leaderboard));
                        break;
                    case "listgames":
                        GamesListRow[] gamesList = player.getAvailableGamesList();
                        if (gamesList == null)
                        {
                            out.writeUTF("No games available");
                            break;
                        }
                        out.writeUTF(gson.toJson(gamesList));
                        break;
                    case "startgame":
                        boolean success = player.createAndJoinGame(playerCommandDTO.data());
                        if (success)
                        {
                            System.out.println("Thread " + id + ": Game created and joined");
                        } else
                        {
                            System.out.println("Thread " + id + ": Game creation failed");
                        }
                        break;
                    case "joingame":
                        try
                        {
                            int gameId = Integer.parseInt(playerCommandDTO.data());
                            boolean joined = player.tryJoinGame(gameId);
                            if (joined)
                            {
                                System.out.println("Thread " + id + ": Joined game " + gameId);
                            } else
                            {
                                System.out.println("Thread " + id + ": Failed to join game " + gameId);
                            }
                        } catch (NumberFormatException e)
                        {
                            System.out.println("Thread " + id + ": Invalid game ID");
                        }
                        break;
                    case "leavegame":
                        player.leaveGame();
                        break;
                    case "ready":
                        player.changeReadyStatus(true);
                        break;
                    case "unready":
                        player.changeReadyStatus(false);
                        break;
                    case "move":
                        try
                        {
                            PlayerDataMoveDTO playerDataMoveDTO = gson.fromJson(playerCommandDTO.data(), PlayerDataMoveDTO.class);
                            player.move(playerDataMoveDTO.x(), playerDataMoveDTO.y());
                        } catch (JsonSyntaxException e)
                        {
                            System.out.println("Invalid move data format");
                        }
                        break;
                    case "pickup":
                        try
                        {
                            int materialId = Integer.parseInt(playerCommandDTO.data());
                            player.tryPickUpMaterial(materialId);
                        } catch (JsonSyntaxException e)
                        {
                            System.out.println("Invalid pickup data format");
                        }
                        break;
                    case "trash":
                        try
                        {
                            int materialType = Integer.parseInt(playerCommandDTO.data());
                            player.trashMaterial(MaterialEnum.values()[materialType]);
                        } catch (JsonSyntaxException e)
                        {
                            System.out.println("Invalid trash data format");
                        }
                        break;
                    case "store":
                        try
                        {
                            PlayerDataStructureDTO playerDataStructureDTO = gson.fromJson(playerCommandDTO.data(), PlayerDataStructureDTO.class);
                            player.tryAddMaterialToStructure(MaterialEnum.values()[playerDataStructureDTO.materialType()], playerDataStructureDTO.structureId());
                        } catch (JsonSyntaxException e)
                        {
                            System.out.println("Invalid store data format");
                        }
                        break;
                    case "steal":
                        try
                        {
                            PlayerDataStructureDTO playerDataStructureDTO = gson.fromJson(playerCommandDTO.data(), PlayerDataStructureDTO.class);
                            player.tryStealMaterialFromStructure(MaterialEnum.values()[playerDataStructureDTO.materialType()], playerDataStructureDTO.structureId());
                        } catch (JsonSyntaxException e)
                        {
                            System.out.println("Invalid steal data format");
                        }
                        break;
                    default:
                        out.writeUTF("Invalid command");
                        break;
                }
            } catch (EOFException e)
            {
                System.out.println("Thread " + id + ": Client disconnected");
                player.leaveGame();
                return;
            } catch (IOException e)
            {
                System.out.println("Thread " + id + " I/O error: " + e);
                player.leaveGame();
                return;
            } catch (Exception e)
            {
                System.out.println("Thread " + id + " error: " + e);
                player.leaveGame();
                return;
            }
        }
    }

    public void sendGameState(String gameState)
    {
        try
        {
            System.out.println("Thread " + id + ": Sending game state");
            out.writeUTF(gameState);
        } catch (IOException e)
        {
            System.out.println("Thread " + id + " I/O error: " + e);
        }
    }
}
