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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Orchestrates the fetching, processing, and reporting of blockchain data.
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

        final int POLLING_INTERVAL_MS = 3000;

        while (running) {
            try {
                List<BlockData> latestBlocks =
                        accessLayer.fetchLatestBlocks(1);

                if (latestBlocks.isEmpty()) {

                    Thread.sleep(POLLING_INTERVAL_MS);
                    continue;
                }

                BlockData latestBlock =
                        latestBlocks.getFirst();

                var latestBlockNumber =
                        java.math.BigInteger.valueOf(
                                latestBlock.getBlockNumber()
                        );

                if (latestBlockNumber.longValue() == lastProcessedBlock) {
                    Thread.sleep(POLLING_INTERVAL_MS);
                    continue;
                }

                lastProcessedBlock = latestBlockNumber.longValue();

                BlockData blockData = latestBlock;
                var transactions =
                        accessLayer.fetchTransactions(
                                blockData
                        );

                List<TransactionData> txList = transactions;

                BlockReport report = blockProcessor.process(blockData, txList);

                if (report != null) {
                    notifyListeners(report);
                }

                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                log.warn("Polling loop interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
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

    private void loadInitialData() {
        log.info("Loading historical blocks...");

        try {
            List<BlockData> blocks = accessLayer.fetchLatestBlocks(100);

            if (blocks == null) {
                log.warn("Failed to fetch historical blocks (returned null).");
                return;
            }

            for (int i = 0; i < blocks.size() && running; i++) {
                if (!running) break;

                BlockData block = blocks.get(i);
                List<TransactionData> transactions = new ArrayList<>();

                if (i < 10) {
                    transactions = accessLayer.fetchTransactions(block);

                    if (transactions == null) {
                        transactions = new ArrayList<>();
                    }
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
                log.info("Fetching data interrupted - gracefully shutting down...");
                Thread.currentThread().interrupt();
            } else {
                log.error("Initial data load failed: {}", e.getMessage());
            }
        }
    }
}