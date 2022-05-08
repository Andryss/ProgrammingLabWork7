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
    private static final ServerController instance = new ServerController();
    private final Logger logger = LogManager.getLogger();

    private ServerController() {}

    public static ServerController getInstance() {
        return instance;
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message) {
        logger.error("\u001B[31m" + message + "\u001B[0m");
    }

    public void run() {
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
                                "usl[ogout] <name> - logout user\n" +
                                "usb[an] <name> - ban and logout user\n" +
                                "tbs[how] - print authorized users and collections\n" +
                                "usr[eg] <name> <pass> - register new user\n" +
                                "elr[emove] <key> - remove movie\n" +
                                "elc[lear] - clear movie collection\n" +
                                "usclh[istory] <name> - clear user history\n" +
                                "tbdropcreate - drop and create tables");
                        break;

                    case "exit":
                        System.exit(0);

                    case "usl":
                    case "uslogout":
                        ServerExecutor.logoutUser(args[1]);
                        break;

                    case "usb":
                    case "usban":
                        ServerExecutor.logoutUser(args[1]);
                        ServerCollectionManager.getInstance().removeUser(args[1]);
                        break;

                    case "tbs":
                    case "tbshow":
                        ServerExecutor.printUsers();
                        ServerCollectionManager.getInstance().printTables();
                        break;

                    case "usr":
                    case "usreg":
                        ServerCollectionManager.getInstance().registerUser(new UserProfile(args[1], args[2]));
                        break;

                    case "elr":
                    case "elremove":
                        try {
                            ServerCollectionManager.getInstance().removeMovie(Integer.parseInt(args[1]));
                        } catch (NumberFormatException e) {
                            error(e.getMessage());
                        }
                        break;

                    case "elc":
                    case "elclear":
                        ServerCollectionManager.getInstance().removeAllMovies();
                        break;

                    case "usclh":
                    case "usclhistory":
                        ServerHistoryManager.getInstance().clearUserHistory(args[1]);
                        break;

                    case "tbdropcreate":
                        try {
                            ServerExecutor.getAuthorizedUsers().clear();
                            ServerCollectionManager.getInstance().dropTables();
                            ServerCollectionManager.getInstance().createTables();
                            ServerCollectionManager.getInstance().loadCollectionsFromDB();
                        } catch (SQLException | FieldException e) {
                            error(e.getMessage());
                        }
                        break;

                    default:
                        info("Undefined console command \"" + args[0] + "\"");
                }
            } catch (IndexOutOfBoundsException e) {
                error("Incorrect amount of arguments: " + e.getMessage());
            } catch (NullPointerException e) {
                error("Found EOF");
                break;
            }
        }
    }
}
