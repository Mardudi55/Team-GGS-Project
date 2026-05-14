package org.example.BusinessLogicLayer;

import org.example.models.BlockData;
import org.example.models.BlockReport;
import org.example.models.TransactionData;
import java.math.BigDecimal;
import java.util.List;

/**
 * Processes raw block and transaction data to generate comprehensive block reports.
 * Acts as an orchestrator within the BusinessLogicLayer, utilizing {@link StatisticsCalculator}
 * to compute necessary aggregated network statistics.
 */
public class BlockProcessor {

    private final StatisticsCalculator calculator;

    /**
     * Constructs a new {@code BlockProcessor} and initializes its underlying
     * {@link StatisticsCalculator} dependency for statistical computations.
     */
    public BlockProcessor() {
        this.calculator = new StatisticsCalculator();
    }

    /**
     * Processes the given block data and its associated transactions to create a summarized report.
     * It automatically calculates the total Ethereum value transferred and the average gas used
     * for the provided transaction list.
     *
     * @param blockData    the core {@link BlockData} containing block metadata.
     * @param transactions a {@link List} of {@link TransactionData} associated with the block.
     * @return a fully constructed {@link BlockReport} containing the original data and calculated statistics,
     * or {@code null} if the provided {@code blockData} is {@code null}.
     */
    public BlockReport process(BlockData blockData, List<TransactionData> transactions) {
        if (blockData == null) {
            return null;
        }

        BigDecimal totalValueEth = calculator.calcTotalValueEth(transactions);
        BigDecimal avgGasUsed = calculator.calcAvgGasUsed(transactions);

        return new BlockReport(
                blockData,
                transactions,
                totalValueEth,
                avgGasUsed
        );
    }
}