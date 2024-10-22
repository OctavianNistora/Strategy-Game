import java.util.HashMap;
import java.util.Hashtable;

public class Server
{
    private final Hashtable<Integer, GameSession> activeGames = new Hashtable<>();
    private int latestGame;

    public Server()
    {
        getLatestGameIdFromDB();
    }

    // Server-related methods

    private void getLatestGameIdFromDB()
    {
        //TODO: Once the DB is implemented, get the latest game id from the database
        latestGame = 1;
    }

    public HashMap<String, Integer[]> getLeaderboard()
    {
        //TODO: Once the DB is implemented, get the first 15 players with the most won games from the database, and return their usernames, won games count and lost games count
        return new HashMap<>();
    }

    public void createGame(String gameName)
    {
        synchronized (activeGames)
        {
            int gameId = ++latestGame;
            activeGames.put(gameId, new GameSession(gameId, gameName));
        }
        // TODO: Once the DB is implemented, update the game count and add the game to the database
    }

    public HashMap<Integer, String> getActiveGames()
    {
        HashMap<Integer, String> gameList = new HashMap<>();
        activeGames.values().forEach(game -> gameList.put(game.getGameId(), game.getGameName()));
        return gameList;
    }

    public boolean addPlayerToGame(Player player, int gameId)
    {
        GameSession foundGameSession = activeGames.get(gameId);
        if (foundGameSession == null)
        {
            return false;
        }

        synchronized (foundGameSession.getEnterReadyExitLock())
        {
            if (foundGameSession.isGameStarted() || foundGameSession.getPlayers().size() >= 4)
            {
                return false;
            }
            foundGameSession.getPlayers().put(player.getUserId(), player);
            foundGameSession.getPlayerReady().put(player.getUserId(), false);
            player.setGameSession(foundGameSession);
        }
        return true;
    }

    // Game-related methods

    public String getGameState(GameSession gameSession)
    {
        //TODO: 30 times per second, send the game state to all players in the game
        return "";
    }

    public void removePlayerFromGame(Player player)
    {
        GameSession leftGameSession;

        synchronized (player.getGameLock())
        {
            leftGameSession = player.getGameSession();
            player.setGameSession(null);
        }
        if (leftGameSession == null)
        {
            return;
        }

        synchronized (leftGameSession.getEnterReadyExitLock())
        {
            leftGameSession.getPlayers().remove(player.getUserId());
            leftGameSession.getPlayerReady().remove(player.getUserId());

            if (leftGameSession.isGameStarted() && !leftGameSession.isGameEnded())
            {
                //TODO: Once the DB is implemented, update the player's lost games count
            } else
            {
                attemptStartGame(leftGameSession);
            }
        }

        synchronized (player.getStructureLock())
        {
            player.setStructure(null);
        }

        synchronized (player.getPositionLock())
        {
            player.setPosition(null);
        }

        synchronized (player.getInventoryLock())
        {
            player.setInventory(null);
            player.setMaterialCount(0);
        }
    }

    public void updatePlayerReady(Player player, boolean ready)
    {
        GameSession joinedGameSession;

        synchronized (player.getGameLock())
        {
            joinedGameSession = player.getGameSession();
        }
        if (joinedGameSession == null)
        {
            return;
        }

        synchronized (joinedGameSession.getEnterReadyExitLock())
        {
            joinedGameSession.getPlayerReady().put(player.getUserId(), ready);
            if (ready)
            {
                attemptStartGame(joinedGameSession);
            }
        }
    }

    public void updatePlayerPosition(Player player, double[] position)
    {
        synchronized (player.getPositionLock())
        {
            if (player.getPosition() == null)
            {
                return;
            }
            player.setPosition(position);
        }
    }

    public void attemptPlayerPickMaterial(Player player, int materialId)
    {
        GameSession currentGameSession;

        synchronized (player.getGameLock())
        {
            currentGameSession = player.getGameSession();
        }
        if (currentGameSession == null)
        {
            return;
        }

        synchronized (player.getInventoryLock())
        {
            if (player.getInventory() == null || player.getMaterialCount() >= player.getInventory().length)
            {
                return;
            }

            MaterialEntity material;
            synchronized (currentGameSession.getMaterials())
            {
                material = currentGameSession.getMaterials().get(materialId);
                currentGameSession.getMaterials().remove(materialId);
            }
            if (material == null)
            {
                return;
            }

            player.getInventory()[player.getMaterialCount()] = material.getMaterialType();
            player.setMaterialCount(player.getMaterialCount() + 1);
        }
    }

    private boolean addPlayerMaterial(Player player, MaterialEnum materialType)
    {
        synchronized (player.getInventoryLock())
        {
            if (player.getInventory() == null || player.getMaterialCount() >= player.getInventory().length)
            {
                return false;
            }

            player.getInventory()[player.getMaterialCount()] = materialType;
            player.setMaterialCount(player.getMaterialCount() + 1);
        }
        return true;
    }

    public boolean removePlayerMaterial(Player player, MaterialEnum materialType)
    {
        synchronized (player.getInventoryLock())
        {
            MaterialEnum[] inventory = player.getInventory();
            if (inventory == null)
            {
                return false;
            }

            for (int i = 0; i < player.getMaterialCount(); i++)
            {
                if (player.getInventory()[i] == materialType)
                {
                    for (int j = i; j < player.getMaterialCount() - 1; j++)
                    {
                        player.getInventory()[j] = player.getInventory()[j + 1];
                    }
                    player.setMaterialCount(player.getMaterialCount() - 1);
                    return true;
                }
            }
        }
        return false;
    }

    public void addMaterialToStructure(Player player, MaterialEnum materialType)
    {
        GameSession currentGameSession;
        synchronized (player.getGameLock())
        {
            currentGameSession = player.getGameSession();
        }
        if (currentGameSession == null)
        {
            return;
        }

        Structure foundStructure;
        synchronized (currentGameSession.getStructures())
        {
            foundStructure = currentGameSession.getStructures().get(player.getUserId());
        }
        if (foundStructure == null)
        {
            return;
        }

        synchronized (foundStructure.getProgressLock())
        {
            Hashtable<MaterialEnum, Integer> progress = foundStructure.getProgress();
            int stage = foundStructure.getStage();
            if (progress.get(materialType) >= stage * 2 + 1)
            {
                return;
            }

            if (!removePlayerMaterial(player, materialType))
            {
                return;
            }

            progress.put(materialType, progress.get(materialType) + 1);
            for (Integer currentProgress : progress.values())
            {
                if (currentProgress < stage * 2 + 1)
                {
                    return;
                }
            }

            if (stage < 3)
            {
                foundStructure.setStage(stage + 1);
                for (MaterialEnum material : MaterialEnum.values())
                {
                    progress.put(material, 0);
                }
            }
            else
            {
                endGame(currentGameSession, player);
            }
        }
    }

    public void removeMaterialFromStructure(Player player, int structureId, MaterialEnum materialType)
    {
        GameSession currentGameSession;
        synchronized (player.getGameLock())
        {
            currentGameSession = player.getGameSession();
        }
        if (currentGameSession == null)
        {
            return;
        }

        Structure foundStructure;
        synchronized (currentGameSession.getStructures())
        {
            foundStructure = currentGameSession.getStructures().get(structureId);
        }
        if (foundStructure == null)
        {
            return;
        }

        synchronized (foundStructure.getProgressLock())
        {
            Hashtable<MaterialEnum, Integer> progress = foundStructure.getProgress();
            int stage = foundStructure.getStage();
            if (progress.get(materialType) <= 0)
            {
                return;
            }

            if (!addPlayerMaterial(player, materialType))
            {
                return;
            }

            progress.put(materialType, progress.get(materialType) - 1);
        }
    }

    private void attemptStartGame(GameSession gameSession)
    {
        synchronized (gameSession.getEnterReadyExitLock())
        {
            if (gameSession.isGameStarted() || gameSession.getPlayerReady().size() > 1 || gameSession.getPlayerReady().containsValue(false))
            {
                return;
            }

            gameSession.setGameStarted(true);

            gameSession.getPlayers().values().forEach(player ->
            {
                player.setStructure(new Structure(player.getUserId(), player.getPosition()));
                gameSession.getStructures().put(player.getUserId(), player.getStructure());

                player.setPosition(new double[]{0, 0});

                player.setInventory(new MaterialEnum[5]);
                player.setMaterialCount(0);
            });

            //TODO: Start threads for spawning materials
        }
    }

    private void endGame(GameSession gameSession, Player winner)
    {
        synchronized (gameSession.getEnterReadyExitLock())
        {
            gameSession.setGameEnded(true);
            gameSession.setWinner(winner);

            gameSession.getPlayers().values().forEach(player ->
            {
                if (player == winner)
                {
                    //TODO: Once the DB is implemented, update the player's won games count
                } else
                {
                    //TODO: Once the DB is implemented, update the player's lost games count
                }

                synchronized (player.getGameLock())
                {
                    player.setGameSession(null);
                }
            });
        }
    }
}
