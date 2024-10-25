import java.util.Collection;
import java.util.Hashtable;

public class Server
{
    private static Server instance = null;
    private final Hashtable<Integer, GameSession> availableGames = new Hashtable<>();
    private int nextGameId = 1;

    /// Get the singleton instance of the server.
    public static Server getInstance()
    {
        if (instance == null)
        {
            instance = new Server();
        }
        return instance;
    }

    /// Returns an array of games that are available to join.
    ///
    /// @return An array of <code>GamesListRow</code> representing the available games.
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
    /// @return The <code>GameSession</code> object representing the new game.
    public synchronized GameSession createGame(String gameName, Player player)
    {
        GameSession game = new GameSession(nextGameId, gameName, player);
        availableGames.put(game.getGameId(), game);
        nextGameId++;
        return game;
    }

    /// Try to add a player to a game.
    ///
    /// @param player The player to add to the game.
    /// @param gameId The ID of the game to add the player to.
    /// @return The <code>GameSession</code> object representing the game the player was added to, or <code>null</code> if the player could not be added.
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
