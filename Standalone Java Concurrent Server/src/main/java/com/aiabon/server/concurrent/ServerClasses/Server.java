package com.aiabon.server.concurrent.ServerClasses;

import com.aiabon.server.concurrent.RecordsEnums.GamesListRow;
import com.aiabon.server.concurrent.Runnables.PlayerRunnable;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

/// The server class that keeps track of all the available games and establishes a connection between a player and a game session.
public class Server extends WebSocketServer
{
    private final Hashtable<Integer, GameSession> availableGames = new Hashtable<>();
    private int threadCount = 1;
    private int nextGameId = 1;

    public Server(int port)
    {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        LinkedBlockingQueue<String> playerQueue = new LinkedBlockingQueue<>();
        conn.setAttachment(playerQueue);

        PlayerRunnable playerRunnable = new PlayerRunnable(threadCount, playerQueue, conn);
        Thread playerThread = new Thread(playerRunnable);
        playerThread.start();

        threadCount++;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        LinkedBlockingQueue<String> playerQueue = conn.getAttachment();
        playerQueue.add("stop");
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        LinkedBlockingQueue<String> playerQueue = conn.getAttachment();
        playerQueue.add(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {

    }

    @Override
    public void onStart()
    {

    }


    /// Returns an array of games that are available to join.
    ///
    /// @return An array of <code>com.aiabon.server.concurrent.RecordsEnums.GamesListRow</code> representing the available games.
    public GamesListRow[] getAvailableGamesList()
    {
        Collection<GameSession> availableGamesSnapshot = availableGames.values();
        GamesListRow[] gamesList = new GamesListRow[availableGames.size()];
        int i = 0;
        for (GameSession game : availableGamesSnapshot)
        {
            gamesList[i] = new GamesListRow(game.getGameId(), game.getGameName());
            i++;
        }
        return gamesList;
    }

    /// Create a new game with the given name and player.
    ///
    /// @param gameName The name of the game.
    /// @param player The player that will be the host of the game.
    /// @return The <code>com.aiabon.server.concurrent.ServerClasses.GameSession</code> object representing the new game.
    public synchronized GameSession createGame(String gameName, Player player)
    {
        GameSession game = new GameSession(nextGameId, gameName, player);
        availableGames.put(game.getGameId(), game);
        //TODO: Once the DB is implemented, add the game to the DB and get the ID from the DB.

        nextGameId++;
        return game;
    }

    /// Try to add a player to a game.
    ///
    /// @param player The player to add to the game.
    /// @param gameId The ID of the game to add the player to.
    /// @return The <code>com.aiabon.server.concurrent.ServerClasses.GameSession</code> object representing the game the player was added to, or <code>null</code> if the player could not be added.
    public GameSession tryAddPlayerToGame(Player player, int gameId)
    {
        GameSession game = availableGames.get(gameId);
        if (game == null)
        {
            return null;
        }

        game.checkRequiresStateBroadcast();

        if (game.tryAddPlayer(player))
        {
            game.decrementActiveUpdatingThreads();
            return game;
        }

        game.decrementActiveUpdatingThreads();
        return null;
    }

    /// Remove a game from the server's list of available games.
    ///
    /// @param gameId The ID of the game to remove.
    public void removeGame(int gameId)
    {
        availableGames.remove(gameId);
    }
}
