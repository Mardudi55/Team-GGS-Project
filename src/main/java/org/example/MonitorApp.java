package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorApp {
    private static final Logger log = LoggerFactory.getLogger(MonitorApp.class);
    private volatile boolean running = true;
    public MonitorApp() {
        log.info("Setting up the Monitor App...");
    }

    public void start() {
        int i = 0;
        while (running) {
            i++;
            if (i > 10) {
                stop();
            } else {
                final int MILLIS = 200;
                try {
                    Thread.sleep(MILLIS); // IntelliJ yells at me, but it's for demonstration purposes only.
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
            log.debug("Something is happening...");
            log.debug("\t{}", i);
        }
    }

    public void stop() {
        running = false;
    }
}
