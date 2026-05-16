package org.example.AccessLayer;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

class Web3jClient {

    private final Web3j web3j;

    public Web3jClient(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public boolean isConnected() {
        try {
            web3j.web3ClientVersion().send();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void disconnect() {
        web3j.shutdown();
    }
}