public class GameSessionSpawnerThread implements Runnable
{
    private final GameSession game;

    public GameSessionSpawnerThread(GameSession game)
    {
        this.game = game;
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted() && game.checkStatusAndTrySpawnMaterial())
        {
            try
            {
                Thread.sleep(5000);
            } catch (InterruptedException e)
            {
                break;
            }
        }
    }
}
