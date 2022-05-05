package client;

import general.commands.CommandException;
import general.element.UserProfile;
import general.Request;
import general.Response;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * <p>ClientManager consist of main client logic:</p>
 * <p>0) Initialization step</p>
 * <p>1) Create connection step</p>
 * <p>2) Login/register user step</p>
 * <p>3) Execution step</p>
 *
 * <p>Execution step consist of:</p>
 * <p>1) Read command from CMD</p>
 * <p>2) Validate command and build Request</p>
 * <p>3) Send Request to the server and get Response</p>
 * <p>4) Print Server response</p>
 */
public class ClientManager {

    public static void run(Properties properties) throws IOException, ClassNotFoundException, CommandException {
        try {
            initializationStep(properties);
            connectionStep();
            loginRegisterStep();
            executionStep();
        } catch (NoSuchElementException e) {
            ClientController.printlnErr("Incorrect input (EOF). Try not to be so unpredictable!");
        }
    }

    private static void initializationStep(Properties properties) throws IOException, CommandException, NumberFormatException {
        ClientConnector.setProperties(properties);
        ClientExecutor.initialize();
    }

    private static void connectionStep() throws IOException, ClassNotFoundException {
        ClientConnector.initialize();
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
        try {
            Response response = ClientConnector.sendToServer(
                    RequestBuilder.createNewRequest().setRequestType(requestType).build()
            );
            if (response.getResponseType() == responseTypeFail) {
                ClientController.printlnErr(response.getMessage());
            } else if (response.getResponseType() == responseTypeSuccess) {
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
            try {
                ClientConnector.sendRequest(
                        RequestBuilder.createNewRequest().setRequestType(Request.RequestType.LOGOUT_USER).build()
                );
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
                Response response = ClientConnector.sendToServer(ClientExecutor.getRequest());
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
                ClientController.printlnErr("Server isn't responding (try again later)");
            } catch (IOException | ClassNotFoundException | CommandException e) {
                ClientController.printlnErr(e.getMessage());
            }
        }
    }
}
