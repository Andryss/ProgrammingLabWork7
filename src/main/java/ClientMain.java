import client.ClientController;
import client.ClientManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ClientMain {

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            File props = new File("client.properties");
            if (!props.createNewFile()) {
                if (!props.isFile() || !props.canRead()) {
                    throw new IOException("Can't read anything from \"client.properties\" file");
                }
            }
            properties.load(new FileReader(props));
        } catch (FileNotFoundException e) {
            ClientController.getInstance().printlnErr("File \"client.properties\" with properties not found");
            return;
        } catch (IOException e) {
            ClientController.getInstance().printlnErr(e.getMessage());
            return;
        }


        try {
            ClientManager.getInstance().run(properties);
        } catch (Throwable e) {
            ClientController.getInstance().printlnErr(e.getMessage());
        }
    }

}
