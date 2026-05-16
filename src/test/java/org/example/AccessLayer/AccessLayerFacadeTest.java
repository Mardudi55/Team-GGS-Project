package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessLayerFacadeTest {

    @Mock private BlockFetcher blockFetcher;
    @Mock private TransactionFetcher transactionFetcher;
    @Mock private RateLimitHandler rateLimitHandler;
    @Mock private Web3jClient client;

    private TestableAccessLayerFacade facade;

    static class TestableAccessLayerFacade extends AccessLayerFacade {
        TestableAccessLayerFacade(Web3jClient client, BlockFetcher blockFetcher,
                                  TransactionFetcher transactionFetcher,
                                  RateLimitHandler rateLimitHandler) {
            super(client, blockFetcher, transactionFetcher, rateLimitHandler);
        }
    }

    @BeforeEach
    void setUp() {
        // no global acquire() stub — each test stubs individually to avoid UnnecessaryStubbingException
        facade = new TestableAccessLayerFacade(client, blockFetcher, transactionFetcher, rateLimitHandler);
    }

    @Test
    void fetchLatestBlocks_returnsCorrectCount() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        when(blockFetcher.fetchLatestBlockNumber()).thenReturn(200L);
        when(blockFetcher.fetchBlock(anyLong())).thenAnswer(inv -> {
            long num = inv.getArgument(0);
            return new BlockData(num, "0x" + num, 0, 0L);
        });

        List<BlockData> result = facade.fetchLatestBlocks(3);

        assertEquals(3, result.size());
        assertEquals(200L, result.get(0).getBlockNumber());
        assertEquals(199L, result.get(1).getBlockNumber());
        assertEquals(198L, result.get(2).getBlockNumber());
    }

    @Test
    void fetchLatestBlocks_count1_returnsOnlyLatestBlock() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        when(blockFetcher.fetchLatestBlockNumber()).thenReturn(500L);
        when(blockFetcher.fetchBlock(500L)).thenReturn(new BlockData(500L, "0x500", 10, 0L));

        List<BlockData> result = facade.fetchLatestBlocks(1);

        assertEquals(1, result.size());
        assertEquals(500L, result.getFirst().getBlockNumber());
    }

    @Test
    void fetchLatestBlocks_blockNumberFailsBothRetries_throwsIOException() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        when(blockFetcher.fetchLatestBlockNumber())
                .thenThrow(new IOException("timeout"))
                .thenThrow(new IOException("timeout again"));

        assertThrows(IOException.class, () -> facade.fetchLatestBlocks(1));
    }

    @Test
    void fetchLatestBlocks_blockFetchFailsAllRetries_throwsIOException() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        when(blockFetcher.fetchLatestBlockNumber()).thenReturn(10L);
        when(blockFetcher.fetchBlock(anyLong()))
                .thenThrow(new IOException("block fetch failed"));

        assertThrows(IOException.class, () -> facade.fetchLatestBlocks(1));
    }

    @Test
    void fetchLatestBlocks_returnsNull_whenInterruptedOnAcquire() throws IOException, InterruptedException {
        doThrow(new InterruptedException()).when(rateLimitHandler).acquire();

        List<BlockData> result = facade.fetchLatestBlocks(1);

        assertNull(result);
    }

    @Test
    void fetchTransactions_returnsTransactionList() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        BlockData block = new BlockData(1L, "0x1", 2, 0L);
        List<TransactionData> expected = List.of(
                new TransactionData("0xtx1", "0xA", "0xB", BigDecimal.ONE, 21000L, 1L),
                new TransactionData("0xtx2", "0xC", "0xD", BigDecimal.TEN, 42000L, 2L)
        );
        when(transactionFetcher.fetchTransactions(block)).thenReturn(expected);

        List<TransactionData> result = facade.fetchTransactions(block);

        assertEquals(2, result.size());
        assertEquals("0xtx1", result.getFirst().getTxHash());
    }

    @Test
    void fetchTransactions_throwsIOException_afterRetries() throws IOException, InterruptedException {
        doNothing().when(rateLimitHandler).acquire();
        BlockData block = new BlockData(1L, "0x1", 0, 0L);
        when(transactionFetcher.fetchTransactions(block))
                .thenThrow(new IOException("failed"));

        assertThrows(IOException.class, () -> facade.fetchTransactions(block));
    }

    @Test
    void fetchTransactions_returnsNull_whenInterruptedOnAcquire() throws IOException, InterruptedException {
        doThrow(new InterruptedException("test interrupt")).when(rateLimitHandler).acquire();
        BlockData block = new BlockData(1L, "0x1", 0, 0L);

        List<TransactionData> result = facade.fetchTransactions(block);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rateLimitHandler).acquire();
    }

    @Test
    void disconnect_callsClientAndStopsRateLimiter() {
        facade.disconnect();

        verify(client).disconnect();
        verify(rateLimitHandler).stop();
    }

    @Test
    void isConnected_delegatesToClient() {
        when(client.isConnected()).thenReturn(true);

        assertTrue(facade.isConnected());
        verify(client).isConnected();
    }
}