package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.example.models.StatsSnapshot;
import org.example.models.TransactionData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Accumulates statistics across multiple incoming block reports.
 * It maintains the running totals and calculates averages on demand.
 */
public class StatsAccumulator implements BlockListener {
    private long totalBlocks = 0;
    private long totalTransactions = 0;
    private BigDecimal totalValueEth = BigDecimal.ZERO;
    private BigDecimal totalGasUsed = BigDecimal.ZERO;

    /**
     * Updates internal counters and accumulators based on the incoming block report.
     *
     * @param report the block report containing new data to accumulate
     */
    @Override
    public void onBlock(BlockReport report) {
        totalBlocks++;
        totalTransactions += report.getBlock().getTransactionCount();

        for (TransactionData tx : report.getTransactions()) {
            totalValueEth = totalValueEth.add(tx.getValueEth());
            totalGasUsed = totalGasUsed.add(BigDecimal.valueOf(tx.getGasUsed()));
        }
    }

    /**
     * Generates a point-in-time snapshot of the accumulated statistics.
     *
     * @return a new StatsSnapshot object containing current calculated values
     */
    public StatsSnapshot getSnapshot() {
        BigDecimal avgGasUsed = BigDecimal.ZERO;
        if (totalTransactions > 0) {
            avgGasUsed = totalGasUsed.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP);
        }

        return new StatsSnapshot(
                totalBlocks,
                totalTransactions,
                totalValueEth,
                avgGasUsed,
                LocalDateTime.now()
        );
    }

    /**
     * Resets all internal accumulators back to zero.
     */
    public void reset() {
        totalBlocks = 0;
        totalTransactions = 0;
        totalValueEth = BigDecimal.ZERO;
        totalGasUsed = BigDecimal.ZERO;
    }
}