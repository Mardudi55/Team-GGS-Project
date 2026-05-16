package org.example.AccessLayer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class PermitReleaseTaskTest {

    @Test
    void run_releasesExactPermitCount() {
        Semaphore semaphore = new Semaphore(0);
        PermitReleaseTask task = new PermitReleaseTask(semaphore, 5);

        task.run();

        assertEquals(5, semaphore.availablePermits());
    }

    @Test
    void run_drainsExistingPermitsBeforeReleasing() {
        Semaphore semaphore = new Semaphore(10);
        PermitReleaseTask task = new PermitReleaseTask(semaphore, 3);

        task.run();

        assertEquals(3, semaphore.availablePermits());
    }

    @Test
    void run_calledMultipleTimes_doesNotAccumulatePermits() {
        Semaphore semaphore = new Semaphore(0);
        PermitReleaseTask task = new PermitReleaseTask(semaphore, 2);

        task.run();
        task.run();
        task.run();

        // each run drains then sets to permitsPerRelease — always 2
        assertEquals(2, semaphore.availablePermits());
    }

    @Test
    void run_withZeroPermits_drainsThenReleasesZero() {
        Semaphore semaphore = new Semaphore(5);
        PermitReleaseTask task = new PermitReleaseTask(semaphore, 0);

        task.run();

        assertEquals(0, semaphore.availablePermits());
    }

    @Test
    void run_withOnePermit_behavesCorrectly() {
        Semaphore semaphore = new Semaphore(0);
        PermitReleaseTask task = new PermitReleaseTask(semaphore, 1);

        task.run();

        assertEquals(1, semaphore.availablePermits());
    }
}
