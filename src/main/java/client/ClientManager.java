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
    private static final ClientManager instance = new ClientManager();

    private ClientManager() {}

    public static ClientManager getInstance() {
        return instance;
    }

    public void run(Properties properties) throws IOException, ClassNotFoundException, CommandException {
        try {
            initializationStep(properties);
            connectionStep();
            loginRegisterStep();
            executionStep();
        } catch (NoSuchElementException e) {
            ClientController.getInstance().printlnErr("Incorrect input (EOF). Try not to be so unpredictable!");
        }
    }

    private void initializationStep(Properties properties) throws IOException, CommandException, NumberFormatException {
        ClientConnector.getInstance().setProperties(properties);
        ClientExecutor.getInstance().initialize();
    }

    private void connectionStep() throws IOException, ClassNotFoundException {
        ClientConnector.getInstance().initialize();
    }

    private void loginRegisterStep() {
        loginRegisterStep:
        while (true) {
            ClientController.getInstance().print("Do you want (\"l[ogin]\" or \"r[egister]\"): ");
            String line = ClientController.getInstance().readLine().trim();
            switch (line) {
                case "exit":
                    System.exit(0);
                case "login":
                case "l":
                    if (loginStep()) {
                        ClientController.getInstance().printlnGood("User successfully logged in");
                        break loginRegisterStep;
                    }
                    break;
                case "register":
                case "r":
                    if (registerStep()) {
                        ClientController.getInstance().printlnGood("New user successfully registered");
                        break loginRegisterStep;
                    }
                    break;
                default:
                    ClientController.getInstance().printlnErr("Unknown command \"" + line + "\"");
                    break;
            }
        }
    }

    private boolean loginStep() {
        return lrstep(Request.RequestType.LOGIN_USER, Response.ResponseType.LOGIN_SUCCESSFUL, Response.ResponseType.LOGIN_FAILED);
    }

    private boolean registerStep() {
        return lrstep(Request.RequestType.REGISTER_USER, Response.ResponseType.REGISTER_SUCCESSFUL, Response.ResponseType.REGISTER_FAILED);
    }

    private boolean lrstep(Request.RequestType requestType, Response.ResponseType responseTypeSuccess, Response.ResponseType responseTypeFail) {
        UserProfile userProfile;
        try {
            userProfile = new UserProfile(ClientController.getInstance().readLogin(),ClientController.getInstance().readPassword());
        } catch (IllegalArgumentException e) {
            ClientController.getInstance().printlnErr(e.getMessage());
            return false;
        }
        RequestBuilder.setUserProfile(userProfile);
        try {
            Response response = ClientConnector.getInstance().sendToServer(
                    RequestBuilder.createNewRequest().setRequestType(requestType).build()
            );
            if (response.getResponseType() == responseTypeFail) {
                ClientController.getInstance().printlnErr(response.getMessage());
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
            ClientController.getInstance().printlnErr(e.getMessage());
        }
        return false;
    }

    private void addLogoutHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ClientConnector.getInstance().sendRequest(
                        RequestBuilder.createNewRequest().setRequestType(Request.RequestType.LOGOUT_USER).build()
                );
            } catch (IOException e) {
                //ignore
            }
        }));
    }

    private void executionStep() {
        ClientController.getInstance().initialize();

        // DDOS code :)
//        try {
//            ClientExecutor.getInstance().parseCommand("execute_script script");
//            for (int i = 0; i < Runtime.getRuntime().availableProcessors() / 2; i++) {
//                new Thread(() -> {
//                    long start = System.currentTimeMillis();
//                    for (int j = 0; j < 100_000; j++) {
//                        try {
//                            synchronized (ClientExecutor.getInstance()) {
//                                ClientConnector.getInstance().sendRequest(ClientExecutor.getInstance().getRequest());
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.println(System.currentTimeMillis() - start);
//                }).start();
//            }
//        } catch (CommandException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(20_000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.exit(0);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                ClientExecutor.getInstance().parseCommand(ClientController.getInstance().readLine());
                Response response = ClientConnector.getInstance().sendToServer(ClientExecutor.getInstance().getRequest());
                if (response.getResponseType() == Response.ResponseType.EXECUTION_SUCCESSFUL) {
                    ClientController.getInstance().println(response.getMessage());
                } else if (response.getResponseType() == Response.ResponseType.EXECUTION_FAILED) {
                    ClientController.getInstance().printlnErr(response.getMessage());
                } else {
                    ClientController.getInstance().printlnErr("Server has wrong logic: expected \"" +
                            Response.ResponseType.EXECUTION_FAILED +
                            "\" or \"" +
                            Response.ResponseType.EXECUTION_SUCCESSFUL +
                            "\", but not \"" +
                            response.getResponseType() +
                            "\"");
                }
            } catch (SocketTimeoutException e) {
                ClientController.getInstance().printlnErr("Server isn't responding (try again later)");
            } catch (IOException | ClassNotFoundException | CommandException e) {
                ClientController.getInstance().printlnErr(e.getMessage());
            }
        }
    }
}
