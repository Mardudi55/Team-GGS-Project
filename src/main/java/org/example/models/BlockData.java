package org.example.models;

/**
 * Data model representing a simplified Ethereum block.
 * Aligned with the exact UML specifications.
 */
public class BlockData {
    private final long blockNumber;
    private final String blockHash;
    private final int transactionCount;
    private final long timestamp;

    public BlockData(long blockNumber, String blockHash, int transactionCount, long timestamp) {
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.transactionCount = transactionCount;
        this.timestamp = timestamp;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}