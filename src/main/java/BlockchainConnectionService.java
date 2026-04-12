import org.web3j.protocol.Web3j;

public class BlockchainConnectionService {

    public boolean checkConnection() {

        try {

            Web3j web3j = SepoliaClient.getClient();

            String version =
                    web3j.web3ClientVersion().send().getWeb3ClientVersion();

            System.out.println("Connected to: " + version);

            return true;

        } catch (Exception e) {

            System.out.println("Connection failed");
            e.printStackTrace();

            return false;
        }
    }
}