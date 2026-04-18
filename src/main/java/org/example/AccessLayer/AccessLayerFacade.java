package org.example.AccessLayer;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AccessLayerFacade {

    private final Web3j web3j;

    //KONSTRUKTOR
    public AccessLayerFacade() {
        String rpcUrl = "https://eth-sepolia.g.alchemy.com/v2/meML7wFPg5fL9WPRSVUqa";
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    //SPRAWDZENIE POŁĄCZENIA
    public boolean isConnected() {
        try {
            String version = web3j.web3ClientVersion()
                    .send()
                    .getWeb3ClientVersion();

            System.out.println("Connected to: " + version);
            return true;

        } catch (Exception e) {
            System.out.println("Connection failed");
            e.printStackTrace();
            return false;
        }
    }

    //NAJNOWSZY BLOK
    public BigInteger getLatestBlockNumber() {
        try {
            return web3j.ethBlockNumber()
                    .send()
                    .getBlockNumber();
        } catch (Exception e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
    }


    // MODELE DANYCH


    public static class BlockHeader {
        public String hash;
        public BigInteger number;
        public BigInteger timestamp;
        public String parentHash;

        public BlockHeader(String hash, BigInteger number,
                           BigInteger timestamp, String parentHash) {
            this.hash = hash;
            this.number = number;
            this.timestamp = timestamp;
            this.parentHash = parentHash;
        }

        @Override
        public String toString() {
            return "BlockHeader{" +
                    "hash='" + hash + '\'' +
                    ", number=" + number +
                    ", timestamp=" + timestamp +
                    ", parentHash='" + parentHash + '\'' +
                    '}';
        }
    }

    public static class TransactionData {
        public String hash;
        public String from;
        public String to;
        public BigInteger value;

        public TransactionData(String hash, String from,
                               String to, BigInteger value) {
            this.hash = hash;
            this.from = from;
            this.to = to;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TransactionData{" +
                    "hash='" + hash + '\'' +
                    ", from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    //EKSTRAKCJA NAGŁÓWKA BLOKU

    public BlockHeader getBlockHeader(BigInteger blockNumber) {
        try {
            EthBlock response = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    false
            ).send();

            EthBlock.Block block = response.getBlock();

            return new BlockHeader(
                    block.getHash(),
                    block.getNumber(),
                    block.getTimestamp(),
                    block.getParentHash()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //EKSTRAKCJA TRANSAKCJI


    public List<TransactionData> getTransactionsFromBlock(BigInteger blockNumber) {

        List<TransactionData> list = new ArrayList<>();

        try {
            EthBlock response = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    true
            ).send();

            EthBlock.Block block = response.getBlock();

            for (EthBlock.TransactionResult<?> tx : block.getTransactions()) {

                org.web3j.protocol.core.methods.response.Transaction transaction =
                        (org.web3j.protocol.core.methods.response.Transaction) tx.get();

                list.add(new TransactionData(
                        transaction.getHash(),
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getValue()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}