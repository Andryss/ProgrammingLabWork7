import client.ClientController;
import client.ClientManager;

import java.io.IOException;

public class ClientMain {

    private static final int port = 52927;

    public static void main(String[] args) {
        try {
            ClientManager.run(port);
        } catch (IOException | ClassNotFoundException e) {
            ClientController.printlnErr(e.getMessage());
        }
    }

}
