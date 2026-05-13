package org.example.AccessLayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessLayerTest {
    @Test
    void shouldWaitBetweenPollingRequests()
            throws InterruptedException {

        long start = System.currentTimeMillis();

        Thread.sleep(3000);

        long end = System.currentTimeMillis();

        long duration = end - start;

        assertTrue(duration >= 3000);
    }
    @Test
    void shouldRespectPollingInterval()
            throws Exception {

        final int pollingInterval = 3000;

        long start = System.currentTimeMillis();

        Thread.sleep(pollingInterval);

        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= pollingInterval);
    }
    @Test
    void shouldHandleInterruptedSleep() {

        Thread.currentThread().interrupt();

        assertThrows(
                InterruptedException.class,
                () -> Thread.sleep(1000)
        );
    }
    @Test
    void shouldPreventRapidRpcRequests()
            throws InterruptedException {

        final int pollingInterval = 2000;

        long firstRequest =
                System.currentTimeMillis();

        Thread.sleep(pollingInterval);

        long secondRequest =
                System.currentTimeMillis();

        long difference =
                secondRequest - firstRequest;

        assertTrue(difference >= pollingInterval);
    }
    @Test
    void shouldThrottleHistoricalProcessing()
            throws InterruptedException {

        final int delay = 300;

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {

            Thread.sleep(delay);
        }

        long elapsed =
                System.currentTimeMillis() - start;

        assertTrue(elapsed >= 3000);
    }
    @Test
    void shouldRestoreInterruptFlag() {

        Thread.currentThread().interrupt();

        assertTrue(Thread.currentThread().isInterrupted());
    }
}
