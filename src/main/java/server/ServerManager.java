package server;

import general.element.FieldException;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ServerManager initialize server and start Thread which receiving requests and Thread which reading console commands
 */
public class ServerManager {

    private ServerManager() {}

    public static void run(Properties properties) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        ServerController.info("Initializations start");
        initializations(properties);
        ServerController.info("Initializations completed");
        ServerController.info("Server started at: " + InetAddress.getLocalHost());

        new Thread(() -> {
            try {
                ServerConnector.run();
            } catch (IOException e) {
                ServerController.error(e.getMessage());
            }
        }, "ReceivingThread").start();
        new Thread(ServerController::run, "SeConsoleThread").start();
    }

    private static void initializations(Properties properties) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        setAllModulesProperties(properties);
        initializeAllModules();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllModules();
            ServerController.info("All services closed");
        }, "SeClosingThread"));
    }

    private static void setAllModulesProperties(Properties properties) {
        ServerHistoryManager.setProperties(properties);
        ServerCollectionManager.setProperties(properties);
        ServerConnector.setProperties(properties);
    }

    private static void initializeAllModules() throws IOException, IllegalAccessException, FieldException, SQLException, ClassNotFoundException {
        ServerHistoryManager.initialize();
        ServerCollectionManager.initialize();
        ServerConnector.initialize();
        ServerExecutor.initialize();
    }

    private static void closeAllModules() {
        ServerHistoryManager.close();
        ServerCollectionManager.close();
        ServerConnector.close();
        ServerExecutor.close();
    }
}
