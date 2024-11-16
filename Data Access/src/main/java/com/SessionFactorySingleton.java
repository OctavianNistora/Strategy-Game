package main.java.com;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactorySingleton {
    private static SessionFactory sessionFactory;
    private SessionFactorySingleton(){}

    public static SessionFactory getInstance()
    {
        if(sessionFactory == null){
            synchronized (SessionFactorySingleton.class)
            {
                try{
                    sessionFactory = new Configuration().configure().buildSessionFactory();
                }
                catch (Exception ex){
                    throw new ExceptionInInitializerError("Cant create session: " + ex);
                }
            }
        }
        return sessionFactory;
    }
}
