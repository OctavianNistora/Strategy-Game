import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Client implements Runnable
{
    private final Server server;
    private final Player player;
    private final int threadNumber;

    public Client(Server server, Player player, int threadNumber)
    {
        this.server = server;
        this.player = player;
        this.threadNumber = threadNumber;
    }

    @Override
    public void run()
    {
        System.out.println("Thread " + threadNumber + " started for " + player.getUsername() + '\n');
        Random random = new Random();
        while (!Thread.currentThread().isInterrupted())
        {
            int randomInt = random.nextInt(10);
            switch (randomInt)
            {
            }
            try
            {
                TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(500));
            } catch (InterruptedException _)
            {
            }
        }
        System.out.println("Thread " + threadNumber + " stopped for " + player.getUsername() + '\n');
    }
}