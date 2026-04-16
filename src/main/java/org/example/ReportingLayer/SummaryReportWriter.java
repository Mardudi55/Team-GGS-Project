package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.example.models.StatsSnapshot;

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

    /**
     * Constructs a new SummaryReportWriter.
     *
     * @param outputFilePath the absolute or relative path where the report file should be saved
     */
    public SummaryReportWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    /**
     * Adds the incoming block report to the internal memory buffer.
     *
     * @param report the block report to buffer for later writing
     */
    @Override
    public void onBlock(BlockReport report) {
        buffer.add(report);
    }

    /**
     * Formats the collected statistics and buffered blocks, then writes them to the file system.
     *
     * @param snapshot the final calculated statistics snapshot to include in the report
     */
    public void writeReport(StatsSnapshot snapshot) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("=================================================\n");
        sb.append("             FINAL MONITORING REPORT             \n");
        sb.append("=================================================\n");
        sb.append("Generated at: ").append(snapshot.getGeneratedAt().format(formatter)).append("\n\n");

        sb.append("--- STATISTICS SUMMARY ---\n");
        sb.append("Total blocks processed:      ").append(snapshot.getTotalBlocks()).append("\n");
        sb.append("Total transactions processed: ").append(snapshot.getTotalTransactions()).append("\n");
        sb.append("Total value transferred:      ").append(snapshot.getTotalValueEth()).append(" ETH\n");
        sb.append("Average gas used:             ").append(snapshot.getAvgGasUsed()).append("\n\n");

        sb.append("--- PROCESSED BLOCKS HISTORY ---\n");
        for (BlockReport report : buffer) {
            sb.append(String.format("Block #%d | Txs: %d | Hash: %s\n",
                    report.getBlock().getBlockNumber(),
                    report.getBlock().getTransactionCount(),
                    report.getBlock().getBlockHash()));
        }

        try {
            Path path = Paths.get(outputFilePath);
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to write the summary report: " + e.getMessage());
        }
    }
}