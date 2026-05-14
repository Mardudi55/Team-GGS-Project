package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.example.models.StatsSnapshot;
import org.example.models.TransactionData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects block reports in memory and writes a comprehensive summary report
 * to a specified text file upon request, typically during application shutdown.
 */
public class SummaryReportWriter implements BlockListener {
    private final List<BlockReport> buffer = new ArrayList<>();
    private final String outputFilePath;

    public SummaryReportWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    @Override
    public void onBlock(BlockReport report) {
        buffer.add(report);
    }

    public void writeReport(StatsSnapshot snapshot) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("=================================================\n");
        sb.append("             FINAL MONITORING REPORT             \n");
        sb.append("=================================================\n");
        sb.append("Generated at: ").append(snapshot.getGeneratedAt().format(formatter)).append("\n\n");

        sb.append("--- STATISTICS SUMMARY ---\n");
        sb.append("Total blocks processed:       ").append(snapshot.getTotalBlocks()).append("\n");
        sb.append("Total transactions processed: ").append(snapshot.getTotalTransactions()).append("\n");
        sb.append("Total value transferred:      ").append(snapshot.getTotalValueEth()).append(" ETH\n");
        sb.append("Average gas used:             ").append(snapshot.getAvgGasUsed()).append("\n\n");

        int totalBuffered = buffer.size();

        int startIdx100 = Math.max(0, totalBuffered - 100);
        int blocksHistoryCount = totalBuffered - startIdx100;

        sb.append("--- PROCESSED BLOCKS HISTORY (LAST ").append(blocksHistoryCount).append(") ---\n");
        for (int i = startIdx100; i < totalBuffered; i++) {
            BlockReport report = buffer.get(i);
            sb.append(String.format("Block #%d | Txs: %d | Hash: %s\n",
                    report.getBlock().getBlockNumber(),
                    report.getBlock().getTransactionCount(),
                    report.getBlock().getBlockHash()));
        }

        int startIdx10 = Math.max(0, totalBuffered - 10);
        int detailedBlocksCount = totalBuffered - startIdx10;

        sb.append("\n--- DETAILED TRANSACTIONS (LAST ").append(detailedBlocksCount).append(" BLOCKS) ---\n");
        sb.append("Note: Showing only the last 10 transactions per block for better readability.\n\n");

        for (int i = startIdx10; i < totalBuffered; i++) {
            BlockReport report = buffer.get(i);
            sb.append(String.format("Block #%d Details:\n", report.getBlock().getBlockNumber()));

            List<TransactionData> transactions = report.getTransactions();
            if (transactions == null || transactions.isEmpty()) {
                sb.append("  [No transactions in this block]\n");
            } else {
                int txCount = transactions.size();
                int startTxIdx = Math.max(0, txCount - 10);

                if (txCount > 10) {
                    sb.append(String.format("  (Displaying last 10 out of %d transactions)\n", txCount));
                }

                for (int j = startTxIdx; j < txCount; j++) {
                    TransactionData tx = transactions.get(j);
                    sb.append(String.format("  - TxHash: %s | From: %s | To: %s | Value: %s ETH | Gas: %s\n",
                            tx.getTxHash(),
                            tx.getSender(),
                            tx.getReceiver(),
                            tx.getValueEth(),
                            tx.getGasUsed()));
                }
            }
            sb.append("\n");
        }

        try {
            Path path = Paths.get(outputFilePath);
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            System.out.println("Summary report successfully written to: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write the summary report: " + e.getMessage());
        }
    }
}