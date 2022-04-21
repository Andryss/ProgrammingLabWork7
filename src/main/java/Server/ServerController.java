package Server;

import MovieObjects.UserProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * <p>FileController implements nothing in FileManager</p>
 * <p>The main idea of this class is just logging into file and terminal</p>
 */
public class ServerController {

    private static final Logger logger = LogManager.getLogger();

    private ServerController() {}

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message, Throwable error) {
        logger.error("\u001B[31m" + message + "\u001B[0m",error);
    }

    public static void run() {
        info("Ready for commands (type \"?\" or \"help\" for list of commands)");

        while (true) {
            try {
                String[] args = System.console().readLine().split("\\s");
                info(Arrays.toString(args) + " command");

                switch (args[0]) {
                    case "?":
                    case "help":
                        info("Available commands:\n" +
                                "exit - shut down server\n" +
                                "logout <name> - logout user\n" +
                                "ban <name> - ban and logout user\n" +
                                "show - print authorized users and collections\n" +
                                "reg <name> <pass> - register new user\n" +
                                "remove <key> - remove movie\n" +
                                "dropcreate - drop and create tables");
                        break;

                    case "exit":
                        System.exit(0);

                    case "logout":
                        ServerExecutor.logoutUser(args[1]);
                        break;

                    case "ban":
                        ServerExecutor.logoutUser(args[1]);
                        try {
                            ServerCollectionManager.removeUser(args[1]);
                        } catch (SQLException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    case "show":
                        ServerExecutor.printUsers();
                        ServerCollectionManager.printTables();
                        break;

                    case "reg":
                        try {
                            ServerCollectionManager.registerUser(new UserProfile(args[1], args[2]));
                        } catch (SQLException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    case "remove":
                        try {
                            ServerCollectionManager.removeMovie(Integer.parseInt(args[1]));
                        } catch (SQLException | NumberFormatException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    case "dropcreate":
                        try {
                            ServerCollectionManager.dropTables();
                            ServerCollectionManager.createTables();
                        } catch (SQLException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    default:
                        info("Undefined console command \"" + args[0] + "\"");
                }
            } catch (NoSuchElementException | IndexOutOfBoundsException e) {
                error(e.getMessage(), e);
            } catch (NullPointerException e) {
                error("Found EOF", e);
                break;
            }
        }
    }
}
