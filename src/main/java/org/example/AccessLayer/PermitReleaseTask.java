package org.example.AccessLayer;

import java.util.concurrent.Semaphore;

/**
 * Runnable Task that is used by {@link RateLimitHandler} to manage waiting on a separate thread.
 */
class PermitReleaseTask implements Runnable {
    private final Semaphore semaphore;
    private final int permitsPerRelease;

    /**
     * @param semaphore {@code Semaphore} responsible for limiting.
     * @param permitsPerRelease {@code int} amount of actions that can happen per single time unit.
     */
    public PermitReleaseTask(Semaphore semaphore, int permitsPerRelease) {
        this.semaphore = semaphore;
        this.permitsPerRelease = permitsPerRelease;
    }

    /**
     * Removes all unused permits and releasing new ones.
     * Amount of released permits depends on {@link #permitsPerRelease} set in a constructor.
     */
    @Override
    public void run() {
        semaphore.drainPermits();
        semaphore.release(permitsPerRelease);
    }
}
