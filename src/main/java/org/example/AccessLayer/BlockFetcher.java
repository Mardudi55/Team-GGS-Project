package org.example.AccessLayer;

import org.example.models.BlockData;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;

public class BlockFetcher {

    private final Web3j web3j;

    public BlockFetcher(Web3jClient client) {

        this.web3j = client.getWeb3j();
    }

    public List<BlockData> fetchLatestBlocks(int count) {

        List<BlockData> blocks = new ArrayList<>();

        try {

            long latest =
                    web3j.ethBlockNumber()
                            .send()
                            .getBlockNumber()
                            .longValue();

            long start = latest - count + 1;

            for (long i = latest; i >= start; i--) {

                EthBlock response =
                        web3j.ethGetBlockByNumber(
                                DefaultBlockParameter.valueOf(
                                        java.math.BigInteger.valueOf(i)
                                ),
                                false
                        ).send();

                EthBlock.Block block =
                        response.getBlock();

                blocks.add(new BlockData(
                        block.getNumber().longValue(),
                        block.getHash(),
                        block.getTransactions().size(),
                        block.getTimestamp().longValue()
                ));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return blocks;
    }
}