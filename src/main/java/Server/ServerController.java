package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        logger.error(message,error);
    }

}
