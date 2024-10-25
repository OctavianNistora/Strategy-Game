import java.net.Socket;

public class PlayerRunnable implements Runnable
{
    private final Player player;
    private final Socket socket;

    public PlayerRunnable(Socket socket, int userId, String name)
    {
        this.socket = socket;
        Player player = new Player(this, userId, name);
        this.player = player;
    }

    @Override
    public void run()
    {
        while (true)
        {
        }
    }

    public void sendGameState(String gameState)
    {
    }
}
