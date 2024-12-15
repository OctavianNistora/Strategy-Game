package com.aiabon.server.concurrent;

import com.aiabon.server.concurrent.ServerClasses.Player;
import com.aiabon.server.concurrent.ServerClasses.SingletonServer;
import com.aiabon.server.concurrent.entities.GamePlayer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Arrays;
import java.util.List;

public class Main
{
    public static void main(String[] args){
        SingletonServer.getServer().start();
    }
//        SingletonServer.getSpringWebSocketClient().send("connect");
}
