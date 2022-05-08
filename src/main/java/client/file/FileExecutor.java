package client.file;

import client.ClientExecutor;
import client.ClientINFOImpl;
import general.ClientINFO;
import general.Request;
import general.commands.*;
import general.ServerINFO;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * <p>FileExecutor implements (2) step in FileManager:</p>
 * <p>*** do same things as ClientExecutor but with some changes in ClientINFO (more sensitive to any Exceptions) ***</p>
 */
public class FileExecutor {
    @SuppressWarnings("unchecked")
    private final HashMap<String, Command> commandMap = (HashMap<String, Command>) ClientExecutor.getInstance().getCommandMap().clone();
    private final FileController controller;
    private final ClientINFO clientINFO;
    private final FileExecutor caller;
    private final Request request;
    private int commandNumber = 1;

    public FileExecutor(FileController fileController, FileExecutor caller, Request request) {
        fillCommandMap();
        this.caller = caller;
        this.controller = fileController;
        this.clientINFO = new ClientINFOImpl.ClientINFOFromFileImpl(this.controller, caller);
        this.request = request;
    }

    private void fillCommandMap() {
        commandMap.put("exit", new NameableCommand("exit") {
            @Override
            public void execute(ServerINFO server) throws CommandException {
                throw new BadArgumentsException(getCommandName(), "do you really want \"exit\" in script? Sorry, not today");
            }

            @Override
            public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
                if (args.length > 0) {
                    throw new BadArgumentsCountException(getCommandName());
                }
                throw new BadArgumentsException(getCommandName(), "do you really want \"exit\" in script? Sorry, not today");
            }
        });
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
        commandNumber++;
    }

    private void executeCommand(String commandName, String[] args) throws CommandException {
        Command command = commandMap.get(commandName);
        if (command == null) {
            throw new UndefinedCommandException(commandName);
        }
        try {
            command.setArgs(clientINFO, args);
        } catch (NoSuchElementException e) {
            throw new CommandException(commandName, "File ended before command \"" + commandName + "\" completed");
        }
        try {
            command.buildRequest(request);
        } catch (CommandException e) {
            throw new CommandException(commandName, e.getReason());
        }
    }

    public FileExecutor getCaller() {
        return caller;
    }
    public String getFileName() {
        return controller.getFileName();
    }
    public int getCommandNumber() {
        return commandNumber;
    }
}
