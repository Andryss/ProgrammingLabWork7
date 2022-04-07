package Server;

import MovieObjects.FieldException;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>ServerManager consist of main server logic:</p>
 * <p>1) Receive Request from client</p>
 * <p>2) Execute commands and build Response</p>
 * <p>3) Send Response to client</p>
 */
public class ServerManager {

    private ServerManager() {}

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ServerCollectionManager::dropTables, "DropTablesThread"));
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
    }

    private static void initializations(int port) throws IOException, FieldException, SQLException, ClassNotFoundException {
        ServerCollectionManager.initialize();
        ServerConnector.initialize(port);
        ServerExecutor.initialize();
    }

}
