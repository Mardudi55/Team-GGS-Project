package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade over Ethereum RPC access layer.
 * Combines rate limiting and retry logic for block and transaction fetches.
 *
 * <p>Hardcoded target: Sepolia testnet via Alchemy.
 * Max retries: {@value #maxRetries} per operation.
 */
public class AccessLayerFacade {

    private static final Logger log = LoggerFactory.getLogger(AccessLayerFacade.class);
    private final Web3jClient client;
    private final BlockFetcher blockFetcher;
    private final TransactionFetcher transactionFetcher;
    private final RateLimitHandler rateLimitHandler;
    private static final int maxRetries = 2;

    /**
     * Constructs facade, wires fetchers, and starts rate limiter.
     * Connects to Sepolia RPC endpoint on construction.
     */
    public AccessLayerFacade() {
        this.client = new Web3jClient("https://eth-sepolia.g.alchemy.com/v2/meML7wFPg5fL9WPRSVUqa");

        this.blockFetcher = new BlockFetcher(client);
        this.transactionFetcher = new TransactionFetcher(client);
        this.rateLimitHandler = new RateLimitHandler(1, 200);
        this.rateLimitHandler.start();
    }

    /**
     * Constructor used for Mocks only.
     * @param client Mock.
     * @param blockFetcher Mock.
     * @param transactionFetcher Mock.
     * @param rateLimitHandler Mock.
     */
    AccessLayerFacade(Web3jClient client, BlockFetcher blockFetcher,
                      TransactionFetcher transactionFetcher, RateLimitHandler rateLimitHandler) {
        this.client = client;
        this.blockFetcher = blockFetcher;
        this.transactionFetcher = transactionFetcher;
        this.rateLimitHandler = rateLimitHandler;
    }

    /**
     * Probes RPC connection liveness.
     *
     * @return {@code true} if node responds; {@code false} otherwise
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Fetches the {@code count} most recent blocks in descending order,
     * from {@code latest} down to {@code latest - count + 1}.
     *
     * <p>Each RPC call is rate-limited and retried up to {@value #maxRetries} times.
     * Latest block number fetch is also retried independently.
     *
     * @param count number of blocks to fetch; must be &gt; 0
     * @return list of {@link BlockData} in descending block-number order;
     *         {@code null} if interrupted at any point
     * @throws IOException if any fetch fails after all retries exhausted
     */
    public List<BlockData> fetchLatestBlocks(int count) throws IOException {
        List<BlockData> blocks = new ArrayList<>(List.of());
        long latest = -1;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                rateLimitHandler.acquire();
                latest = blockFetcher.fetchLatestBlockNumber();
                break;
            } catch (InterruptedException e) {
                log.info("Block number fetch stopped.");
                return null;
            } catch (IOException e) {
                if (attempt == maxRetries - 1) throw e;
                log.warn("Could not fetch block number {}, retrying ({}/{})", e.getMessage(), attempt + 1, maxRetries);
            }
        }

        long start = latest - count + 1;
        for (long i = latest; i >= start; i--) {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    rateLimitHandler.acquire();
                    blocks.add(blockFetcher.fetchBlock(i));
                    break;
                } catch (InterruptedException e) {
                    log.info("Block fetch stopped.");
                    return null;
                } catch (IOException e) {
                    if (attempt == maxRetries - 1) throw e;
                    log.warn("Could not fetch block {}, retrying ({}/{})", e.getMessage(), attempt + 1, maxRetries);
                }
            }
        }

        return blocks;
    }

    /**
     * Fetches all transactions for the given block,
     * with rate limiting and up to {@value #maxRetries} retries.
     *
     * @param block target block; must not be {@code null}
     * @return list of {@link TransactionData}; {@code null} if interrupted
     * @throws IOException if fetch fails after all retries exhausted
     */
    public List<TransactionData> fetchTransactions(BlockData block) throws IOException {
        List<TransactionData> tx = List.of();
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                rateLimitHandler.acquire();
                tx = transactionFetcher.fetchTransactions(block);
                break;
            } catch (InterruptedException e) {
                log.info("Transaction fetch stopped.");
                return tx;
            } catch (IOException e) {
                if (attempt == maxRetries - 1) throw e;
                log.warn("Could not fetch transactions {}, retrying ({}/{})", e.getMessage(), attempt + 1, maxRetries);
            }
        }
        return tx;
    }

    /**
     * Disconnects RPC client and stops the rate limiter.
     * Facade unusable after call.
     */
    public void disconnect() {
        client.disconnect();
        rateLimitHandler.stop();
    }
}