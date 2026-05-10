package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;

import java.util.List;

public class AccessLayerFacade {

    private final Web3jClient client;
    private final BlockFetcher blockFetcher;
    private final TransactionFetcher transactionFetcher;
    private final RateLimitHandler rateLimitHandler;

    public AccessLayerFacade() {

        this.client =
                new Web3jClient(
                        "https://eth-sepolia.g.alchemy.com/v2/meML7wFPg5fL9WPRSVUqa"
                );

        this.blockFetcher =
                new BlockFetcher(client);

        this.transactionFetcher =
                new TransactionFetcher(client);

        this.rateLimitHandler =
                new RateLimitHandler(5);
    }

    public boolean isConnected() {

        return client.isConnected();
    }

    public List<BlockData> fetchLatestBlocks(
            int count) {

        try {
            rateLimitHandler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return blockFetcher.fetchLatestBlocks(
                count
        );
    }

    public List<TransactionData> fetchTransactions(
            BlockData block) {

        try {
            rateLimitHandler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return transactionFetcher.fetchTransactions(
                block
        );
    }

    public void disconnect() {

        client.disconnect();
    }
}