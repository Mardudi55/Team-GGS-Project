package org.example;

import org.example.AccessLayer.AccessLayerFacade;
import org.example.BusinessLogicLayer.BlockProcessor;
import org.example.ReportingLayer.BlockListener;
import org.example.ReportingLayer.ConsoleReporter;
import org.example.ReportingLayer.StatsAccumulator;
import org.example.ReportingLayer.SummaryReportWriter;
import org.example.models.BlockData;
import org.example.models.BlockReport;
import org.example.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Entry point for the Ethereum block monitoring application.
 *
 * <p>Orchestrates the polling loop, observer notification pipeline,
 * historical data preload, and graceful shutdown sequence.</p>
 *
 * <p>Shutdown flow:
 * <ol>
 *   <li>JVM signal triggers shutdown hook</li>
 *   <li>{@link #stop()} sets {@code running = false}, interrupts monitor thread</li>
 *   <li>{@link CountDownLatch} awaited until polling loop exits</li>
 *   <li>Final summary written; RPC connection disconnected</li>
 * </ol>
 */
public class MonitorApp {
    private static final Logger log = LoggerFactory.getLogger(MonitorApp.class);

    private final AccessLayerFacade accessLayer;
    private final BlockProcessor blockProcessor ;

    private final StatsAccumulator statsAccumulator;
    private final SummaryReportWriter reportWriter;
    private final List<BlockListener> listeners = new ArrayList<>();

    private volatile boolean running = true;
    private CountDownLatch shutdownLatch = null;

    private long lastProcessedBlock = -1;
    private Thread monitorThread;

    /**
     * Initializes the MonitorApp and registers all required observers.
     */
    public MonitorApp() {
        log.info("Setting up the Monitor App...");

        this.accessLayer = new AccessLayerFacade();
        this.statsAccumulator = new StatsAccumulator();
        this.reportWriter = new SummaryReportWriter("report.txt");
        ConsoleReporter consoleReporter = new ConsoleReporter();
        this.blockProcessor = new BlockProcessor();

        addListener(this.statsAccumulator);
        addListener(this.reportWriter);
        addListener(consoleReporter);

        this.shutdownLatch = new CountDownLatch(1);
        registerShutdownHook();

        log.info("Setup complete.");
    }

    /**
     * Registers a new observer to receive block reports.
     * * @param listener the observer to add
     */
    public void addListener(BlockListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered observers with the newly generated block report.
     * * @param report the block report to broadcast
     */
    public void notifyListeners(BlockReport report) {
        listeners.forEach(l -> l.onBlock(report));
    }

    /**
     * Starts the main polling loop to fetch and process blocks continuously.
     */
    public void start() {
        this.monitorThread = Thread.currentThread();

        log.info("""
                Starting blockchain monitoring.
                Loading historical blockchain data...
                Press CTRL+C for graceful shutdown.
                """);

        loadInitialData();


        while (running) {
            List<BlockData> latestBlocks;
            try {
                latestBlocks = accessLayer.fetchLatestBlocks(1);
            } catch (IOException e) {
                continue;
            }

            if (latestBlocks == null || latestBlocks.isEmpty()) {
                continue;
            }

            BlockData latestBlock = latestBlocks.getFirst();
            var latestBlockNumber = BigInteger.valueOf(latestBlock.getBlockNumber());

            if (latestBlockNumber.longValue() == lastProcessedBlock) {
                continue;
            }

            lastProcessedBlock = latestBlockNumber.longValue();

            BlockReport report = null;
            try {
                List<TransactionData> txList = accessLayer.fetchTransactions(latestBlock);
                report = blockProcessor.process(latestBlock, txList);
            } catch (IOException e) {
                log.error("Could not fetch transaction.");
            }

            if (report != null) {
                notifyListeners(report);
            }
        }

        if (shutdownLatch != null) {
            shutdownLatch.countDown();
        }
    }

    /**
     * Initiates the graceful shutdown sequence, awaiting the loop termination
     * and triggering the final report generation.
     */
    public void stop() {
        if (!running) return;

        log.info("Initiating graceful shutdown...");
        running = false;

        if (monitorThread != null) {
            monitorThread.interrupt();
        }

        try {
            if (shutdownLatch != null) {
                shutdownLatch.await();
            }

            log.info("Writing final summary report...");
            reportWriter.writeReport(statsAccumulator.getSnapshot());

            if (accessLayer != null) accessLayer.disconnect();

        } catch (InterruptedException e) {
            log.error("Shutdown interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        log.info("Application shut down successfully.");
    }

    /**
     * Registers a JVM shutdown hook to capture external termination signals.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown Hook triggered by the OS.");
            this.stop();
        }));
    }

    /**
     * Preloads the most recent 100 blocks into the observer pipeline.
     *
     * <p>Full transaction fetch is performed only for the 10 most recent blocks;
     * older blocks are processed with an empty transaction list to reduce RPC load.
     *
     * <p>Sets {@link #lastProcessedBlock} to the most recent block number on success.
     * Processing halts early if {@code running} becomes {@code false} mid-load.
     */
    private void loadInitialData() {
        try {
            List<BlockData> blocks;

            blocks = accessLayer.fetchLatestBlocks(100);

            if (blocks == null) {
                log.warn("Failed to fetch historical blocks (returned null).");
                return;
            }

            for (int i = 0; i < blocks.size() && running; i++) {
                if (!running) break;

                BlockData block = blocks.get(i);
                List<TransactionData> transactions = new ArrayList<>();

                if (i < 10) {
                    try {
                        transactions = accessLayer.fetchTransactions(block);
                    } catch (IOException e) {
                        log.error("Could not fetch transaction.");
                    }
                }

                if (transactions == null) {
                    transactions = new ArrayList<>();
                }

                if (!running) {
                    log.info("Stopping historical block processing due to shutdown...");
                    break;
                }

                BlockReport report = blockProcessor.process(block, transactions);

                if (report != null) {
                    notifyListeners(report);
                }

                log.info("Processed historical block: {}", block.getBlockNumber());
            }

            if (!blocks.isEmpty() && blocks.getFirst() != null) {
                lastProcessedBlock = blocks.getFirst().getBlockNumber();
            }

        } catch (Exception e) {
            if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                log.warn("Fetching data interrupted - gracefully shutting down...");
                Thread.currentThread().interrupt();
            } else {
                log.error("Initial data load failed: {}", e.getMessage());
            }
        }
    }
}
