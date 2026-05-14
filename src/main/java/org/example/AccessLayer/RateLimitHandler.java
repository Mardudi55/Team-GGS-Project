package org.example.AccessLayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Controls the rate of outbound API calls to the Ethereum node provider.
 *
 * <p>Uses a {@link Semaphore} refilled at a fixed rate by a background scheduler thread.
 * Callers invoke {@link #acquire()} before each API call.
 * If the rate limit is reached, the calling thread blocks until the next refill window.
 *
 * <p>Lifecycle: call {@link #start()} once before use, {@link #stop()} on shutdown.
 *
 * <p>Usage:
 * <pre>{@code
 * RateLimitHandler handler = new RateLimitHandler(5);
 * handler.start();
 * // for (i=0; i<10; i++) {
 *      handler.acquire();
 * //   make an API call
 * // }
 * handler.stop();
 * }</pre>
 */
class RateLimitHandler {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final int permitsPerSecond;

    /**
     * @param permitsPerSecond maximum number of API calls allowed per second.
     */
    public RateLimitHandler(int permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
        this.semaphore = new Semaphore(permitsPerSecond);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Starts the background scheduler that refills permits every second.
     * Must be called once before any {@link #acquire()} calls.
     */
    public void start() {
        final long INITIAL_DELAY_MS = 0;
        final long DELAY_MS = 1000;

        scheduler.scheduleAtFixedRate(
            new PermitReleaseTask(semaphore, permitsPerSecond),
            INITIAL_DELAY_MS,
            DELAY_MS,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Acquires a permit, blocking the calling thread if the rate limit is reached.
     * Returns immediately if a permit is available.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    /**
     * Shuts down the background scheduler thread.
     * Waits up to 2 seconds for graceful termination before forcing shutdown.
     * Should be called once on application shutdown.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            final int MAX_WAIT_TIME_SEC = 2;
            if (!scheduler.awaitTermination(MAX_WAIT_TIME_SEC, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
