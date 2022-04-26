package Server;

import Client.Request;
import Commands.Command;
import Commands.CommandException;
import MovieObjects.UserProfile;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * ServerExecutor executing Request depending on RequestType and starting Thread which sending server Response
 *
 * Also, ServerExecutor monitor authorized users
 */
public class ServerExecutor {
    private static ExecutorService executorService;
    private static final List<UserProfile> authorizedUsers = new CopyOnWriteArrayList<>();

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
        } else if (request.getRequestType() == Request.RequestType.LOGOUT_USER) {
            logoutUserRequest();
        } else if (request.getRequestType() == Request.RequestType.REGISTER_USER) {
            registerUserRequest();
        } else if (request.getRequestType() == Request.RequestType.EXECUTE_COMMAND) {
            executeCommandRequest();
        } else {
            ServerController.info("Incorrect request type: " + request.getRequestType());
        }

        ServerController.info("Request executed");

        printUsers();
        ServerCollectionManager.printTables();
    }

    private void checkConnectionRequest() {
        ResponseBuilder responseBuilder = ResponseBuilder.createNewResponse(
                Response.ResponseType.CONNECTION_SUCCESSFUL,
                "Connection with server was successful"
        );
        new Thread(() -> ServerConnector.sendToClient(client, responseBuilder.getResponse()), "SendingCCThread").start();
    }

    private void loginUserRequest() {
        ResponseBuilder responseBuilder;
        if (ServerCollectionManager.isUserPresented(request.getUserProfile())) {
            if (authorizedUsers.contains(request.getUserProfile())) {
                responseBuilder = ResponseBuilder.createNewResponse(
                        Response.ResponseType.LOGIN_FAILED,
                        "User already authorized (multi-session is not supported)"
                );
            } else {
                authorizedUsers.add(request.getUserProfile());
                responseBuilder = ResponseBuilder.createNewResponse(
                        Response.ResponseType.LOGIN_SUCCESSFUL,
                        "User successfully logged in"
                );
            }
        } else {
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.LOGIN_FAILED,
                    "Incorrect login or password"
            );
        }
        new Thread(() -> ServerConnector.sendToClient(client, responseBuilder.getResponse()), "SendingLUThread").start();
    }

    private void logoutUserRequest() {
        authorizedUsers.stream().filter(u -> u.equals(request.getUserProfile())).forEach(authorizedUsers::remove);
    }

    static void logoutUser(String userName) {
        authorizedUsers.removeAll(authorizedUsers.stream().filter(u -> u.getName().equals(userName)).collect(Collectors.toList()));
    }

    private void registerUserRequest() {
        ResponseBuilder responseBuilder;
        long newUserID = ServerCollectionManager.registerUser(request.getUserProfile());
        if (newUserID == -1) {
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.REGISTER_FAILED,
                    "User is already registered"
            );
        } else {
            authorizedUsers.add(request.getUserProfile());
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.REGISTER_SUCCESSFUL,
                    "New user successfully registered"
            );
        }
        new Thread(() -> ServerConnector.sendToClient(client, responseBuilder.getResponse()), "SendingRUThread").start();
    }

    private void executeCommandRequest() {
        ResponseBuilder responseBuilder;
        if (authorizedUsers.stream().noneMatch((u) -> u.equals(request.getUserProfile()))) {
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.EXECUTION_FAILED,
                    "User isn't logged in yet"
            );
        } else {
            responseBuilder = ResponseBuilder.createNewResponse(
                    Response.ResponseType.EXECUTION_SUCCESSFUL
            );
            serverINFO = new ServerINFO(request.getUserProfile(), responseBuilder);

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
                responseBuilder = ResponseBuilder.createNewResponse(
                        Response.ResponseType.EXECUTION_FAILED,
                        e.getMessage()
                );
            }
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

    static void printUsers() {
        ServerController.info("Authorized users: " + authorizedUsers);
    }

    public static ExecutorService getService() {
        return executorService;
    }
    static List<UserProfile> getAuthorizedUsers() {return authorizedUsers;}

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
