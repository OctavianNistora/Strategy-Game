import lombok.Getter;
import lombok.Setter;

@Getter
public class Player
{
    private final int userId; // This is the player's unique identifier
    private final String username;    // This is the player's username
    @Setter private GameSession gameSession;  // This is the game the player is currently in
    @Setter private Structure structure; // This is the id of the structure the player is currently building
    @Setter private double[] position;  // This is the player's position in the game (double instead of float for precision)
    @Setter private MaterialEnum[] inventory;   // This is the player's inventory
    @Setter private int materialCount;  // This is the number of materials the player has in their inventory

    private final Object gameLock = new Object();  // This is the lock for the game the player is in
    private final Object structureLock = new Object(); // This is the lock for the structure the player is building
    private final Object positionLock = new Object();  // This is the lock for the player's position
    private final Object inventoryLock = new Object();  // This is the lock for the player's inventory

    public Player(int userId, String username, int gamesWon, int gamesLost)
    {
        this.userId = userId;
        this.username = username;
        this.gameSession = null;   // The player is not in a game by default
        this.structure = null;  // The player is not building a structure by default
        this.position = null;   // The player's position is not set until the game starts
        this.inventory = null;  // The player's inventory is not set until the game starts
        this.materialCount = 0; // The player's material count is not set until the game starts
    }
}
