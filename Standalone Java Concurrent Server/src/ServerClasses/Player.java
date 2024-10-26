package ServerClasses;

import RecordsEnums.GamesListRow;
import RecordsEnums.LeaderboardRow;
import RecordsEnums.MaterialEntity;
import RecordsEnums.MaterialEnum;
import Runnables.PlayerRunnable;

/**
 * Represents a player in the game.
 */
public class Player
{
    // ServerClasses.Server instance
    // Slightly faster than calling ServerClasses.Server.getInstance() every time
    private static final Server server = Server.getInstance();
    private final PlayerRunnable runnable;
    // ServerClasses.Player's unique identifier
    private final int userId;
    // ServerClasses.Player's name
    private final String name;

    // Game the player is currently in
    private GameSession gameSession;
    // ServerClasses.Player's position in the game (double instead of float for additional precision)
    private double[] position;
    // ServerClasses.Player's inventory
    private MaterialEnum[] inventory;
    // Number of materials the player has in their inventory
    private int materialCount;

    // This is the lock for the game the player is in
    private final Object gameLock = new Object();
    // This is the lock for the player's position
    private final Object positionLock = new Object();
    // This is the lock for the player's inventory
    private final Object inventoryLock = new Object();

    public Player(PlayerRunnable runnable, int userId, String name)
    {
        this.runnable = runnable;
        this.userId = userId;
        this.name = name;
        // The player is not in a game by default
        this.gameSession = null;
        // The player's position is not set until the game starts
        this.position = null;
        // The player's inventory is not set until the game starts
        this.inventory = null;
        // The player's material count is not set until the game starts
        this.materialCount = 0;
    }


    public int getUserId()
    {
        return userId;
    }

    /// Gets the player stats (name, wins, losses) for the first 15 players sorted by the player's wins in
    /// descending, and also the stats for the player calling this method, who is the first player in the array.
    ///
    /// @return an array of <code>RecordsEnums.LeaderboardRow</code> objects of length less
    /// than 17, or <code>null</code> if the server loses connection to the database
    public LeaderboardRow[] getLeaderboard()
    {
        //TODO: Implement this method once the DB is implemented
        return null;
    }

    /// Gets the list of available games that the player can join.
    ///
    /// @return an array of <code>RecordsEnums.GamesListRow</code> objects
    public GamesListRow[] getAvailableGamesList()
    {
        return server.getAvailableGamesList();
    }

    /// Creates a new game with the given name and the player as the host.
    ///
    /// @param gameName the name of the game
    /// @return <code>true</code> if the player successfully created and joined the game, <code>false</code> otherwise
    public boolean createAndJoinGame(String gameName)
    {
        synchronized (gameLock)
        {
            if (gameSession != null)
            {
                // The player is already in a game
                return false;
            }

            GameSession newGame = server.createGame(gameName, this);
            if (newGame == null)
            {
                return false;
            }

            gameSession = newGame;
            return true;
        }
    }

    /// Tries to join a game with the given ID, and if successful,
    /// stores the game's reference in the player's inner state for faster access.
    ///
    /// @param gameId the ID of the game to join
    /// @return <code>true</code> if the player successfully joined the game, <code>false</code> otherwise
    public boolean tryJoinGame(int gameId)
    {
        synchronized (gameLock)
        {
            if (gameSession != null)
            {
                // The player is already in a game
                return false;
            }

            GameSession joinedGame = server.tryAddPlayerToGame(this, gameId);
            if (joinedGame == null)
            {
                return false;
            }

            gameSession = joinedGame;
            return true;
        }
    }

    /// Leaves the game the player is currently in, if any, and resets the player's inner state to the initial one.
    public void leaveGame()
    {
        synchronized (gameLock)
        {
            if (gameSession == null)
            {
                // The player is not in a game so there's nothing to leave
                return;
            }

            gameSession.checkRequiresStateBroadcast();

            gameSession.removePlayer(this);

            gameSession.decrementActiveUpdatingThreads();

            gameSession = null;

            synchronized (inventoryLock)
            {
                inventory = null;
                materialCount = 0;
            }

            synchronized (positionLock)
            {
                position = null;
            }
        }
    }

    /// Changes the player's ready status in the game they are currently in.
    ///
    /// @param isReady the new ready status
    /// @return <code>true</code> if the player successfully changed their ready status,
    /// or <code>false</code> if the player is not in a game
    public boolean changeReadyStatus(boolean isReady)
    {
        GameSession gameSession;
        synchronized (gameLock)
        {
            gameSession = this.gameSession;
        }
        if (gameSession == null)
        {
            // The player is not in a game so they cannot change their ready status
            return false;
        }

        gameSession.checkRequiresStateBroadcast();

        gameSession.changePlayerReadyStatus(this, isReady);

        gameSession.decrementActiveUpdatingThreads();
        return true;
    }

    /// Attempts to prepare the player's inner state for the game they are currently in.
    public void preparePlayerForGame()
    {
        synchronized (gameLock)
        {
            if (gameSession == null)
            {
                // The player is not in a game so there's nothing to prepare
                return;
            }

            gameSession.checkRequiresStateBroadcast();

            synchronized (inventoryLock)
            {
                inventory = new MaterialEnum[5];
                materialCount = 0;
            }

            synchronized (positionLock)
            {
                position = new double[2];
            }

            gameSession.decrementActiveUpdatingThreads();
        }
    }

    /// Moves the player to the given position.
    ///
    /// @param x the new x-coordinate
    /// @param y the new y-coordinate
    /// @return <code>true</code> if the player successfully moved, or <code>false</code> if the player is not in a game
    public boolean move(double x, double y)
    {
        synchronized (positionLock)
        {
            GameSession gameSession;
            // gameLock is used instead of positionLock to get the gameSession and avoid
            // changing the player's position while the game is broadcasting its state
            synchronized (gameLock)
            {
                gameSession = this.gameSession;
            }

            if (gameSession == null)
            {
                // The player is not in a game so they cannot move
                return false;
            }

            gameSession.checkRequiresStateBroadcast();

            position[0] = x;
            position[1] = y;

            gameSession.decrementActiveUpdatingThreads();
        }
        return true;
    }

    /// Attempts to pick up a material from the game the player is currently in and add it to the player's inventory.
    ///
    /// @param materialId the ID of the material to pick up
    /// @return <code>true</code> if the player successfully picked up the material and added
    /// it to the inventory, or <code>false</code> if the player is not in a game, the
    /// player's inventory is full, or the material does not exist or is already picked up
    public boolean tryPickUpMaterial(int materialId)
    {
        GameSession gameSession;
        synchronized (gameLock)
        {
            gameSession = this.gameSession;
        }
        if (gameSession == null)
        {
            // The player is not in a game so they cannot pick up materials
            return false;
        }

        gameSession.checkRequiresStateBroadcast();

        synchronized (inventoryLock)
        {
            if (inventory == null)
            {
                // The player's inventory is not set until the game starts
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            if (materialCount >= 5)
            {
                // The player's inventory is full
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            MaterialEntity material = gameSession.removeMaterial(materialId);
            if (material == null)
            {
                // The material does not exist or is already picked up
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            inventory[materialCount] = material.materialType();
            materialCount++;

            gameSession.decrementActiveUpdatingThreads();
        }
        return true;
    }

    /// Attempts to delete a material from the player's inventory.
    ///
    /// @param material the material type to delete
    public void trashMaterial(MaterialEnum material)
    {
        GameSession gameSession;
        synchronized (gameLock)
        {
            gameSession = this.gameSession;
        }
        if (gameSession == null)
        {
            // The player is not in a game so they cannot trash materials
            return;
        }

        gameSession.checkRequiresStateBroadcast();

        synchronized (inventoryLock)
        {
            if (inventory == null)
            {
                // The player's inventory is not set until the game starts
                gameSession.decrementActiveUpdatingThreads();
                return;
            }

            for (int i = 0; i < materialCount; i++)
            {
                if (inventory[i] == material)
                {
                    inventory[i] = null;
                    for (int j = i; j < materialCount - 1; j++)
                    {
                        inventory[j] = inventory[j + 1];
                    }
                    inventory[materialCount - 1] = null;
                    materialCount--;
                    break;
                }
            }

            gameSession.decrementActiveUpdatingThreads();
        }
    }

    /// Attempts to add a material to a structure in the game the
    /// player is currently in and remove it from their inventory.
    ///
    /// @param materialType the type of material to add
    /// @param structureId the ID of the structure to add the material to
    /// @return <code>true</code> if the player successfully added the material to the
    /// structure, or <code>false</code> if the player is not in a game, the player
    /// does not have the material, or the structure's material progress is already full
    public boolean tryAddMaterialToStructure(MaterialEnum materialType, int structureId)
    {
        GameSession gameSession;
        synchronized (gameLock)
        {
            gameSession = this.gameSession;
        }
        if (gameSession == null)
        {
            // The player is not in a game so they cannot add materials to structures
            return false;
        }

        gameSession.checkRequiresStateBroadcast();

        synchronized (inventoryLock)
        {
            if (inventory == null)
            {
                // The player's inventory is not set until the game starts
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            for (int i = 0; i < materialCount; i++)
            {
                if (inventory[i] == materialType)
                {
                    if (gameSession.tryAddMaterialToStructure(materialType, structureId))
                    {
                        for (int j = i; j < materialCount - 1; j++)
                        {
                            inventory[j] = inventory[j + 1];
                        }
                        inventory[materialCount - 1] = null;
                        materialCount--;
                        gameSession.decrementActiveUpdatingThreads();
                        return true;
                    }
                    else
                    {
                        gameSession.decrementActiveUpdatingThreads();
                        return false;
                    }
                }
            }

            gameSession.decrementActiveUpdatingThreads();
            return false;
        }
    }

    /// Attempts to steal a material from a structure in the game
    /// the player is currently in and add it to their inventory.
    ///
    /// @param materialType the type of material to steal
    /// @param structureId the ID of the structure to steal the material from
    /// @return <code>true</code> if the player successfully stole the material from the structure
    /// and added it to their inventory, or <code>false</code> if the player is not in a game, the
    /// player's inventory is full, or the structure's material progress is already empty
    public boolean tryStealMaterialFromStructure(MaterialEnum materialType, int structureId)
    {
        GameSession gameSession;
        synchronized (gameLock)
        {
            gameSession = this.gameSession;
        }
        if (gameSession == null)
        {
            // The player is not in a game so they cannot steal materials from structures
            return false;
        }

        gameSession.checkRequiresStateBroadcast();

        synchronized (inventoryLock)
        {
            if (inventory == null)
            {
                // The player's inventory is not set until the game starts
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            if (materialCount >= 5)
            {
                // The player's inventory is full
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            if (gameSession.tryRemoveMaterialFromStructure(materialType, structureId))
            {
                inventory[materialCount] = materialType;
                materialCount++;
                gameSession.decrementActiveUpdatingThreads();
                return true;
            }

            gameSession.decrementActiveUpdatingThreads();
            return false;
        }
    }

    public void sendGameState(String gameState)
    {
        runnable.sendGameState(gameState);
    }


    @Override
    public String toString()
    {
        StringBuilder playerString = new StringBuilder();
        playerString.append("{");
        playerString.append("\"id\":").append(userId).append(",");
        playerString.append("\"name\":\"").append(name).append("\",");
        playerString.append("\"position\":[");
        if (position != null)
        {
            playerString.append(position[0]).append(",").append(position[1]);
        }
        playerString.append("],");
        playerString.append("\"inventory\":[");
        for (int i = 0; i < materialCount; i++)
        {
            playerString.append(inventory[i].ordinal()).append(",");
        }
        if (materialCount > 0)
        {
            playerString.deleteCharAt(playerString.length() - 1);
        }
        playerString.append("]");
        playerString.append("}");
        return playerString.toString();
    }
}
