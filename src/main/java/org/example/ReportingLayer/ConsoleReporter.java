package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener implementation that logs real-time information about newly processed
 * blocks to the standard console output. Transaction details have been omitted
 * to provide a cleaner, multi-line block summary.
 */
public class ConsoleReporter implements BlockListener {
    private final Logger log = LoggerFactory.getLogger(ConsoleReporter.class);

    /**
     * Logs the summarized block details to the console across multiple lines.
     *
     * @param report the block report to be printed to the console
     */
    @Override
    public void onBlock(BlockReport report) {
        log.info(formatBlock(report));
    }

    /**
     * Formats the primary block data into a multi-line readable string containing
     * the block number, hash, and total transaction count.
     *
     * @param report the block report containing block data
     * @return a formatted multi-line string representing the block
     */
    private String formatBlock(BlockReport report) {
        return String.format("\n  BLOCK #%d\n  Hash: %s\n  Txs count: %d",
                report.getBlock().getBlockNumber(),
                report.getBlock().getBlockHash(),
                report.getBlock().getTransactionCount());
    }
}