package com.aiabon.server.concurrent;

import com.aiabon.server.concurrent.ServerClasses.SingletonServer;

public class Main
{
    public static void main(String[] args)
    {
        SingletonServer.getServer().start();
    }
}
