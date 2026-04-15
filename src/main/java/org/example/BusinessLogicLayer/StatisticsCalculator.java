package org.example.BusinessLogicLayer;

import org.example.model.BlockReport;
import org.example.model.TransactionData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculates aggregated statistics for Ethereum network data.
 * Resides in the BusinessLogicLayer as defined by the system architecture.
 */
public class StatisticsCalculator {

    /**
     * Calculates the total number of valid transactions across multiple block reports.
     *
     * @param reports a {@link List} of {@link BlockReport} objects.
     * @return the total number of transactions.
     */
    public long calcTotalTransactions(List<BlockReport> reports) {
        if (reports == null) return 0;

        return reports.stream()
                .filter(report -> report.getTransactions() != null)
                .mapToLong(report -> report.getTransactions().size())
                .sum();
    }

    /**
     * Calculates the average gas used by a list of valid transactions.
     * Excludes transactions with a 0 ETH value.
     *
     * @param txs a {@link List} of {@link TransactionData} to be processed.
     * @return a {@link BigDecimal} representing the average gas used.
     */
    public BigDecimal calcAvgGasUsed(List<TransactionData> txs) {
        if (txs == null || txs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Filtrujemy puste transakcje przed wyliczeniem
        List<TransactionData> validTxs = getValidTransactions(txs);
        if (validTxs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long totalGas = validTxs.stream()
                .mapToLong(TransactionData::getGasUsed)
                .sum();

        return BigDecimal.valueOf(totalGas)
                .divide(BigDecimal.valueOf(validTxs.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the total Ethereum value transferred in a list of valid transactions.
     * Excludes transactions with a 0 ETH value.
     *
     * @param txs a {@link List} of {@link TransactionData} to be processed.
     * @return a {@link BigDecimal} representing the total value in ETH.
     */
    public BigDecimal calcTotalValueEth(List<TransactionData> txs) {
        if (txs == null || txs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return getValidTransactions(txs).stream()
                .map(TransactionData::getValueEth)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Helper method to filter out invalid/empty transactions (value <= 0).
     */
    private List<TransactionData> getValidTransactions(List<TransactionData> txs) {
        return txs.stream()
                .filter(tx -> tx != null)
                .filter(tx -> tx.getValueEth() != null && tx.getValueEth().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }
}