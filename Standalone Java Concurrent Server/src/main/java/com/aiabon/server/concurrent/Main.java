package com.aiabon.server.concurrent;

import com.aiabon.server.concurrent.Runnables.PlayerRunnable;

import java.net.ServerSocket;
import java.net.Socket;

public class Main
{
    public static void main(String[] args)
    {
        ServerSocket serverSocket;
        Socket socket = null;
        int threadCount = 1;

        try
        {
            //noinspection resource
            serverSocket = new ServerSocket(7676);
        } catch (Exception e)
        {
            System.out.println("Error: " + e);
            return;
        }

        //noinspection InfiniteLoopStatement
        while (true)
        {
            try
            {
                socket = serverSocket.accept();
            } catch (Exception e)
            {
                System.out.println("com.aiabon.server.concurrent.Main I/O error: " + e);
            }

            PlayerRunnable playerRunnable = new PlayerRunnable(threadCount, socket);
            Thread playerThread = new Thread(playerRunnable);
            playerThread.start();
            threadCount++;
        }
    }
}
