package Client;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * <p>ClientController implements (1) and (4) steps in ClientManager</p>
 * <p>The main idea of this class is "to speak" with the user</p>
 */
public class ClientController {

    private static final Scanner reader = new Scanner(System.in);
    private static final PrintStream writer = System.out;

    private ClientController() {}

    static void initialize() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("\u001B[36m" + "THANK YOU for choosing our app to work with collections.\n" +
                "Developers are searching for the best realizations. Have a nice day :)" + "\u001B[0m")));
        println("\u001B[36m" + "Hi! This is a simple client-server program for working with collection.");
        println("I'm waiting for your commands (type \"help\" for list of available commands)." + "\u001B[0m");
    }

    public static String readLine() {
        return reader.nextLine();
    }

    public static void println(String line) {
        writer.println(line);
    }
    public static void print(String line) {
        writer.print(line);
    }
    public static void printlnGood(String line) {
        writer.println("\u001B[32m" + line + "\u001B[0m");
    }
    public static void printlnErr(String line) {
        writer.println("\u001B[31m" + line + "\u001B[0m");
    }

    static InetAddress readServerAddress() {
        print("Enter server domain name or IP (or \"exit\"): ");
        while (true) {
            String line = readLine().trim();
            if ("exit".equals(line)) {
                System.exit(0);
            }
            try {
                return InetAddress.getByName(line);
            } catch (UnknownHostException e) {
                printlnErr("Unknown host \"" + line + "\"");
                print("Enter VALID server domain name or IP: ");
            }
        }
    }

    private static final int requiredLoginLength = 3;
    static String readLogin() {
        while (true) {
            ClientController.print("Enter user login: ");
            String userLogin = ClientController.readLine().trim();
            if (userLogin.length() < requiredLoginLength) {
                ClientController.printlnErr("Login must have at least " +  requiredLoginLength + " characters");
            } else {
                return userLogin;
            }
        }
    }

    private static final int requiredPasswordLength = 3;
    static String readPassword() {
        while (true) {
            ClientController.print("Enter user password: ");
            String userPassword = ClientController.readLine().trim();
            if (userPassword.length() < requiredPasswordLength) {
                ClientController.printlnErr("Password must have at least " +  requiredPasswordLength + " characters");
            } else {
                return userPassword;
            }
        }
    }

    public static Scanner getReader() {
        return reader;
    }
}
