package Server;

import MovieObjects.FieldException;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

/**
 * ServerManager initialize server and start Thread which receiving requests and Thread which read console commands
 */
public class ServerManager {

    private ServerManager() {}

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ServerCollectionManager.close();
                ServerConnector.close();
                ServerController.info("All services closed");
            } catch (Throwable e) {
                //ignore
            }
        }, "CloseThread"));
    }

    public static void run(int port) throws IOException, FieldException, SQLException, ClassNotFoundException {
        ServerController.info("Initializations start");
        initializations(port);
        ServerController.info("Initializations completed");
        ServerController.info("Server started at: " + InetAddress.getLocalHost());

        new Thread(() -> {
            try {
                ServerConnector.run();
            } catch (IOException | ClassNotFoundException e) {
                ServerController.error(e.getMessage(), e);
            }
        }, "ReceivingThread").start();
        new Thread(ServerController::run, "ConsoleThread").start();
    }

    private static void initializations(int port) throws IOException, FieldException, SQLException, ClassNotFoundException {
        ServerCollectionManager.initialize();
        ServerConnector.initialize(port);
        ServerExecutor.initialize();
    }

}
