package server;

import general.Request;
import general.commands.Command;
import general.commands.CommandException;
import general.element.Movie;
import general.element.UserProfile;
import general.Response;
import general.ServerINFO;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServerExecutor executing Request depending on RequestType and starting Thread which sending server Response
 *
 * Also, ServerExecutor monitor authorized users
 */
public class ServerExecutor {
    private static ExecutorService executorService;     // Follow "Object pool" pattern
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

    static void close() {
        try {
            executorService.shutdown();
        } catch (Throwable e) {
            // ignore
        }
    }

    void executeRequest() {
        ServerController.info("Request starts executing");

        try {
            if (request.getRequestType() == Request.RequestType.CHECK_CONNECTION) {
                checkConnectionRequest();
            } else if (request.getRequestType() == Request.RequestType.LOGIN_USER) {
                loginUserRequest();
            } else if (request.getRequestType() == Request.RequestType.LOGOUT_USER) {
                logoutUserRequest();
            } else if (request.getRequestType() == Request.RequestType.REGISTER_USER) {
                registerUserRequest();
            } else if (request.getRequestType() == Request.RequestType.CHECK_ELEMENT) {
                checkElementRequest();
            } else if (request.getRequestType() == Request.RequestType.EXECUTE_COMMAND) {
                executeCommandRequest();
            } else {
                ServerController.info("Unexpected request type: " + request.getRequestType());
            }
        } catch (NullPointerException e) {
            Response response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.WRONG_REQUEST_FORMAT)
                    .addMessage("Wrong request format")
                    .build();
            new Thread(() -> ServerConnector.sendToClient(client, response), "SendingWFThread").start();
        }

        ServerController.info("Request executed");

        printUsers();
        ServerCollectionManager.printTables();
    }

    private void checkConnectionRequest() {
        Response response = ResponseBuilder.createNewResponse()
                .setResponseType(Response.ResponseType.CONNECTION_SUCCESSFUL)
                .addMessage("Connection with server was successful")
                .build();
        new Thread(() -> ServerConnector.sendToClient(client, response), "SendingCCThread").start();
    }

    private void loginUserRequest() {
        Response response;
        if (ServerCollectionManager.isUserPresented(request.getUserProfile())) {
            if (authorizedUsers.contains(request.getUserProfile())) {
                response = ResponseBuilder.createNewResponse()
                        .setResponseType(Response.ResponseType.LOGIN_FAILED)
                        .addMessage("User already authorized (multi-session is not supported)")
                        .build();
            } else {
                authorizedUsers.add(request.getUserProfile());
                response = ResponseBuilder.createNewResponse()
                        .setResponseType(Response.ResponseType.LOGIN_SUCCESSFUL)
                        .addMessage("User successfully logged in")
                        .build();
                ServerHistoryManager.updateUser(request.getUserProfile());
            }
        } else {
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.LOGIN_FAILED)
                    .addMessage("Incorrect login or password")
                    .build();
        }
        new Thread(() -> ServerConnector.sendToClient(client, response), "SendingLUThread").start();
    }

    private void logoutUserRequest() {
        authorizedUsers.remove(request.getUserProfile());
        ServerHistoryManager.deleteUser(request.getUserProfile());
    }

    static void logoutUser(String userName) {
        authorizedUsers.stream().filter(u -> u.getName().equals(userName))
                .forEach(u -> {
                    authorizedUsers.remove(u);
                    ServerHistoryManager.deleteUser(u);
                });
    }

    private void checkElementRequest() {
        Response response;
        if (authorizedUsers.stream().noneMatch((u) -> u.equals(request.getUserProfile()))) {
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.CHECKING_FAILED)
                    .addMessage("User isn't logged in yet (or connection support time is out)")
                    .build();
        } else {
            Movie movie = ServerCollectionManager.getMovie(request.getCheckingIndex());
            if (movie == null) {
                response = ResponseBuilder.createNewResponse()
                        .setResponseType(Response.ResponseType.ELEMENT_NOT_PRESENTED)
                        .addMessage("Movie with key \"" + request.getCheckingIndex() + "\" doesn't exist")
                        .build();
            } else {
                if (!movie.getOwner().equals(request.getUserProfile().getName())) {
                    response = ResponseBuilder.createNewResponse()
                            .setResponseType(Response.ResponseType.PERMISSION_DENIED)
                            .addMessage("User \"" + request.getUserProfile().getName() + "\" doesn't have permission to update movie with key \"" + request.getCheckingIndex() + "\"")
                            .build();
                } else {
                    response = ResponseBuilder.createNewResponse()
                            .setResponseType(Response.ResponseType.CHECKING_SUCCESSFUL)
                            .addMessage("User \"" + request.getUserProfile().getName() + "\" have permission to update movie with key \"" + request.getCheckingIndex() + "\"")
                            .build();
                }
            }
            ServerHistoryManager.updateUser(request.getUserProfile());
        }
        new Thread(() -> ServerConnector.sendToClient(client, response), "SendingCEThread").start();
    }

    private void registerUserRequest() {
        Response response;
        long newUserID = ServerCollectionManager.registerUser(request.getUserProfile());
        if (newUserID == -1) {
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.REGISTER_FAILED)
                    .addMessage("User is already registered")
                    .build();
        } else {
            authorizedUsers.add(request.getUserProfile());
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.REGISTER_SUCCESSFUL)
                    .addMessage("New user successfully registered")
                    .build();
            ServerHistoryManager.updateUser(request.getUserProfile());
        }
        new Thread(() -> ServerConnector.sendToClient(client, response), "SendingRUThread").start();
    }

    private void executeCommandRequest() {
        Response response;
        if (authorizedUsers.stream().noneMatch((u) -> u.equals(request.getUserProfile()))) {
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.EXECUTION_FAILED)
                    .addMessage("User isn't logged in yet (or connection support time is out)")
                    .build();
        } else {
            response = ResponseBuilder.createNewResponse()
                    .setResponseType(Response.ResponseType.EXECUTION_SUCCESSFUL)
                    .build();
            ServerHistoryManager.updateUser(request.getUserProfile());
            serverINFO = new ServerINFOImpl(request.getUserProfile(), response);

            Queue<Command> commandQueue = request.getCommandQueue();
            try {
                response.addMessage("\u001B[34m" + "START: command \"" + request.getCommandName() + "\" start executing" + "\u001B[0m");
                if (commandQueue.size() > 1) {
                    validateCommands(commandQueue);
                }
                for (Command command : commandQueue) {
                    command.execute(serverINFO);
                }
                response.addMessage("\u001B[32m" + "SUCCESS: command \"" + request.getCommandName() + "\" successfully completed" + "\u001B[0m");
                ServerHistoryManager.addUserHistory(request.getUserProfile(), request.getCommandName());
            } catch (CommandException e) {
                response = ResponseBuilder.createNewResponse()
                        .setResponseType(Response.ResponseType.EXECUTION_FAILED)
                        .addMessage(e.getMessage())
                        .build();
            }
        }
        Response finalResponse = response;
        new Thread(() -> ServerConnector.sendToClient(client, finalResponse), "SendingECThread").start();
    }

    private void validateCommands(Queue<Command> commandQueue) throws CommandException {
        ServerINFO copiedServerINFO = serverINFO.validationClone();

        for (Command command : commandQueue) {
            try {
                command.execute(copiedServerINFO);
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
}
