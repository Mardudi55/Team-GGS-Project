package org.example.models;

import java.util.List;

/**
 * Aggregates block data along with its associated detailed transactions.
 */
public class BlockReport {
    private final BlockData block;
    private final List<TransactionData> transactions;

    /**
     * Constructs a new BlockReport instance.
     *
     * @param block        the summary data of the block
     * @param transactions the list of detailed transactions within the block
     */
    public BlockReport(BlockData block, List<TransactionData> transactions) {
        this.block = block;
        this.transactions = List.copyOf(transactions);
    }

    public BlockData getBlock() { return block; }
    public List<TransactionData> getTransactions() { return transactions; }
}