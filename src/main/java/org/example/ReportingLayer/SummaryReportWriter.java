package org.example.ReportingLayer;

import org.example.models.BlockReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryReportWriter implements BlockListener {
    private static final Logger log = LoggerFactory.getLogger(SummaryReportWriter.class);
    @Override
    public void onBlock(BlockReport report) {}

    public void writeReport(/*StatsSnapshot*/) {
        log.info("Much Report. Such information. Wow.");
    }
}
