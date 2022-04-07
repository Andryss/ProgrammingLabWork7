import MovieObjects.FieldException;
import Server.ServerController;
import Server.ServerManager;

import java.io.IOException;
import java.sql.SQLException;

public class ServerMain {

    private static final int port = 52927;

    public static void main(String[] args) {
        try {
            ServerManager.run(port);
        } catch (FieldException e) {
            ServerController.error("Problems with Movie File: " + e.getMessage(), e);
        } catch (Throwable e) {
            ServerController.error(e.getMessage(), e);
        }
    }
}
