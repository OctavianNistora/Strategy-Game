package ServerClasses;

import RecordsEnums.MaterialEntity;
import RecordsEnums.MaterialEnum;
import Runnables.GameSessionBroadcastRunnable;
import Runnables.GameSessionSpawnerRunnable;

import java.util.Hashtable;
import java.util.concurrent.Semaphore;

/// This class represents a game session. A game session is a game that is being
/// played by multiple players. The methods in this class are used to manage the
/// game session's state and broadcast the game state to all the players in the game session.
public class GameSession
{
    private final int gameId;
    private final String gameName;
    private final Hashtable<Integer, Player> players;
    private final Hashtable<Integer, Boolean> readyMap;
    private final Hashtable<Integer, Structure> structures;
    private final Hashtable<Integer, MaterialEntity> materials;

    private int gameStatus;
    private int latestMaterialId;
    private Player winner;

    private final Thread gameSessionBroadcastThread;
    private Thread gameSessionSpawnerThread;

    private final Object CheckRequiresStateBroadcastLock = new Object();
    private boolean requiresStateBroadcast = false;
    private final Object ActiveUpdatingThreadsLock = new Object();
    private volatile int activeUpdatingThreads = 0;
    private final Semaphore RequiresStateBroadcastSemaphore = new Semaphore(1);

    private final Object EnterReadyExitLock = new Object();

    public GameSession(int gameId, String gameName, Player player)
    {
        this.gameId = gameId;
        this.gameName = gameName;
        players = new Hashtable<>();  // The game starts with no players
        players.put(player.getUserId(), player);
        readyMap = new Hashtable<>();  // The game starts with no players ready
        readyMap.put(player.getUserId(), false);
        gameStatus = 0;  // The game starts in the lobby
        structures = new Hashtable<>();  // The game starts with no structures
        materials = new Hashtable<>();  // The game starts with no materials
        latestMaterialId = 0;  // The game starts with 0 materials
        winner = null;  // The game has no winner yet

        GameSessionBroadcastRunnable gameSessionBroadcastRunnable = new GameSessionBroadcastRunnable(this);
        this.gameSessionBroadcastThread = new Thread(gameSessionBroadcastRunnable);
        this.gameSessionBroadcastThread.start();
    }


    public int getGameId()
    {
        return gameId;
    }

    public String getGameName()
    {
        return gameName;
    }

    public boolean isGameStarted()
    {
        return gameStatus == 1;
    }

    public boolean isGameEnded()
    {
        return gameStatus == 2;
    }


    /// This method is used to check if the game session requires a state broadcast.
    /// If it does, the method will block until the state is broadcast. If it doesn't,
    /// the method will increment the number of active updating threads.
    public void checkRequiresStateBroadcast()
    {
        synchronized (CheckRequiresStateBroadcastLock)
        {
            if (!requiresStateBroadcast)
            {
                synchronized (ActiveUpdatingThreadsLock)
                {
                    activeUpdatingThreads++;
                }
                return;
            }
        }
        RequiresStateBroadcastSemaphore.acquireUninterruptibly();
        // The semaphore was released in the broadcastGameState method, which means all the other
        // threads related to this game session will wait until the semaphore is released
        activeUpdatingThreads++;
        RequiresStateBroadcastSemaphore.release();
    }

    /// This method is used to decrement the number of active updating threads.
    public void decrementActiveUpdatingThreads()
    {
        synchronized (ActiveUpdatingThreadsLock)
        {
            activeUpdatingThreads--;
        }
    }

    /// Tries to add a player to the game session. If the game session
    /// is in the lobby, the player will be added, otherwise not.
    ///
    /// @param player The player to be added to the game session.
    /// @return True if the player was added successfully, false otherwise.
    public boolean tryAddPlayer(Player player)
    {
        synchronized (EnterReadyExitLock)
        {
            if (players.isEmpty())
            {
                return false;
            }

            if (gameStatus == 0)
            {
                players.put(player.getUserId(), player);
                readyMap.put(player.getUserId(), false);
                return true;
            }
            return false;
        }
    }

    /// Removes a player from the game session.
    ///
    /// @param playerId The player to be removed from the game session.
    public void removePlayer(int playerId)
    {
        synchronized (EnterReadyExitLock)
        {
            Player removedPlayer = players.remove(playerId);
            readyMap.remove(playerId);
            if (removedPlayer == null)
            {
                return;
            }

            if (players.isEmpty())
            {
                Server.getInstance().removeGame(gameId);
                gameSessionBroadcastThread.interrupt();
                if (gameSessionSpawnerThread != null)
                {
                    gameSessionSpawnerThread.interrupt();
                }
            }
        }
    }

    /// Changes the ready status of a player.
    /// If all players are ready, the game will start.
    ///
    /// @param playerId The player whose ready status will be changed.
    /// @param ready    The new ready status of the player.
    public void changePlayerReadyStatus(int playerId, boolean ready)
    {
        synchronized (EnterReadyExitLock)
        {
            if (!readyMap.containsKey(playerId))
            {
                return;
            }

            readyMap.put(playerId, ready);
            if (ready)
            {
                tryStartGame();
            }
        }
    }

    /// Tries to start the game. The game will start if the game is in the
    /// lobby, there are at least two players and all players are ready.
    private void tryStartGame()
    {
        if (gameStatus != 0)
        {
            return;
        }
        if (players.size() < 2)
        {
            return;
        }

        for (Boolean ready : readyMap.values())
        {
            if (!ready)
            {
                return;
            }
        }
        gameStatus = 1;
        Server.getInstance().removeGame(this.gameId);

        //TODO: Once the DB is implemented, insert a new record for each
        // player-session pair in the mapping (many-to-many) table

        Thread gameSessionSpawnerThread = new Thread(new GameSessionSpawnerRunnable(this));
        gameSessionSpawnerThread.start();
        this.gameSessionSpawnerThread = gameSessionSpawnerThread;

        for (Player player : players.values())
        {
            Structure structure = new Structure(player.getUserId());
            structures.put(player.getUserId(), structure);
            player.preparePlayerForGame();
        }
    }

    /// Checks the status of the game and tries to spawn a material.
    ///
    /// @return True if the game is in progress and an attempt to spawn a material was made, false otherwise.
    public boolean checkStatusAndTrySpawnMaterial()
    {
        if (gameStatus != 1)
        {
            return false;
        }

        if (materials.size() >= 10)
        {
            return true;
        }

        // The client will handle overlapping materials
        double[] position = new double[2];
        position[0] = -10.0 + Math.random() * 20.0;
        position[1] = -10.0 + Math.random() * 20.0;
        MaterialEntity material = new MaterialEntity(latestMaterialId, MaterialEnum.values()[(int) (Math.random() * 3)], position, 1.0);

        materials.put(latestMaterialId, material);
        latestMaterialId++;

        return true;

    }

    /// Removes a material from the game session.
    ///
    /// @param materialId The ID of the material to be removed.
    public MaterialEntity removeMaterial(int materialId)
    {
        return materials.remove(materialId);
    }

    /// Tries to add a material to a structure. If the structure is complete, the game will end.
    ///
    /// @param materialType The type of the material to be added.
    /// @param structureId  The ID of the structure to which the material will be added.
    /// @return True if the material was added successfully, false otherwise.
    public boolean tryAddMaterialToStructure(MaterialEnum materialType, int structureId)
    {
        Structure structure = structures.get(structureId);
        if (structure == null)
        {
            return false;
        }

        if (!structure.tryAddProgress(materialType))
        {
            return false;
        }

        if (gameStatus == 1 && structure.isComplete())
        {
            winner = players.get(structureId);
            gameStatus = 2;

            //TODO: Once the DB is implemented, update the record by adding the winner
        }

        return true;
    }

    /// Tries to remove a material from a structure.
    ///
    /// @param materialType The type of the material to be removed.
    /// @param structureId  The ID of the structure from which the material will be removed.
    /// @return True if the material was removed successfully, false otherwise.
    public boolean tryRemoveMaterialFromStructure(MaterialEnum materialType, int structureId)
    {
        Structure structure = structures.get(structureId);
        if (structure == null)
        {
            return false;
        }

        return structure.tryRemoveProgress(materialType);
    }

    /**
     * This method is used to broadcast the game state to all the players in the game session.
     */
    public void broadcastGameState()
    {
        RequiresStateBroadcastSemaphore.acquireUninterruptibly();
        synchronized (CheckRequiresStateBroadcastLock)
        {
            requiresStateBroadcast = true;
        }

        while (activeUpdatingThreads > 0)
        {
            Thread.onSpinWait();
        }

        String state = this.toString();
        for (Player player : players.values())
        {
            player.sendGameState(state);
        }
        RequiresStateBroadcastSemaphore.release();
    }


    /**
     * This method is used to get all the important information about the game session in a JSON format.
     *
     * @return The game session as a JSON string.
     */
    @Override
    public String toString()
    {
        StringBuilder GameSessionString = new StringBuilder();
        GameSessionString.append("{");

        GameSessionString.append("\"id\":").append(gameId).append(",");

        GameSessionString.append("\"name\":\"").append(gameName).append("\",");

        GameSessionString.append("\"players\":[");
        players.values().forEach(player ->
                GameSessionString.append(player).append(","));
        GameSessionString.deleteCharAt(GameSessionString.length() - 1);
        GameSessionString.append("],");

        GameSessionString.append("\"readyMap\":[");
        readyMap.forEach((playerId, ready) ->
                GameSessionString.append("{\"playerId\":").append(playerId).append(",\"ready\":").append(ready).append("},"));
        GameSessionString.deleteCharAt(GameSessionString.length() - 1);
        GameSessionString.append("],");

        GameSessionString.append("\"winner\":").append(winner == null ? "null" : winner.getUserId()).append(",");

        GameSessionString.append("\"gameState\":").append(gameStatus).append(",");

        GameSessionString.append("\"materials\":[");
        materials.values().forEach(material ->
                GameSessionString.append(material).append(","));
        if (!materials.isEmpty())
        {
            GameSessionString.deleteCharAt(GameSessionString.length() - 1);
        }
        GameSessionString.append("],");

        GameSessionString.append("\"structures\":[");
        structures.values().forEach(structure ->
                GameSessionString.append(structure).append(","));
        if (!structures.isEmpty())
        {
            GameSessionString.deleteCharAt(GameSessionString.length() - 1);
        }
        GameSessionString.append("]");

        GameSessionString.append("}");

        return GameSessionString.toString();
    }
}
