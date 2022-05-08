package client;

import general.ClientINFO;
import general.commands.*;
import general.Request;

import java.io.IOException;
import java.util.*;

/**
 * <p>ClientExecutor implements (2) step in ClientManager:</p>
 * <p>1) Split input into command name and args</p>
 * <p>2) Check if command name is valid</p>
 * <p>3) Check if args for this command is valid</p>
 * <p>4) Make command build Request</p>
 */
public class ClientExecutor {
    private static final ClientExecutor instance = new ClientExecutor();
    private final HashMap<String, Command> commandMap = new HashMap<>();
    private final ClientINFO clientINFO = new ClientINFOImpl();
    private Request request;

    private ClientExecutor() {}

    public static ClientExecutor getInstance() {
        return instance;
    }

    void initialize() throws IOException, CommandException {
        fillCommandMap();
    }

    private void fillCommandMap() throws IOException, CommandException {

        CommandFiller.fillCommandMap(commandMap);

    }

    void parseCommand(String inputLine) throws CommandException {
        String[] operands = inputLine.trim().split("\\s+", 2);
        if (operands.length == 0) {
            throw new UndefinedCommandException("");
        } else if (operands.length == 1) {
            executeCommand(operands[0], new String[0]);
        } else {
            String[] args = operands[1].split("\\s+");
            executeCommand(operands[0], args);
        }
    }

    private void executeCommand(String commandName, String[] args) throws CommandException {
        Command command = commandMap.get(commandName);
        if (command == null) {
            throw new UndefinedCommandException(commandName);
        }
        command.setArgs(clientINFO, args);
        request = RequestBuilder.createNewRequest()
                .setRequestType(Request.RequestType.EXECUTE_COMMAND)
                .setCommandName(commandName)
                .build();
        command.buildRequest(request);
    }

    public HashMap<String,Command> getCommandMap() {
        return commandMap;
    }
    public Request getRequest() {
        return request;
    }
}
