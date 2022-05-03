package Client;

import Commands.*;

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
    private static Request request;

    static void initialize() {
        fillCommandMap();
    }

    private static void fillCommandMap() {
        commandMap.put("help", new HelpCommand("help"));
        commandMap.put("info", new InfoCommand("info"));
        commandMap.put("show", new ShowCommand("show"));
        commandMap.put("insert", new InsertCommand("insert", ClientController.getReader()));
        commandMap.put("update", new UpdateCommand("update", ClientController.getReader()));
        commandMap.put("remove_key", new RemoveKeyCommand("remove_key"));
        commandMap.put("clear", new ClearCommand("clear"));
        //save --- FORBIDDEN!
        commandMap.put("execute_script", new ExecuteScriptCommand("execute_script", null));
        commandMap.put("exit", new ExitCommand("exit"));
        commandMap.put("history", new HistoryCommand("history"));
        commandMap.put("replace_if_greater", new ReplaceIfGreaterCommand("replace_if_greater", ClientController.getReader()));
        commandMap.put("remove_lower_key", new RemoveLowerKeyCommand("remove_key_command"));
        commandMap.put("group_counting_by_length", new GroupCountingByLengthCommand("group_counting_by_length"));
        commandMap.put("count_less_than_length", new CountLessThenLengthCommand("count_less_than_length"));
        commandMap.put("filter_by_mpaa_rating", new FilterByMpaaRatingCommand("filter_by_mpaa_rating"));
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
        command.setArgs(args);
        request = RequestBuilder.createNewRequest(Request.RequestType.EXECUTE_COMMAND, commandName);
        command.buildRequest(request);
    }

    public static HashMap<String,Command> getCommandMap() {
        return commandMap;
    }
    public static Request getRequest() {
        return request;
    }
}
