package Runnables;

import ServerClasses.GameSession;

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
