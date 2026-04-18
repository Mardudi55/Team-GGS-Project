package org.example.AccessLayer;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;

import java.math.BigInteger;

public class AccessLayerFacade {

    private final Web3j web3j;

    // Konstruktor - inicjalizacja klienta
    public AccessLayerFacade() {

        String rpcUrl = "https://eth-sepolia.g.alchemy.com/v2/meML7wFPg5fL9WPRSVUqa";

        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    // Sprawdzenie połączenia
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

    // Pobranie numeru najnowszego bloku
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

    // Pobranie najnowszego bloku (bez transakcji)
    public EthBlock getLatestBlock() {

        try {
            return web3j.ethGetBlockByNumber(
                    DefaultBlockParameterName.LATEST,
                    false
            ).send();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Pobranie salda adresu
    public BigInteger getBalance(String address) {

        try {
            EthGetBalance balance = web3j.ethGetBalance(
                    address,
                    DefaultBlockParameterName.LATEST
            ).send();

            return balance.getBalance();

        } catch (Exception e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
    }

    // Pobranie szczegółów transakcji
    public EthTransaction getTransaction(String txHash) {

        try {
            return web3j.ethGetTransactionByHash(txHash)
                    .send();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
