package org.example.AccessLayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Web3jClientTest {

    /**
     * isConnected() returns false when URL is unreachable — no real node needed.
     * Validates that the method handles exceptions gracefully rather than throwing.
     */
    @Test
    void isConnected_returnsFalse_whenNodeUnreachable() {
        Web3jClient client = new Web3jClient("http://localhost:1"); // nothing listening

        assertFalse(client.isConnected());
    }

    @Test
    void getWeb3j_returnsNonNull() {
        Web3jClient client = new Web3jClient("http://localhost:8545");

        assertNotNull(client.getWeb3j());

        client.disconnect();
    }

    @Test
    void disconnect_doesNotThrow() {
        Web3jClient client = new Web3jClient("http://localhost:8545");

        assertDoesNotThrow(client::disconnect);
    }

    @Test
    void disconnect_calledTwice_doesNotThrow() {
        Web3jClient client = new Web3jClient("http://localhost:8545");
        client.disconnect();

        assertDoesNotThrow(client::disconnect);
    }
}
