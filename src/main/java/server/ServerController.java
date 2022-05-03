package server;

import general.element.FieldException;
import general.element.UserProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * ServerController logging into file and terminal, implements simple console commands for server
 */
public class ServerController {

    private static final Logger logger = LogManager.getLogger();

    private ServerController() {}

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message, Throwable error) {
        logger.error("\u001B[31m" + message + "\u001B[0m");
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
                                "clear - clear movie collection\n" +
                                "dropcreate - drop and create tables");
                        break;

                    case "exit":
                        System.exit(0);

                    case "logout":
                        ServerExecutor.logoutUser(args[1]);
                        break;

                    case "ban":
                        ServerExecutor.logoutUser(args[1]);
                        ServerCollectionManager.removeUser(args[1]);
                        break;

                    case "show":
                        ServerExecutor.printUsers();
                        ServerCollectionManager.printTables();
                        break;

                    case "reg":
                        ServerCollectionManager.registerUser(new UserProfile(args[1], args[2]));
                        break;

                    case "remove":
                        try {
                            ServerCollectionManager.removeMovie(Integer.parseInt(args[1]));
                        } catch (NumberFormatException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    case "clear":
                        ServerCollectionManager.removeAllMovies();
                        break;

                    case "dropcreate":
                        try {
                            ServerExecutor.getAuthorizedUsers().clear();
                            ServerCollectionManager.dropTables();
                            ServerCollectionManager.createTables();
                            ServerCollectionManager.loadCollectionsFromDB();
                        } catch (SQLException | FieldException e) {
                            error(e.getMessage(), e);
                        }
                        break;

                    default:
                        info("Undefined console command \"" + args[0] + "\"");
                }
            } catch (IndexOutOfBoundsException e) {
                error("Incorrect amount of arguments: " + e.getMessage(), e);
            } catch (NullPointerException e) {
                error("Found EOF", e);
                break;
            }
        }
    }
}
