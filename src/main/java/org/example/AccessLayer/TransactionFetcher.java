package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TransactionFetcher {

    private final Web3j web3j;

    public TransactionFetcher(Web3jClient client) {

        this.web3j = client.getWeb3j();
    }

    public List<TransactionData> fetchTransactions(
            BlockData blockData) {

        List<TransactionData> list =
                new ArrayList<>();

        try {

            EthBlock response =
                    web3j.ethGetBlockByNumber(
                            DefaultBlockParameter.valueOf(
                                    BigInteger.valueOf(
                                            blockData.getBlockNumber()
                                    )
                            ),
                            true
                    ).send();

            EthBlock.Block block =
                    response.getBlock();

            for (EthBlock.TransactionResult<?> tx :
                    block.getTransactions()) {

                Transaction transaction =
                        (Transaction) tx.get();

                EthGetTransactionReceipt receipt =
                        web3j.ethGetTransactionReceipt(
                                transaction.getHash()
                        ).send();

                BigInteger gasUsed =
                        BigInteger.ZERO;

                if (receipt.getTransactionReceipt()
                        .isPresent()) {

                    gasUsed =
                            receipt.getTransactionReceipt()
                                    .get()
                                    .getGasUsed();
                }

                BigDecimal valueEth =
                        Convert.fromWei(
                                new BigDecimal(
                                        transaction.getValue()
                                ),
                                Convert.Unit.ETHER
                        );

                list.add(new TransactionData(
                        transaction.getHash(),
                        transaction.getFrom(),
                        transaction.getTo(),
                        valueEth,
                        gasUsed.longValue(),
                        transaction.getGasPrice().longValue()
                ));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }
}