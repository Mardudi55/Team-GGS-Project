package org.example;

import org.example.AccessLayer.AccessLayerFacade;
import org.example.BusinessLogicLayer.BlockProcessor;
import org.example.ReportingLayer.BlockListener;
import org.example.ReportingLayer.StatsAccumulator;
import org.example.ReportingLayer.SummaryReportWriter;
import org.example.models.BlockReport;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MonitorApp {
    private static final Logger log = LoggerFactory.getLogger(MonitorApp.class);

    AccessLayerFacade accessLayer     = null;
    BlockProcessor blockProcessor     = null;
    StatsAccumulator statsAccumulator; // WHAT IF:
                                              // StatsAccumulator (SAC) is replaced there with SummaryReportWriter (SRW),
                                              // and the SAC is a member of SRW only, so onBlock of SRW passes data do accumulator?
    SummaryReportWriter reportWriter;
    List<BlockListener> listeners     = new ArrayList<>();

    private volatile boolean running  = true;
    CountDownLatch shutdownLatch      = null;

    public MonitorApp() {
        log.info("Setting up the Monitor App...");
        //...
        reportWriter = new SummaryReportWriter();
        addListener(reportWriter);
        //...
        statsAccumulator = new StatsAccumulator();
        addListener(statsAccumulator);
        //...
        shutdownLatch = new CountDownLatch(1);
        registerShutdownHook();
        log.info("Done.");
    }

    public void start() {
        int i = 0;
        final int SECOND = 1000;
        while (running) {
            try {
                Thread.sleep(SECOND); // IntelliJ yells at me, but it's for demonstration purposes only.
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            i++;
            log.debug("Something is happening...");
            log.debug("\t{}", i);
        }
        shutdownLatch.countDown();
    }

    public void stop() {
        running = false;
        log.debug("Shutting down the App...");
        try {
            shutdownLatch.await();
            //StatsSnapshot snapshot = statsAccumulator.getSnapshot();
            reportWriter.writeReport(/*snapshot*/);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            //accessLayer.disconnect(); // also stops RateLimitHandler thread
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }
    }

    public void addListener(BlockListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners(BlockReport report) {
        listeners.forEach(l -> l.onBlock(report));
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
}
