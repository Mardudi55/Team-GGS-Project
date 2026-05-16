package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches and maps Ethereum transactions from a given block via JSON-RPC.
 */
public class TransactionFetcher {

    private final Web3j web3j;

    /**
     * Constructs fetcher using transport from provided client.
     *
     * @param client connected {@link Web3jClient}; must not be {@code null}
     */
    public TransactionFetcher(Web3jClient client) {
        this.web3j = client.getWeb3j();
    }

    /**
     * Retrieves all transactions in the specified block, enriching each
     * with receipt data (gas used) and converting value from Wei to Ether.
     *
     * <p>For each transaction, an additional RPC call ({@code eth_getTransactionReceipt})
     * is made — complexity is O(n) in transaction count.
     *
     * <p>If a receipt is absent, {@code gasUsed} defaults to {@code 0}.
     *
     * @param blockData block descriptor containing the target block number
     * @return ordered list of {@link TransactionData} mapped from the block;
     *         empty list if block contains no transactions
     * @throws IOException      if any RPC call fails at transport level
     */
    public List<TransactionData> fetchTransactions(BlockData blockData) throws IOException {
        List<TransactionData> list = new ArrayList<>();

        EthBlock response = web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockData.getBlockNumber())),
                true
        ).send();

        EthBlock.Block block = response.getBlock();

        for (EthBlock.TransactionResult<?> tx : block.getTransactions()) {

            Transaction transaction = (Transaction) tx.get();
            EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transaction.getHash()).send();
            BigInteger gasUsed = BigInteger.ZERO;

            if (receipt.getTransactionReceipt().isPresent()) {
                gasUsed = receipt.getTransactionReceipt()
                        .get()
                        .getGasUsed();
            }

            BigDecimal valueEth = Convert.fromWei(
                    new BigDecimal(transaction.getValue()),
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

        return list;
    }
}