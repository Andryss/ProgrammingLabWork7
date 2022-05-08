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
    private static final ServerManager instance = new ServerManager(); // Follow "Singleton" pattern

    private ServerManager() {}

    public static ServerManager getInstance() {
        return instance;
    }

    public void run(Properties properties) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        ServerController.getInstance().info("Initializations start");
        initializations(properties);
        ServerController.getInstance().info("Initializations completed");
        ServerController.getInstance().info("Server started at: " + InetAddress.getLocalHost());

        new Thread(() -> {
            try {
                ServerConnector.getInstance().run();
            } catch (IOException e) {
                ServerController.getInstance().error(e.getMessage());
            }
        }, "ReceivingThread").start();
        new Thread(ServerController.getInstance()::run, "SeConsoleThread").start();
    }

    private void initializations(Properties properties) throws IOException, FieldException, SQLException, ClassNotFoundException, IllegalAccessException {
        setAllModulesProperties(properties);
        initializeAllModules();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeAllModules();
            ServerController.getInstance().info("All services closed");
        }, "SeClosingThread"));
    }

    private void setAllModulesProperties(Properties properties) {
        ServerHistoryManager.getInstance().setProperties(properties);
        ServerCollectionManager.getInstance().setProperties(properties);
        ServerConnector.getInstance().setProperties(properties);
    }

    private void initializeAllModules() throws IOException, IllegalAccessException, FieldException, SQLException, ClassNotFoundException {
        ServerHistoryManager.getInstance().initialize();
        ServerCollectionManager.getInstance().initialize();
        ServerConnector.getInstance().initialize();
        ServerExecutor.initialize();
    }

    private void closeAllModules() {
        ServerHistoryManager.getInstance().close();
        ServerCollectionManager.getInstance().close();
        ServerConnector.getInstance().close();
        ServerExecutor.close();
    }
}
