package org.example.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregates block data along with its associated detailed transactions.
 */
public class BlockReport {
    private final BlockData block;
    private final List<TransactionData> transactions;
    private final BigDecimal totalValueEth;
    private final BigDecimal avgGasUsed;

    /**
     * Constructs a new BlockReport instance with calculated metrics.
     *
     * @param block         the summary data of the block
     * @param transactions  the list of detailed transactions within the block
     * @param totalValueEth total ETH transferred in the block
     * @param avgGasUsed    average gas used by transactions
     */
    public BlockReport(BlockData block, List<TransactionData> transactions, BigDecimal totalValueEth, BigDecimal avgGasUsed) {
        this.block = block;
        this.transactions = List.copyOf(transactions);
        this.totalValueEth = totalValueEth;
        this.avgGasUsed = avgGasUsed;
    }

    /**
     * Constructs a new BlockReport instance (legacy/mock support).
     *
     * @param block        the summary data of the block
     * @param transactions the list of detailed transactions within the block
     */
    public BlockReport(BlockData block, List<TransactionData> transactions) {
        this.block = block;
        this.transactions = List.copyOf(transactions);
        this.totalValueEth = BigDecimal.ZERO;
        this.avgGasUsed = BigDecimal.ZERO;
    }

    public BlockData getBlock() { return block; }
    public List<TransactionData> getTransactions() { return transactions; }
    public BigDecimal getTotalValueEth() { return totalValueEth; }
    public BigDecimal getAvgGasUsed() { return avgGasUsed; }
}