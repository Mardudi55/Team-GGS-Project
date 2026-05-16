package org.example.AccessLayer;

import org.example.models.BlockData;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;

/**
 * Fetches Ethereum block metadata via JSON-RPC without loading full transaction bodies.
 */
class BlockFetcher {

    private final Web3j web3j;

    /**
     * Constructs fetcher using transport from provided client.
     *
     * @param client connected {@link Web3jClient}; must not be {@code null}
     */
    public BlockFetcher(Web3jClient client) {
        this.web3j = client.getWeb3j();
    }

    /**
     * Retrieves the latest canonical block number ({@code eth_blockNumber}).
     *
     * @return latest block number as {@code long}
     * @throws IOException if RPC call fails at transport level
     */
    public long fetchLatestBlockNumber() throws IOException {
        return web3j.ethBlockNumber()
                .send()
                .getBlockNumber()
                .longValue();
    }

    /**
     * Fetches block metadata for the given block number ({@code eth_getBlockByNumber}).
     * Transaction bodies are not hydrated; only transaction count is recorded.
     *
     * @param blockNumber target block number; must be &gt;= 0
     * @return {@link BlockData} containing number, hash, transaction count, and Unix timestamp
     * @throws IOException              if RPC call fails at transport level
     * @throws NullPointerException     if returned block is {@code null} (unknown block number)
     */
    public BlockData fetchBlock(long blockNumber) throws IOException {
        EthBlock response = web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(java.math.BigInteger.valueOf(blockNumber)),
                false
        ).send();

        EthBlock.Block block = response.getBlock();

        return new BlockData(
                block.getNumber().longValue(),
                block.getHash(),
                block.getTransactions().size(),
                block.getTimestamp().longValue()
        );
    }
}