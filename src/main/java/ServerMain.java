import client.ClientController;
import general.element.FieldException;
import server.ServerController;
import server.ServerManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServerMain {

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            File props = new File("server.properties");
            if (!props.createNewFile()) {
                if (!props.isFile() || !props.canRead()) {
                    throw new IOException("Can't read anything from \"server.properties\" file");
                }
            }
            properties.load(new FileReader(props));
        } catch (FileNotFoundException e) {
            ClientController.printlnErr("File \"server.properties\" with properties not found");
            return;
        } catch (IOException e) {
            ClientController.printlnErr(e.getMessage());
            return;
        }


        try {
            ServerManager.run(properties);
        } catch (FieldException e) {
            ServerController.error("Problems with Movie File: " + e.getMessage());
            System.exit(0);
        } catch (Throwable e) {
            ServerController.error(e.getMessage());
            System.exit(0);
        }
    }
}
