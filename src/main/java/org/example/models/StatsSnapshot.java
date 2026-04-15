package org.example.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an aggregated snapshot of application statistics at a specific point in time.
 */
public class StatsSnapshot {
    private final long totalBlocks;
    private final long totalTransactions;
    private final BigDecimal totalValueEth;
    private final BigDecimal avgGasUsed;
    private final LocalDateTime generatedAt;

    /**
     * Constructs a new StatsSnapshot instance.
     *
     * @param totalBlocks       the total number of blocks processed so far
     * @param totalTransactions the total number of transactions processed so far
     * @param totalValueEth     the cumulative value of all transactions in ETH
     * @param avgGasUsed        the average amount of gas used across all transactions
     * @param generatedAt       the exact date and time this snapshot was created
     */
    public StatsSnapshot(long totalBlocks, long totalTransactions, BigDecimal totalValueEth, BigDecimal avgGasUsed, LocalDateTime generatedAt) {
        this.totalBlocks = totalBlocks;
        this.totalTransactions = totalTransactions;
        this.totalValueEth = totalValueEth;
        this.avgGasUsed = avgGasUsed;
        this.generatedAt = generatedAt;
    }

    public long getTotalBlocks() { return totalBlocks; }
    public long getTotalTransactions() { return totalTransactions; }
    public BigDecimal getTotalValueEth() { return totalValueEth; }
    public BigDecimal getAvgGasUsed() { return avgGasUsed; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}