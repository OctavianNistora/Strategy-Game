package Runnables;

import ServerClasses.GameSession;

/// Runnable class that is responsible for spawning materials in the game session until the maximum number of materials is reached or the game session has ended.
public class GameSessionSpawnerRunnable implements Runnable
{
    private final GameSession game;

    public GameSessionSpawnerRunnable(GameSession game)
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
                //noinspection BusyWait
                Thread.sleep(5000);
            } catch (InterruptedException e)
            {
                break;
            }
        }
    }
}
