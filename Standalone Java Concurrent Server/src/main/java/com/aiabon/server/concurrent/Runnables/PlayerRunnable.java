package com.aiabon.server.concurrent.Runnables;

import com.aiabon.server.concurrent.DTOs.*;
import com.aiabon.server.concurrent.RecordsEnums.GamesListRow;
import com.aiabon.server.concurrent.RecordsEnums.LeaderboardRow;
import com.aiabon.server.concurrent.RecordsEnums.MaterialEnum;
import com.aiabon.server.concurrent.ServerClasses.Player;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;

import java.util.concurrent.LinkedBlockingQueue;

/// This class is responsible for handling the commands sent by the client in the form of JSON strings.
public class PlayerRunnable implements Runnable
{
    private final int id;
    private final LinkedBlockingQueue<String> input;
    private final WebSocket output;
    private Player player;

    public PlayerRunnable(int id, LinkedBlockingQueue<String> input, WebSocket output)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.player = null;
    }

    @Override
    public void run()
    {

        Gson gson = new Gson();
        while (true)
        {
            try
            {
                String playerName = input.take();
                try
                {
                    PlayerCommandDTO playerCommandDTO = gson.fromJson(playerName, PlayerCommandDTO.class);
                    PlayerDataLoginDTO playerDataLoginDTO = gson.fromJson(playerCommandDTO.data(), PlayerDataLoginDTO.class);
                    if (playerCommandDTO.command().equals("login"))
                    {
                        player = new Player(this, playerDataLoginDTO.playerId(), playerDataLoginDTO.playerName());
                        output.send("Successfully logged in");
                        input.clear();
                        break;
                    } else
                    {
                        output.send("Invalid player data format");
                    }
                } catch (JsonSyntaxException e)
                {
                    output.send("Invalid player data format");
                }
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
                String command = input.take();
                if (command.equals("stop"))
                {
                    player.leaveGame();
                    return;
                }

                PlayerCommandDTO playerCommandDTO = gson.fromJson(command, PlayerCommandDTO.class);
                switch (playerCommandDTO.command())
                {
                    case "listleaderboard":
                        LeaderboardRow[] leaderboard = player.getLeaderboard();
                        output.send(gson.toJson(leaderboard));
                        break;
                    case "listgames":
                        GamesListRow[] gamesList = player.getAvailableGamesList();
                        output.send(gson.toJson(gamesList));
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
                        output.send("Invalid command");
                        break;
                }
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
            System.out.println(System.currentTimeMillis() + " Thread " + id + ": Sending game state");
            output.send(gameState);
        } catch (Exception e)
        {
            System.out.println("Thread " + id + " error: " + e);
        }
    }
}
