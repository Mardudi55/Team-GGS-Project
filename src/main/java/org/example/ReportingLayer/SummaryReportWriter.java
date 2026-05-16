package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.example.models.StatsSnapshot;
import org.example.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects block reports in memory and writes a comprehensive summary report
 * to a specified text file upon request, typically during application shutdown.
 */
public class SummaryReportWriter implements BlockListener {

    private static final Logger log = LoggerFactory.getLogger(SummaryReportWriter.class);
    private final List<BlockReport> buffer = new ArrayList<>();
    private final String outputFilePath;

    public SummaryReportWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    @Override
    public void onBlock(BlockReport report) {
        buffer.add(report);
    }

    /**
     * Writes the collected statistics and block summaries to the output file.
     * Generates extended block-level statistics for the last 10 blocks and
     * a compact, comma-separated list of block numbers for the remaining history.
     *
     * @param snapshot the overall statistics snapshot
     */
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
        int startIdx10 = Math.max(0, totalBuffered - 10);

        if (startIdx10 > 0) {
            sb.append("--- PROCESSED BLOCKS HISTORY ---\n");
            String basicBlocks = buffer.subList(0, startIdx10).stream()
                    .map(r -> String.valueOf(r.getBlock().getBlockNumber()))
                    .collect(Collectors.joining(", "));
            sb.append("Block numbers: ").append(basicBlocks).append("\n\n");
        }

        int detailedBlocksCount = totalBuffered - startIdx10;
        sb.append("--- EXTENDED BLOCKS (LAST ").append(detailedBlocksCount).append(" BLOCKS) ---\n\n");

        for (int i = startIdx10; i < totalBuffered; i++) {
            BlockReport report = buffer.get(i);
            List<TransactionData> transactions = report.getTransactions();

            BigDecimal totalEth = BigDecimal.ZERO;
            long totalGas = 0;
            long avgGas = 0;

            if (transactions != null && !transactions.isEmpty()) {
                for (TransactionData tx : transactions) {
                    try {
                        totalEth = totalEth.add(new BigDecimal(String.valueOf(tx.getValueEth())));
                        totalGas += Long.parseLong(String.valueOf(tx.getGasUsed()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                avgGas = totalGas / transactions.size();
            }

            sb.append(String.format("Block #%d | Hash: %s\n",
                    report.getBlock().getBlockNumber(),
                    report.getBlock().getBlockHash()));
            sb.append(String.format("  Transactions: %d\n", report.getBlock().getTransactionCount()));
            sb.append(String.format("  Total Value:  %s ETH\n", totalEth.toPlainString()));
            sb.append(String.format("  Average Gas:  %d\n\n", avgGas));
        }

        try {
            Path path = Paths.get(outputFilePath);
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            log.info("Summary report successfully written to: {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write the summary report: {}", e.getMessage());
        }
    }
}