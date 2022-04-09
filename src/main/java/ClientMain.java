import Client.ClientController;
import Client.ClientManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

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
