package org.example.AccessLayer;

import java.util.concurrent.Semaphore;

public class RateLimitHandler {

    private final Semaphore semaphore;

    public RateLimitHandler(int permitsPerSecond) {

        this.semaphore =
                new Semaphore(permitsPerSecond);
    }

    public void acquire() {

        try {

            semaphore.acquire();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}