package org.example.model;

import java.util.List;

/**
 * Data model representing a block alongside its executed transactions.
 * Aligned with the exact UML specifications.
 */
public class BlockReport {

    private final BlockData block;
    private final List<TransactionData> transactions;

    public BlockReport(BlockData block, List<TransactionData> transactions) {
        this.block = block;
        this.transactions = transactions;
    }

    public BlockData getBlock() {
        return block;
    }

    public List<TransactionData> getTransactions() {
        return transactions;
    }
}