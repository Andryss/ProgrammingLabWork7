package Client;

import Commands.CommandException;
import Server.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

/**
 * <p>ClientManager consist of main client logic:</p>
 * <p>1) Read command from CMD</p>
 * <p>2) Validate command and build Request</p>
 * <p>3) Send Request to the server and get Response</p>
 * <p>4) Print Server response</p>
 */
public class ClientManager {

    public static void run(int port) throws IOException, ClassNotFoundException {
        try {
            connectionStep(port);
            loginRegisterStep();
            executionStep();
        } catch (NoSuchElementException e) {
            ClientController.printlnErr("Incorrect input (EOF). Try not to be so unpredictable!");
        }
    }

    private static void connectionStep(int port) throws IOException, ClassNotFoundException {
        while (true) {
            try {
                InetAddress serverAddress = readServerAddress();
                ClientController.println("Connecting to server \"" + serverAddress + "\"");
                ClientConnector.initialize(serverAddress, port);
                ClientController.println("Connection to server was successful");
                break;
            } catch (SocketTimeoutException e) {
                ClientController.printlnErr(e.getMessage());
            }
        }
    }

    private static InetAddress readServerAddress() {
        ClientController.print("Enter server domain name or IP (or \"exit\"): ");
        while (true) {
            String line = ClientController.readLine().trim();
            if ("exit".equals(line)) {
                System.exit(0);
            }
            try {
                return InetAddress.getByName(line);
            } catch (UnknownHostException e) {
                ClientController.printlnErr("Unknown host \"" + line + "\"");
                ClientController.print("Enter VALID server domain name or IP: ");
            }
        }
    }

    private static void loginRegisterStep() {
        loginRegisterStep:
        while (true) {
            ClientController.print("Do you want (\"l[ogin]\" or \"r[egister]\"): ");
            String line = ClientController.readLine().trim();
            switch (line) {
                case "exit":
                    System.exit(0);
                case "login":
                case "l":
                    if (loginStep()) {
                        ClientController.println("User successfully logged in");
                        break loginRegisterStep;
                    }
                    break;
                case "register":
                case "r":
                    if (registerStep()) {
                        ClientController.println("New user successfully registered");
                        break loginRegisterStep;
                    }
                    break;
                default:
                    ClientController.printlnErr("Unknown command \"" + line + "\"");
                    break;
            }
        }
    }

    private static boolean loginStep() {
        ClientController.println("Enter user login: ");
        String userLogin = ClientController.readLine().trim();
        ClientController.println("Enter user password: ");
        String userPassword = ClientController.readLine().trim();
        RequestBuilder.createNewRequest(Request.RequestType.LOGIN_USER, userLogin, userPassword);
        try {
            Response response = ClientConnector.sendToServer(RequestBuilder.getRequest());
            if (response.getResponseType() == Response.ResponseType.LOGIN_FAILED) {
                ClientController.printlnErr(response.getMessage());
            } else if (response.getResponseType() == Response.ResponseType.LOGIN_SUCCESSFUL) {
                ClientExecutor.initialize(userLogin, userPassword);
                return true;
            } else {
                throw new IOException("Server has wrong logic: expected \"" +
                        Response.ResponseType.LOGIN_FAILED +
                        "\" or \"" +
                        Response.ResponseType.LOGIN_SUCCESSFUL +
                        "\", but not \"" +
                        response.getResponseType() +
                        "\"");
            }
        } catch (IOException | ClassNotFoundException e) {
            ClientController.printlnErr(e.getMessage());
        }
        return false;
    }

    private static boolean registerStep() {
        ClientController.println("Enter user login: ");
        String userLogin = ClientController.readLine().trim();
        ClientController.println("Enter user password: ");
        String userPassword = ClientController.readLine().trim();
        RequestBuilder.createNewRequest(Request.RequestType.REGISTER_USER, userLogin, userPassword);
        try {
            Response response = ClientConnector.sendToServer(RequestBuilder.getRequest());
            if (response.getResponseType() == Response.ResponseType.REGISTER_FAILED) {
                ClientController.printlnErr(response.getMessage());
            } else if (response.getResponseType() == Response.ResponseType.REGISTER_SUCCESSFUL) {
                ClientExecutor.initialize(userLogin, userPassword);
                return true;
            } else {
                throw new IOException("Server has wrong logic: expected \"" +
                        Response.ResponseType.LOGIN_FAILED +
                        "\" or \"" +
                        Response.ResponseType.LOGIN_SUCCESSFUL +
                        "\", but not \"" +
                        response.getResponseType() +
                        "\"");
            }
        } catch (IOException | ClassNotFoundException e) {
            ClientController.printlnErr(e.getMessage());
        }
        return false;
    }

    private static void executionStep() {
        ClientController.initialize();

        while (true) {
            try {
                ClientExecutor.parseCommand(ClientController.readLine());
                ClientController.println(ClientConnector.sendToServer(RequestBuilder.getRequest()).getMessage());
            } catch (SocketTimeoutException e) {
                ClientController.printlnErr("Server isn't responding (try again later or choose another server)");
            } catch (IOException | ClassNotFoundException | CommandException e) {
                ClientController.printlnErr(e.getMessage());
            }
        }
    }
}
