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

    // Dependencies to be injected later by the team
    private AccessLayerFacade accessLayer = null;
    private BlockProcessor blockProcessor = null;

    // Reporting layer components owned by MonitorApp (as per UML diagram)
    private final StatsAccumulator statsAccumulator;
    private final SummaryReportWriter reportWriter;
    private final ConsoleReporter consoleReporter;

    private final List<BlockListener> listeners = new ArrayList<>();

    private volatile boolean running = true;
    private CountDownLatch shutdownLatch = null;

    // Temporary field for mock generation
    private long mockBlockNumber = 1000;

    /**
     * Initializes the MonitorApp and registers all required observers.
     */
    public MonitorApp() {
        log.info("Setting up the Monitor App...");

        this.statsAccumulator = new StatsAccumulator();
        this.reportWriter = new SummaryReportWriter("report.txt");
        this.consoleReporter = new ConsoleReporter();

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
        log.info("Starting the polling loop. Press CTRL+C to perform a graceful shutdown and generate the report...");
        final int POLLING_INTERVAL_MS = 3000;

        while (running) {
            try {
                // TODO: Replace this mock block with real AccessLayer logic in the future
                // List<BlockData> blocks = accessLayer.fetchLatestBlocks(1);
                // List<BlockReport> reports = blockProcessor.process(blocks);

                BlockReport mockReport = generateMockBlock();
                notifyListeners(mockReport);

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

            // TODO: Uncomment when AccessLayer is ready
            // if (accessLayer != null) accessLayer.disconnect();

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
     * Temporary helper method to simulate the business logic layer output.
     * * @return a mock BlockReport containing dummy transaction data
     */
    private BlockReport generateMockBlock() {
        mockBlockNumber++;
        BlockData block = new BlockData(mockBlockNumber, "0xabc" + mockBlockNumber, 2, Instant.now().getEpochSecond());
        List<TransactionData> txs = Arrays.asList(
                new TransactionData("0x111...", "0xAAA", "0xBBB", new BigDecimal("1.5"), 21000, 50),
                new TransactionData("0x222...", "0xCCC", "0xDDD", new BigDecimal("0.5"), 21000, 55)
        );
        return new BlockReport(block, txs);
    }
}