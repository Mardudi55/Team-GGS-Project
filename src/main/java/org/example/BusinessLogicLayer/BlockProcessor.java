package org.example.BusinessLogicLayer;

import org.example.models.BlockData;
import org.example.models.BlockReport;
import org.example.models.TransactionData;

import java.math.BigDecimal;
import java.util.List;

public class BlockProcessor {

    private final StatisticsCalculator calculator;

    public BlockProcessor() {
        this.calculator = new StatisticsCalculator();
    }

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