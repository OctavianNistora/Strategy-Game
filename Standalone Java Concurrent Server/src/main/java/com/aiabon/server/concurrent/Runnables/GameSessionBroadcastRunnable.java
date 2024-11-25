package com.aiabon.server.concurrent.Runnables;

import com.aiabon.server.concurrent.ServerClasses.GameSession;

/// Runnable class that broadcasts the game state (snapshot) to all players in the game session at a fixed rate until the game ends
public class GameSessionBroadcastRunnable implements Runnable
{
    private final GameSession game;

    public GameSessionBroadcastRunnable(GameSession game)
    {
        this.game = game;
    }

    @Override
    public void run()
    {
        try
        {
            while (!game.isGameStarted())   // Broadcast the game state 3 times per second until the game starts
            {
                game.broadcastGameState();
                //noinspection BusyWait
                Thread.sleep(333);
            }
            while (!game.isGameEnded())  // Broadcast the game state 30 times per second while the game is running
            {
                game.broadcastGameState();
                //noinspection BusyWait
                Thread.sleep(33);
            }
            for (int i = 0; i < 10; i++)    // Broadcast the game state 1 times per second after the game ends
            {
                game.broadcastGameState();
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
}
