package org.example.AccessLayer;

import org.example.models.BlockData;
import org.example.models.TransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class TransactionFetcherTest {

    @Mock private Web3j web3j;
    @Mock private Web3jClient client;

    private TransactionFetcher transactionFetcher;

    private final BlockData testBlock = new BlockData(100L, "0xblock", 2, 1_700_000_000L);

    @BeforeEach
    void setUp() {
        when(client.getWeb3j()).thenReturn(web3j);
        transactionFetcher = new TransactionFetcher(client);
    }

    private void mockBlockRequest(EthBlock ethBlock) throws IOException {
        Request request = mock(Request.class);
        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenReturn(ethBlock);
    }

    private EthBlock.TransactionResult mockTxResult(String hash, String from, String to,
                                                    BigInteger value, BigInteger gasPrice) {
        Transaction tx = mock(Transaction.class);
        lenient().when(tx.getHash()).thenReturn(hash);
        lenient().when(tx.getFrom()).thenReturn(from);
        lenient().when(tx.getTo()).thenReturn(to);
        lenient().when(tx.getValue()).thenReturn(value);
        lenient().when(tx.getGasPrice()).thenReturn(gasPrice);

        EthBlock.TransactionResult txResult = mock(EthBlock.TransactionResult.class);
        doReturn(tx).when(txResult).get();
        return txResult;
    }

    private void mockReceiptFor(String hash, BigInteger gasUsed) throws IOException {
        Request receiptReq = mock(Request.class);
        EthGetTransactionReceipt receiptResponse = mock(EthGetTransactionReceipt.class);
        TransactionReceipt receipt = mock(TransactionReceipt.class);

        when(web3j.ethGetTransactionReceipt(hash)).thenReturn(receiptReq);
        when(receiptReq.send()).thenReturn(receiptResponse);
        when(receiptResponse.getTransactionReceipt()).thenReturn(Optional.of(receipt));
        when(receipt.getGasUsed()).thenReturn(gasUsed);
    }

    private void mockMissingReceiptFor(String hash) throws IOException {
        Request receiptReq = mock(Request.class);
        EthGetTransactionReceipt receiptResponse = mock(EthGetTransactionReceipt.class);

        when(web3j.ethGetTransactionReceipt(hash)).thenReturn(receiptReq);
        when(receiptReq.send()).thenReturn(receiptResponse);
        when(receiptResponse.getTransactionReceipt()).thenReturn(Optional.empty());
    }

    @Test
    void fetchTransactions_emptyBlock_returnsEmptyList() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of()).when(block).getTransactions();

        List<TransactionData> result = transactionFetcher.fetchTransactions(testBlock);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchTransactions_mapsFieldsCorrectly() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        String hash = "0xtx1";
        BigInteger valueWei = BigInteger.valueOf(1_000_000_000_000_000_000L);
        BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
        BigInteger gasUsed = BigInteger.valueOf(21_000L);

        EthBlock.TransactionResult txResult = mockTxResult(hash, "0xsender", "0xreceiver", valueWei, gasPrice);
        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of(txResult)).when(block).getTransactions();
        mockReceiptFor(hash, gasUsed);

        List<TransactionData> result = transactionFetcher.fetchTransactions(testBlock);

        assertEquals(1, result.size());
        TransactionData tx = result.getFirst();
        assertEquals(hash, tx.getTxHash());
        assertEquals("0xsender", tx.getSender());
        assertEquals("0xreceiver", tx.getReceiver());
        assertEquals(0, new BigDecimal("1.0").compareTo(tx.getValueEth()));
        assertEquals(21_000L, tx.getGasUsed());
        assertEquals(gasPrice.longValue(), tx.getGasPrice());
    }

    @Test
    void fetchTransactions_missingReceipt_defaultsGasUsedToZero() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        String hash = "0xtx_no_receipt";
        EthBlock.TransactionResult txResult = mockTxResult(
                hash, "0xfrom", "0xto", BigInteger.ZERO, BigInteger.ONE);

        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of(txResult)).when(block).getTransactions();
        mockMissingReceiptFor(hash);

        List<TransactionData> result = transactionFetcher.fetchTransactions(testBlock);

        assertEquals(1, result.size());
        assertEquals(0L, result.getFirst().getGasUsed());
    }

    @Test
    void fetchTransactions_zeroValueTx_mappedCorrectly() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        String hash = "0xtx_zero";
        EthBlock.TransactionResult txResult = mockTxResult(
                hash, "0xfrom", "0xcontract", BigInteger.ZERO, BigInteger.TEN);

        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of(txResult)).when(block).getTransactions();
        mockReceiptFor(hash, BigInteger.valueOf(50_000L));

        List<TransactionData> result = transactionFetcher.fetchTransactions(testBlock);

        assertEquals(1, result.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFirst().getValueEth()));
    }

    @Test
    void fetchTransactions_multipleTransactions_allMapped() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        EthBlock.TransactionResult tx1 = mockTxResult("0xhash1", "0xA", "0xB",
                BigInteger.valueOf(500_000_000_000_000_000L), BigInteger.ONE);
        EthBlock.TransactionResult tx2 = mockTxResult("0xhash2", "0xC", "0xD",
                BigInteger.valueOf(250_000_000_000_000_000L), BigInteger.TWO);

        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of(tx1, tx2)).when(block).getTransactions();
        mockReceiptFor("0xhash1", BigInteger.valueOf(21_000L));
        mockReceiptFor("0xhash2", BigInteger.valueOf(42_000L));

        List<TransactionData> result = transactionFetcher.fetchTransactions(testBlock);

        assertEquals(2, result.size());
        assertEquals("0xhash1", result.get(0).getTxHash());
        assertEquals("0xhash2", result.get(1).getTxHash());
    }

    @Test
    void fetchTransactions_throwsIOException_onBlockFetchFailure() throws IOException {
        Request request = mock(Request.class);
        when(web3j.ethGetBlockByNumber(any(), anyBoolean())).thenReturn(request);
        when(request.send()).thenThrow(new IOException("RPC down"));

        assertThrows(IOException.class, () -> transactionFetcher.fetchTransactions(testBlock));
    }

    @Test
    void fetchTransactions_throwsIOException_onReceiptFetchFailure() throws IOException {
        EthBlock ethBlock = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);

        String hash = "0xfailing";
        EthBlock.TransactionResult txResult = mockTxResult(
                hash, "0xA", "0xB", BigInteger.ONE, BigInteger.ONE);

        mockBlockRequest(ethBlock);
        when(ethBlock.getBlock()).thenReturn(block);
        doReturn(List.of(txResult)).when(block).getTransactions();

        Request receiptReq = mock(Request.class);
        when(web3j.ethGetTransactionReceipt(hash)).thenReturn(receiptReq);
        when(receiptReq.send()).thenThrow(new IOException("receipt fetch failed"));

        assertThrows(IOException.class, () -> transactionFetcher.fetchTransactions(testBlock));
    }
}