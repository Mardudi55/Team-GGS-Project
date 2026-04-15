package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import java.math.BigDecimal;

public class Mapper {

    private Mapper() {}

    public static BlockData mapBlock(EthBlock.Block block) {
        if (block == null) return null;

        return new BlockData(
                block.getNumber().longValue(),
                block.getHash(),
                block.getTransactions() != null ? block.getTransactions().size() : 0,
                block.getTimestamp().longValue()
        );
    }

    public static TransactionData mapTransaction(Transaction transaction) {
        if (transaction == null) return null;

        BigDecimal valueInEther = Convert.fromWei(new BigDecimal(transaction.getValue()), Convert.Unit.ETHER);

        return new TransactionData(
                transaction.getHash(),
                transaction.getFrom(),
                transaction.getTo(),
                valueInEther,
                transaction.getGas().longValue(),
                transaction.getGasPrice().longValue()
        );
    }
}