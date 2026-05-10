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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Orchestrates the fetching, processing, and reporting of blockchain data.
 */
public class MonitorApp {
    private static final Logger log = LoggerFactory.getLogger(MonitorApp.class);

    private AccessLayerFacade accessLayer = null;
    private BlockProcessor blockProcessor = null;

    private final StatsAccumulator statsAccumulator;
    private final SummaryReportWriter reportWriter;
    private final ConsoleReporter consoleReporter;
    private final List<BlockListener> listeners = new ArrayList<>();

    private volatile boolean running = true;
    private CountDownLatch shutdownLatch = null;

    private long lastProcessedBlock = -1;

    /**
     * Initializes the MonitorApp and registers all required observers.
     */
    public MonitorApp() {
        log.info("Setting up the Monitor App...");

        this.accessLayer = new AccessLayerFacade();
        this.statsAccumulator = new StatsAccumulator();
        this.reportWriter = new SummaryReportWriter("report.txt");
        this.consoleReporter = new ConsoleReporter();
        this.blockProcessor = new BlockProcessor();

        addListener(this.statsAccumulator);
        addListener(this.reportWriter);
        addListener(this.consoleReporter);

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
                        latestBlocks.get(0);

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
        if (!running) return; // Prevent multiple executions

        log.info("Initiating graceful shutdown...");
        running = false;

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

        log.info("Loading last 100 blocks...");

        try {

            List<BlockData> blocks =
                    accessLayer.fetchLatestBlocks(100);

            int limit = Math.min(10, blocks.size());

            for (int i = 0; i < limit && running; i++) {

                if (!running) {

                    log.info("Stopping historical block processing...");
                    break;
                }

                BlockData block = blocks.get(i);

                List<TransactionData> transactions =
                        accessLayer.fetchTransactions(block);

                BlockReport report =
                        blockProcessor.process(
                                block,
                                transactions
                        );

                if (report != null) {

                    notifyListeners(report);
                }

                log.info("Processed historical block: {}",
                        block.getBlockNumber());
            }
            if (!blocks.isEmpty()) {

                lastProcessedBlock =
                        blocks.get(0).getBlockNumber();
            }

        } catch (Exception e) {

            log.error("Initial data load failed: {}",
                    e.getMessage());
        }
    }
}