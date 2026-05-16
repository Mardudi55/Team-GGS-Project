package org.example.AccessLayer;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Thin wrapper around {@link Web3j} providing lifecycle management
 * for an Ethereum JSON-RPC connection over HTTP.
 */
class Web3jClient {

    private final Web3j web3j;

    /**
     * Creates client and establishes HTTP transport to given RPC endpoint.
     *
     * @param rpcUrl full URL of Ethereum JSON-RPC node (e.g. {@code http://localhost:8545})
     */
    public Web3jClient(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    /**
     * Probes liveness by calling {@code web3_clientVersion}.
     *
     * @return {@code true} if node responds; {@code false} on any exception
     */
    public boolean isConnected() {
        try {
            web3j.web3ClientVersion().send();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Exposes underlying {@link Web3j} instance for direct RPC calls.
     *
     * @return configured {@link Web3j} instance; never {@code null}
     */
    public Web3j getWeb3j() {
        return web3j;
    }

    /**
     * Shuts down the {@link Web3j} instance and releases HTTP resources.
     * Client unusable after call.
     */
    public void disconnect() {
        web3j.shutdown();
    }
}