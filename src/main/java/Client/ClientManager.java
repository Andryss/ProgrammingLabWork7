package Client;

import Commands.CommandException;
import MovieObjects.UserProfile;
import Server.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * <p>ClientManager consist of main client logic:</p>
 * <p>1) Connection step</p>
 * <p>2) Login or register step</p>
 * <p>3) Execution step</p>
 *
 * <p>Execution step consist of:</p>
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
                InetAddress serverAddress = ClientController.readServerAddress();
                ClientController.println("Connecting to server \"" + serverAddress + "\"");
                ClientConnector.initialize(serverAddress, port);
                ClientController.printlnGood("Connection to server was successful");
                break;
            } catch (SocketTimeoutException e) {
                ClientController.printlnErr(e.getMessage());
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
                        ClientController.printlnGood("User successfully logged in");
                        break loginRegisterStep;
                    }
                    break;
                case "register":
                case "r":
                    if (registerStep()) {
                        ClientController.printlnGood("New user successfully registered");
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
        return lrstep(Request.RequestType.LOGIN_USER, Response.ResponseType.LOGIN_SUCCESSFUL, Response.ResponseType.LOGIN_FAILED);
    }

    private static boolean registerStep() {
        return lrstep(Request.RequestType.REGISTER_USER, Response.ResponseType.REGISTER_SUCCESSFUL, Response.ResponseType.REGISTER_FAILED);
    }

    private static boolean lrstep(Request.RequestType requestType, Response.ResponseType responseTypeSuccess, Response.ResponseType responseTypeFail) {
        UserProfile userProfile;
        try {
            userProfile = new UserProfile(ClientController.readLogin(),ClientController.readPassword());
        } catch (IllegalArgumentException e) {
            ClientController.printlnErr(e.getMessage());
            return false;
        }
        RequestBuilder.setUserProfile(userProfile);
        RequestBuilder.createNewRequest(requestType);
        try {
            Response response = ClientConnector.sendToServer(RequestBuilder.getRequest());
            if (response.getResponseType() == responseTypeFail) {
                ClientController.printlnErr(response.getMessage());
            } else if (response.getResponseType() == responseTypeSuccess) {
                ClientExecutor.initialize();
                addLogoutHook();
                return true;
            } else {
                throw new IOException("Server has wrong logic: expected \"" +
                        responseTypeFail +
                        "\" or \"" +
                        responseTypeSuccess +
                        "\", but not \"" +
                        response.getResponseType() +
                        "\"");
            }
        } catch (IOException | ClassNotFoundException e) {
            ClientController.printlnErr(e.getMessage());
        }
        return false;
    }

    private static void addLogoutHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            RequestBuilder.createNewRequest(Request.RequestType.LOGOUT_USER);
            try {
                ClientConnector.sendRequest(RequestBuilder.getRequest());
            } catch (IOException e) {
                //ignore
            }
        }));
    }

    private static void executionStep() {
        ClientController.initialize();

        while (true) {
            try {
                ClientExecutor.parseCommand(ClientController.readLine());
                Response response = ClientConnector.sendToServer(RequestBuilder.getRequest());
                if (response.getResponseType() == Response.ResponseType.EXECUTION_SUCCESSFUL) {
                    ClientController.println(response.getMessage());
                } else if (response.getResponseType() == Response.ResponseType.EXECUTION_FAILED) {
                    ClientController.printlnErr(response.getMessage());
                } else {
                    ClientController.printlnErr("Server has wrong logic: expected \"" +
                            Response.ResponseType.EXECUTION_FAILED +
                            "\" or \"" +
                            Response.ResponseType.EXECUTION_SUCCESSFUL +
                            "\", but not \"" +
                            response.getResponseType() +
                            "\"");
                }
            } catch (SocketTimeoutException e) {
                ClientController.printlnErr("Server isn't responding (try again later or choose another server)");
            } catch (IOException | ClassNotFoundException | CommandException e) {
                ClientController.printlnErr(e.getMessage());
            }
        }
    }
}
