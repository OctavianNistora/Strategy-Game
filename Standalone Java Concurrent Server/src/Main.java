import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main
{
    public static void main(String[] args)
    {
        Server server = new Server();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<Player> playersLocal = new ArrayList<>();
        playersLocal.add(new Player(1, "Alice", 0, 0));
        playersLocal.add(new Player(2, "Bob", 0, 0));
        playersLocal.add(new Player(3, "Charlie", 0, 0));
        playersLocal.add(new Player(4, "David", 0, 0));

        for (Player player : playersLocal)
        {
            Thread thread = new Thread(new Client(server, player, playersLocal.indexOf(player)));
            thread.start();
            threads.add(thread);

            try
            {
                TimeUnit.MILLISECONDS.sleep(750);
            } catch (InterruptedException _)
            {
            }
        }

        try
        {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException _)
        {
        } finally
        {
            for (Thread thread : threads)
            {
                thread.interrupt();
            }
        }
    }
}
