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
    private static final HashMap<String, Command> commandMap = new HashMap<>();
    private static final ClientINFO clientINFO = new ClientINFOImpl();
    private static Request request;

    static void initialize() throws IOException, CommandException {
        fillCommandMap();
    }

    private static void fillCommandMap() throws IOException, CommandException {

        CommandFiller.fillCommandMap(commandMap);

    }

    static void parseCommand(String inputLine) throws CommandException {
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

    private static void executeCommand(String commandName, String[] args) throws CommandException {
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

    public static HashMap<String,Command> getCommandMap() {
        return commandMap;
    }
    public static Request getRequest() {
        return request;
    }
}
