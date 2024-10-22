import lombok.Getter;
import lombok.Setter;

import java.util.Hashtable;

@Getter
public class GameSession
{
    private final int gameId;  // This is the id specific to the game
    private final String gameName;  // This is the name of the game
    private final Hashtable<Integer, Player> players;  // This is the list of players in the game
    private final Hashtable<Integer, Boolean> playerReady;  // This is the list of players who are ready to start the game
    @Setter private boolean gameStarted;  // This is the flag to indicate if the game has started
    @Setter private boolean gameEnded;  // This is the flag to indicate if the game has ended
    private final Hashtable<Integer, Structure> structures;  // This is the list of structures in the game
    private final Hashtable<Integer, MaterialEntity> materials;  // This is the list of materials in the game
    @Setter private int materialCount;  // This is the number of materials in the game
    @Setter private Player winner;  // This is the player who won the game

    private final Object EnterReadyExitLock = new Object();  // This is the lock for the game

    public GameSession(int gameId, String gameName)
    {
        this.gameId = gameId;
        this.gameName = gameName;
        players = new Hashtable<>();  // The game starts with no players
        playerReady = new Hashtable<>();  // The game starts with no players ready
        gameStarted = false;  // The game has not started yet
        gameEnded = false;  // The game has not ended yet
        structures = new Hashtable<>();  // The game starts with no structures
        materials = new Hashtable<>();  // The game starts with no materials
        materialCount = 0;  // The game starts with 0 materials
        winner = null;  // The game has no winner yet
    }
}
