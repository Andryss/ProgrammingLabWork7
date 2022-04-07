package Server;

import Client.Request;
import Commands.Command;
import Commands.CommandException;

import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>ServerExecutor implements (2) step in ServerManager:</p>
 * <p>1) Get command queue from Request</p>
 * <p>2) Validate it in some times</p>
 * <p>3) Execute commands and make them build Response</p>
 */
public class ServerExecutor {
    private static ExecutorService executorService;

    private final SocketAddress client;
    private final Request request;
    private ServerINFO serverINFO;

    ServerExecutor(SocketAddress client, Request request) {
        this.client = client;
        this.request = request;
    }

    static void initialize() {
        executorService = Executors.newCachedThreadPool();
    }

    void executeRequest() {
        ServerController.info("Request starts executing");

        if (request.getRequestType() == Request.RequestType.CHECK_CONNECTION) {
            checkConnectionRequest();
        } else if (request.getRequestType() == Request.RequestType.LOGIN_USER) {
            loginUserRequest();
        } else if (request.getRequestType() == Request.RequestType.REGISTER_USER) {
            registerUserRequest();
        } else if (request.getRequestType() == Request.RequestType.EXECUTE_COMMAND) {
            executeCommandRequest();
        } else {
            ServerController.info("Incorrect request type: " + request.getRequestType());
        }

        ServerController.info("Request executed");

        try {
            ServerCollectionManager.printTables();
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        }
    }

    private void checkConnectionRequest() {
        ResponseBuilder responseBuilder = ResponseBuilder.createNewResponse(
                Response.ResponseType.CONNECTION_SUCCESSFUL,
                "Connection with server was successful"
        );
        new Thread(() -> ServerConnector.sendToClient(client, responseBuilder.getResponse()), "SendingCCThread").start();
    }

    private void loginUserRequest() {
        boolean userPresented = ServerCollectionManager.isUserPresented(request.getUserName(), request.getUserPassword());
        ResponseBuilder responseBuilder = ResponseBuilder.createNewResponse(
                userPresented ? Response.ResponseType.LOGIN_SUCCESSFUL : Response.ResponseType.LOGIN_FAILED,
                userPresented ? "User successfully logged in" : "Incorrect login or password"
        );
        new Thread(() -> ServerConnector.sendToClient(client, responseBuilder.getResponse()), "SendingLUThread").start();
    }

    private void registerUserRequest() {
        ResponseBuilder responseBuilder;
        try {
            long newUserID = ServerCollectionManager.registerUser(request.getUserName(), request.getUserPassword());
            responseBuilder = ResponseBuilder.createNewResponse(
                    newUserID == -1 ? Response.ResponseType.REGISTER_FAILED : Response.ResponseType.REGISTER_SUCCESSFUL,
                    newUserID == -1 ? "User is already registered" : "New user successfully registered"
            );
        } catch (SQLException throwables) {
            ServerController.error("Error while registering new user: ", throwables);
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.LOGIN_FAILED,
                    "Some server error, try again later"
            );
        }
        ResponseBuilder finalResponseBuilder = responseBuilder;
        new Thread(() -> ServerConnector.sendToClient(client, finalResponseBuilder.getResponse()), "SendingRUThread").start();
    }

    private void executeCommandRequest() {
        ResponseBuilder responseBuilder = ResponseBuilder.createNewResponse(Response.ResponseType.EXECUTION_SUCCESSFUL);
        serverINFO = new ServerINFO(request.getUserName(), request.getUserPassword(), responseBuilder);

        Queue<Command> commandQueue = request.getCommandQueue();
        try {
            responseBuilder.add("\u001B[34m" + "START: command \"" + request.getCommandName() + "\" start executing" + "\u001B[0m");
            if (commandQueue.size() > 1) {
                validateCommands(commandQueue);
            }
            for (Command command : commandQueue) {
                command.execute(ExecuteState.EXECUTE, serverINFO);
            }
            responseBuilder.add("\u001B[32m" + "SUCCESS: command \"" + request.getCommandName() + "\" successfully completed" + "\u001B[0m");
        } catch (CommandException e) {
            responseBuilder = ResponseBuilder.createNewResponse(Response.ResponseType.EXECUTION_FAILED, e.getMessage());
        }

        ResponseBuilder finalResponseBuilder = responseBuilder;
        new Thread(() -> ServerConnector.sendToClient(client, finalResponseBuilder.getResponse()), "SendingECThread").start();
    }

    private void validateCommands(Queue<Command> commandQueue) throws CommandException {
        ServerINFO copiedServerINFO = serverINFO.validationClone();

        for (Command command : commandQueue) {
            try {
                command.execute(ExecuteState.VALIDATE, copiedServerINFO);
            } catch (CommandException e) {
                throw new CommandException(e.getCommand(), "Error in validation: " + e.getMessage());
            }
        }
    }

    public static ExecutorService getService() {
        return executorService;
    }

    /**
     * Enum with two main states of executing command
     */
    public enum ExecuteState {
        /**
         * When command do what it should do (with logging)
         */
        EXECUTE,
        /**
         * When command is validating, for example, in script (without logging)
         */
        VALIDATE
    }
}
