package server;

import general.element.FieldException;

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
            ServerHistoryManager.close();
            ServerCollectionManager.close();
            ServerConnector.close();
            ServerExecutor.close();
            ServerController.info("All services closed");
        }, "SeClosingThread"));
    }

    public static void run(int port) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        ServerController.info("Initializations start");
        initializations(port);
        ServerController.info("Initializations completed");
        ServerController.info("Server started at: " + InetAddress.getLocalHost());

        new Thread(() -> {
            try {
                ServerConnector.run();
            } catch (IOException | ClassNotFoundException e) {
                ServerController.error(e.getMessage());
            }
        }, "ReceivingThread").start();
        new Thread(ServerController::run, "SeConsoleThread").start();
    }

    private static void initializations(int port) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        ServerHistoryManager.initialize();
        ServerCollectionManager.initialize();
        ServerConnector.initialize(port);
        ServerExecutor.initialize();
    }

}
