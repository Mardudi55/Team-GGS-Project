package org.example.models;

import java.math.BigDecimal;

/**
 * Represents detailed information about a single blockchain transaction.
 */
public class TransactionData {
    private final String txHash;
    private final String sender;
    private final String receiver;
    private final BigDecimal valueEth;
    private final long gasUsed;
    private final long gasPrice;

    /**
     * Constructs a new TransactionData instance.
     *
     * @param txHash   the unique cryptographic hash of the transaction
     * @param sender   the public address of the sender
     * @param receiver the public address of the receiver
     * @param valueEth the amount of Ethereum transferred, represented as a BigDecimal
     * @param gasUsed  the amount of gas consumed by the transaction execution
     * @param gasPrice the price of gas in Wei at the time of execution
     */
    public TransactionData(String txHash, String sender, String receiver, BigDecimal valueEth, long gasUsed, long gasPrice) {
        this.txHash = txHash;
        this.sender = sender;
        this.receiver = receiver;
        this.valueEth = valueEth;
        this.gasUsed = gasUsed;
        this.gasPrice = gasPrice;
    }

    public String getTxHash() { return txHash; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public BigDecimal getValueEth() { return valueEth; }
    public long getGasUsed() { return gasUsed; }
    public long getGasPrice() { return gasPrice; }
}