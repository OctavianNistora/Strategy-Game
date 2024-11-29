package com.aiabon.server.concurrent.Runnables;

import com.aiabon.server.concurrent.DTOs.*;
import com.aiabon.server.concurrent.RecordsEnums.GamesListRow;
import com.aiabon.server.concurrent.RecordsEnums.LeaderboardRow;
import com.aiabon.server.concurrent.RecordsEnums.MaterialEnum;
import com.aiabon.server.concurrent.ServerClasses.Player;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/// This class is responsible for handling the commands sent by the client in the form of JSON strings.
public class PlayerRunnable implements Runnable
{
    private final int id;
    private final LinkedBlockingQueue<String> input;
    private final WebSocket output;

    List<Player> players;

    public PlayerRunnable(int id, LinkedBlockingQueue<String> input, WebSocket output)
    {
        this.id = id;
        this.input = input;
        this.output = output;
        this.players = new ArrayList<>();
    }

    private Player getPlayer(int playerId) {
        return players.stream()
                .filter(player -> player.getUserId() == playerId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run()
    {

        Gson gson = new Gson();

        while (true)
        {
            try
            {
                String command = input.take();
                PlayerCommandDTO playerCommandDTO = gson.fromJson(command, PlayerCommandDTO.class);
                Player player = getPlayer(playerCommandDTO.playerId());

                if (command.equals("stop"))
                {
                    if (player != null) {
                        player.leaveGame();
                    }
                    return;
                }

                switch (playerCommandDTO.command())
                {
                    case "login":
                        if (player == null) {
                            player = new Player(this, playerCommandDTO.playerId(), playerCommandDTO.data());
                            players.add(player);
                            output.send(createPlayerCommandResponse("login", player.getUserId(), "Successfully logged in"));
                        }
                        break;
                    case "listleaderboard":
                        LeaderboardRow[] leaderboard = player.getLeaderboard();
                        if (leaderboard == null) {
                            output.send(createPlayerCommandResponse("listleaderboard", player.getUserId(), "No leaderboard data available"));
                            break;
                        }
                        output.send(createPlayerCommandResponse("listleaderboard", player.getUserId(), gson.toJson(leaderboard)));
                        break;
                    case "listgames":
                        GamesListRow[] gamesList = player.getAvailableGamesList();
                        if (gamesList == null)
                        {
                            output.send(createPlayerCommandResponse("listleaderboard", player.getUserId(), "No leaderboard data available"));
                            break;
                        }
                        output.send(createPlayerCommandResponse("listleaderboard", player.getUserId(), gson.toJson(gamesList)));
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
                        output.send(createPlayerCommandResponse("invalidResponse", player.getUserId(), "Invalid response"));
                        break;
                }
            } catch (Exception e)
            {
                System.out.println("Thread " + id + " error: " + e);
                return;
            }
        }
    }

    private String createPlayerCommandResponse(String command, int playerId, String responseMessage) {
        return new Gson().toJson(new PlayerCommandResponseDTO(command, new PlayerCommandResponseDataDTO(playerId, responseMessage)));
    }

    public void sendGameState(String gameState, int playerId)
    {
        try
        {
            System.out.println(System.currentTimeMillis() + " Thread " + id + ": Sending game state");
            output.send(createPlayerCommandResponse("gameState", playerId, gameState));
        } catch (Exception e)
        {
            System.out.println("Thread " + id + " error: " + e);
        }
    }
}
