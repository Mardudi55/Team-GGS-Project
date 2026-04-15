package org.example.model;

import java.math.BigDecimal;

/**
 * Data model representing a simplified Ethereum transaction.
 * Aligned with the exact UML specifications.
 */
public class TransactionData {
    private final String txHash;
    private final String sender;
    private final String receiver;
    private final BigDecimal valueEth;
    private final long gasUsed;
    private final long gasPrice;

    public TransactionData(String txHash, String sender, String receiver, BigDecimal valueEth, long gasUsed, long gasPrice) {
        this.txHash = txHash;
        this.sender = sender;
        this.receiver = receiver;
        this.valueEth = valueEth;
        this.gasUsed = gasUsed;
        this.gasPrice = gasPrice;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public BigDecimal getValueEth() {
        return valueEth;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public long getGasPrice() {
        return gasPrice;
    }
}