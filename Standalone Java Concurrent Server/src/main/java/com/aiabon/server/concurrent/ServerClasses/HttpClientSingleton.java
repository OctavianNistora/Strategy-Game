package com.aiabon.server.concurrent.ServerClasses;

import java.net.http.HttpClient;

public class HttpClientSingleton {

    // Volatile variable to ensure visibility and prevent instruction reordering
    private static volatile HttpClientSingleton instance;

    // The actual HttpClient instance
    private final HttpClient httpClient;

    // Private constructor to prevent instantiation
    private HttpClientSingleton() {
        // Configure the HttpClient instance as needed
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    // Public method to provide access to the singleton instance
    public static HttpClientSingleton getInstance() {
        if (instance == null) {
            synchronized (HttpClientSingleton.class) {
                if (instance == null) {
                    instance = new HttpClientSingleton();
                }
            }
        }
        return instance;
    }

    // Method to get the HttpClient
    public HttpClient getHttpClient() {
        return this.httpClient;
    }
}