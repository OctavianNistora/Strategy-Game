package com.aiabon.server.concurrent.ServerClasses;

import com.aiabon.server.concurrent.HibernateUtil;
import com.aiabon.server.concurrent.RecordsEnums.GamesListRow;
import com.aiabon.server.concurrent.RecordsEnums.LeaderboardRow;
import com.aiabon.server.concurrent.RecordsEnums.MaterialEntity;
import com.aiabon.server.concurrent.RecordsEnums.MaterialEnum;
import com.aiabon.server.concurrent.Runnables.PlayerRunnable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

/// Represents a player in the game. The methods in this class are used to interact with the game the player is in,
/// while also keeping track of the player's inner state and checking if game session allows the player to any action.
public class Player
{
    // com.aiabon.server.concurrent.ServerClasses.Server instance
    // Slightly faster than calling com.aiabon.server.concurrent.ServerClasses.Server.getInstance() every time
    private static final Server server = SingletonServer.getServer();
    private final PlayerRunnable runnable;
    // com.aiabon.server.concurrent.ServerClasses.Player's unique identifier
    private final int userId;
    // com.aiabon.server.concurrent.ServerClasses.Player's name
    private final String name;

    // Game the player is currently in
    private GameSession gameSession;
    // com.aiabon.server.concurrent.ServerClasses.Player's position in the game (double instead of float for additional precision)
    private double[] position;
    // com.aiabon.server.concurrent.ServerClasses.Player's inventory
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
    /// @return an array of <code>com.aiabon.server.concurrent.RecordsEnums.LeaderboardRow</code> objects of length less
    /// than 17, or <code>null</code> if the server loses connection to the database
    public LeaderboardRow[] getLeaderboard() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            // Query to fetch top 15 players by wins and calculate losses
            String hqlTopPlayers = """
                    SELECT p.name, 
                           COUNT(DISTINCT g.id) AS wins, 
                           COUNT(DISTINCT gp.game.id) - COUNT(DISTINCT g.id) AS losses
                    FROM Player p
                    LEFT JOIN p.gamesWon g
                    LEFT JOIN p.gamesPlayed gp
                    GROUP BY p.id, p.name
                    ORDER BY wins DESC
                    """;

            Query<Object[]> query = session.createQuery(hqlTopPlayers, Object[].class);
            query.setMaxResults(15);
            List<Object[]> topPlayers = query.getResultList();

            // Query to fetch current player's stats
            String hqlCurrentPlayer = """
                    SELECT p.name, 
                           COUNT(DISTINCT g.id) AS wins, 
                           COUNT(DISTINCT gp.game.id) - COUNT(DISTINCT g.id) AS losses
                    FROM Player p
                    LEFT JOIN p.gamesWon g
                    LEFT JOIN p.gamesPlayed gp
                    WHERE p.id = :currentPlayerId
                    GROUP BY p.id, p.name
                    """;

            Query<Object[]> currentPlayerQuery = session.createQuery(hqlCurrentPlayer, Object[].class);
            currentPlayerQuery.setParameter("currentPlayerId", userId);
            Object[] currentPlayerStats = currentPlayerQuery.uniqueResult();

            // Collect the results into LeaderboardRow objects
            List<LeaderboardRow> leaderboard = new ArrayList<>();

            // Add the current player's stats as the first entry
            if (currentPlayerStats != null) {
                String name = (String) currentPlayerStats[0];
                int wins = ((Long) currentPlayerStats[1]).intValue();
                int losses = ((Long) currentPlayerStats[2]).intValue();
                leaderboard.add(new LeaderboardRow(name, wins, losses));
            }

            for (Object[] row : topPlayers) {
                String name = (String) row[0];
                int wins = ((Long) row[1]).intValue();
                int losses = ((Long) row[2]).intValue();
                leaderboard.add(new LeaderboardRow(name, wins, losses));

                // Limit the leaderboard size to 17
                if (leaderboard.size() >= 17) break;
            }

            transaction.commit();

            return leaderboard.toArray(new LeaderboardRow[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /// Gets the list of available games that the player can join.
    ///
    /// @return an array of <code>com.aiabon.server.concurrent.RecordsEnums.GamesListRow</code> objects
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
            System.out.println("Created game with id: " + newGame.getGameId());

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

            gameSession.removePlayer(userId);

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

        gameSession.changePlayerReadyStatus(userId, isReady);

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

        synchronized (positionLock)
        {
            if (position == null)
            {
                // The player's position is not set until the game starts
                gameSession.decrementActiveUpdatingThreads();
                return false;
            }

            position[0] = x;
            position[1] = y;
        }

        gameSession.decrementActiveUpdatingThreads();
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
    /// @param structureId  the ID of the structure to add the material to
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
                    } else
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
    /// @param structureId  the ID of the structure to steal the material from
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
        runnable.sendGameState(gameState, this.userId);
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
