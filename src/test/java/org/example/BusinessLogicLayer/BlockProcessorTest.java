package org.example.BusinessLogicLayer;

import org.example.BusinessLogicLayer.StatisticsCalculator;
import org.example.models.BlockReport;
import org.example.models.TransactionData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockProcessorTest{

    private final StatisticsCalculator calculator = new StatisticsCalculator();

    @Test
    void shouldReturnZeroWhenReportsIsNull() {
        long result = calculator.calcTotalTransactions(null);
        assertEquals(0, result);
    }

    @Test
    void shouldCalculateTotalTransactions() {
        TransactionData tx1 = new TransactionData("1", "A", "B", BigDecimal.ONE, 100, 1);
        TransactionData tx2 = new TransactionData("2", "A", "B", BigDecimal.ONE, 100, 1);
        TransactionData tx3 = new TransactionData("3", "A", "B", BigDecimal.ONE, 100, 1);

        BlockReport report1 = new BlockReport(null, List.of(tx1, tx2));
        BlockReport report2 = new BlockReport(null, List.of(tx3));

        long result = calculator.calcTotalTransactions(List.of(report1, report2));

        assertEquals(3, result);
    }

    @Test
    void shouldCalculateAverageGasUsed() {
        TransactionData tx1 = new TransactionData("1", "A", "B", BigDecimal.ONE, 100, 1);
        TransactionData tx2 = new TransactionData("2", "A", "B", BigDecimal.ONE, 200, 1);

        BigDecimal result = calculator.calcAvgGasUsed(List.of(tx1, tx2));

        assertEquals(new BigDecimal("150.00"), result);
    }

    @Test
    void shouldIgnoreZeroValueTransactionsInAvgGas() {
        TransactionData tx1 = new TransactionData("1", "A", "B", BigDecimal.ZERO, 100, 1); // ignorowany
        TransactionData tx2 = new TransactionData("2", "A", "B", BigDecimal.ONE, 200, 1);

        BigDecimal result = calculator.calcAvgGasUsed(List.of(tx1, tx2));

        assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void shouldCalculateTotalValueEth() {
        TransactionData tx1 = new TransactionData("1", "A", "B", new BigDecimal("1.5"), 100, 1);
        TransactionData tx2 = new TransactionData("2", "A", "B", new BigDecimal("2.5"), 100, 1);

        BigDecimal result = calculator.calcTotalValueEth(List.of(tx1, tx2));

        assertEquals(new BigDecimal("4.0"), result);
    }
}