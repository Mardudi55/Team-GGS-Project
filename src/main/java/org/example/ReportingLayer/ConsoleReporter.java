package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.example.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener implementation that logs real-time information about newly processed
 * blocks and their transactions to the standard console output.
 */
public class ConsoleReporter implements BlockListener {
    private final Logger log = LoggerFactory.getLogger(ConsoleReporter.class);

    /**
     * Logs the block details and iterates over its transactions to log them individually.
     *
     * @param report the block report to be printed to the console
     */
    @Override
    public void onBlock(BlockReport report) {
        log.info(formatBlock(report));
        for (TransactionData tx : report.getTransactions()) {
            log.info(formatTransaction(tx));
        }
    }

    /**
     * Formats the primary block data into a readable string.
     *
     * @param report the block report containing block data
     * @return a formatted string representing the block
     */
    private String formatBlock(BlockReport report) {
        return String.format("BLOCK #%d | Hash: %s | Txs: %d",
                report.getBlock().getBlockNumber(),
                report.getBlock().getBlockHash(),
                report.getBlock().getTransactionCount());
    }

    /**
     * Formats the individual transaction data into a readable string.
     *
     * @param tx the transaction data to format
     * @return a formatted string representing the transaction
     */
    private String formatTransaction(TransactionData tx) {
        return String.format("  -> TX: %s | %s -> %s | Value: %s ETH",
                tx.getTxHash(), tx.getSender(), tx.getReceiver(), tx.getValueEth());
    }
}