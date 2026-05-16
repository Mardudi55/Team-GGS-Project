package org.example.AccessLayer;

import org.example.models.BlockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class BlockFetcherTest {

    @Mock private Web3j web3j;
    @Mock private Web3jClient client;

    private BlockFetcher blockFetcher;

    @BeforeEach
    void setUp() {
        when(client.getWeb3j()).thenReturn(web3j);
        blockFetcher = new BlockFetcher(client);
    }

    @Test
    void fetchLatestBlockNumber_returnsCorrectNumber() throws IOException {
        Request request = mock(Request.class);
        EthBlockNumber response = mock(EthBlockNumber.class);

        when(web3j.ethBlockNumber()).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getBlockNumber()).thenReturn(BigInteger.valueOf(7_000_000L));

        long result = blockFetcher.fetchLatestBlockNumber();

        assertEquals(7_000_000L, result);
    }

    @Test
    void fetchLatestBlockNumber_throwsIOException_onRpcFailure() throws IOException {
        Request request = mock(Request.class);

        when(web3j.ethBlockNumber()).thenReturn(request);
        when(request.send()).thenThrow(new IOException("RPC failure"));

        assertThrows(IOException.class, () -> blockFetcher.fetchLatestBlockNumber());
    }

    @Test
    void fetchBlock_returnsCorrectBlockData() throws IOException {
        long blockNumber = 42L;
        String blockHash = "0xabc123";
        int txCount = 5;
        long timestamp = 1_700_000_000L;

        Request request = mock(Request.class);
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenReturn(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        when(block.getNumber()).thenReturn(BigInteger.valueOf(blockNumber));
        when(block.getHash()).thenReturn(blockHash);
        when(block.getTimestamp()).thenReturn(BigInteger.valueOf(timestamp));

        List<EthBlock.TransactionResult> txResults = List.of(
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class),
                mock(EthBlock.TransactionResult.class)
        );
        doReturn(txResults).when(block).getTransactions();

        BlockData result = blockFetcher.fetchBlock(blockNumber);

        assertNotNull(result);
        assertEquals(blockNumber, result.getBlockNumber());
        assertEquals(blockHash, result.getBlockHash());
        assertEquals(txCount, result.getTransactionCount());
        assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    void fetchBlock_emptyBlock_hasZeroTransactions() throws IOException {
        Request request = mock(Request.class);
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenReturn(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        when(block.getNumber()).thenReturn(BigInteger.valueOf(1L));
        when(block.getHash()).thenReturn("0x000");
        when(block.getTimestamp()).thenReturn(BigInteger.valueOf(1000L));
        doReturn(List.of()).when(block).getTransactions();

        BlockData result = blockFetcher.fetchBlock(1L);

        assertEquals(0, result.getTransactionCount());
    }

    @Test
    void fetchBlock_throwsIOException_onRpcFailure() throws IOException {
        Request request = mock(Request.class);

        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenThrow(new IOException("connection refused"));

        assertThrows(IOException.class, () -> blockFetcher.fetchBlock(99L));
    }

    @Test
    void fetchBlock_throwsNullPointerException_whenBlockIsNull() throws IOException {
        Request request = mock(Request.class);
        EthBlock ethBlock = mock(EthBlock.class);

        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenReturn(ethBlock);
        when(ethBlock.getBlock()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> blockFetcher.fetchBlock(Long.MAX_VALUE));
    }
}