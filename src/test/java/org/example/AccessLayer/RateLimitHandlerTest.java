package org.example.AccessLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitHandlerTest {

    @Test
    void acquire_succeedsWithinPermitLimit() throws InterruptedException {
        RateLimitHandler handler = new RateLimitHandler(3, 1000);
        handler.start();

        handler.acquire();
        handler.acquire();
        handler.acquire();

        handler.stop();
    }

    @Test
    @Timeout(3)
    void acquire_blocksWhenPermitsExhausted_thenReleasesAfterRefill() throws InterruptedException {
        RateLimitHandler handler = new RateLimitHandler(1, 300);
        handler.start();

        Thread.sleep(50);
        handler.acquire();

        long before = System.currentTimeMillis();
        handler.acquire();
        long elapsed = System.currentTimeMillis() - before;

        assertTrue(elapsed >= 200, "Should have blocked at least ~200ms, was: " + elapsed + "ms");

        handler.stop();
    }

    @Test
    @Timeout(3)
    void acquire_throwsInterruptedException_whenThreadInterrupted() throws InterruptedException {
        RateLimitHandler handler = new RateLimitHandler(1, 60_000);
        handler.start();
        Thread.sleep(50);
        handler.acquire();

        CountDownLatch blocking = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        AtomicInteger caughtCount = new AtomicInteger(0);

        Thread t = new Thread(() -> {
            try {
                blocking.countDown(); // signal: about to block
                handler.acquire();
            } catch (InterruptedException e) {
                caughtCount.incrementAndGet();
                interrupted.countDown();
            }
        });

        t.start();
        blocking.await();
        Thread.sleep(30);
        t.interrupt();
        interrupted.await();

        assertEquals(1, caughtCount.get());
        handler.stop();
    }

    @Test
    void stop_gracefullyTerminatesScheduler() {
        RateLimitHandler handler = new RateLimitHandler(5, 200);
        handler.start();

        assertDoesNotThrow(handler::stop);
    }

    @Test
    void stop_calledTwice_doesNotThrow() {
        RateLimitHandler handler = new RateLimitHandler(1, 200);
        handler.start();
        handler.stop();

        assertDoesNotThrow(handler::stop);
    }
}