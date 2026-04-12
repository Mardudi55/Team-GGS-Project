import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class SepoliaClient {

    private static final String RPC_URL =
            "https://eth-sepolia.g.alchemy.com/v2/meML7wFPg5fL9WPRSVUqa";

    private static final Web3j web3j =
            Web3j.build(new HttpService(RPC_URL));

    public static Web3j getClient() {
        return web3j;
    }
}