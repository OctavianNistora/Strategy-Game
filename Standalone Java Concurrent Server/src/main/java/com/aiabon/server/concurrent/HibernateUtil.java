package com.aiabon.server.concurrent;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from the Hibernate configuration file
            sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception and throw an exception to prevent the application from starting
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Provide a static method to access the SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Close the SessionFactory when the application terminates
    public static void shutdown() {
        getSessionFactory().close();
    }
}