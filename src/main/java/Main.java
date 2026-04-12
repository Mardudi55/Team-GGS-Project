import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Projekt działa!");

        BlockchainConnectionService service =
                new BlockchainConnectionService();

        boolean connected = service.checkConnection();

        if (connected) {
            System.out.println("Blockchain connection OK");
        } else {
            System.out.println("Blockchain connection FAILED");
        }
    }

}