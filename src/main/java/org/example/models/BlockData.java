package org.example.models;

/**
 * Represents the essential data of a blockchain block.
 */
public class BlockData {
    private final long blockNumber;
    private final String blockHash;
    private final int transactionCount;
    private final long timestamp;

    /**
     * Constructs a new BlockData instance.
     *
     * @param blockNumber      the sequential number of the block
     * @param blockHash        the unique cryptographic hash of the block
     * @param transactionCount the total number of transactions included in this block
     * @param timestamp        the Unix timestamp indicating when the block was mined
     */
    public BlockData(long blockNumber, String blockHash, int transactionCount, long timestamp) {
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.transactionCount = transactionCount;
        this.timestamp = timestamp;
    }

    public long getBlockNumber() { return blockNumber; }
    public String getBlockHash() { return blockHash; }
    public int getTransactionCount() { return transactionCount; }
    public long getTimestamp() { return timestamp; }
}