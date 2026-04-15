package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StatsSnapshot {
    private final long totalBlocks;
    private final long totalTransactions;
    private final BigDecimal totalValueEth;
    private final BigDecimal avgGasUsed;
    private final LocalDateTime generatedAt;

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