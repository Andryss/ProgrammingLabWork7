package Commands;

import Client.Request;
import Server.ServerINFO;

/**
 * Command, which deletes an element with given key
 * @see NameableCommand
 */
public class RemoveKeyCommand extends NameableCommand {
    private Integer key;

    public RemoveKeyCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        try {
            server.removeMovie(key);
            server.getResponse().addMessage("Element with key \"" + key + "\" has been removed");
        } catch (IllegalAccessException e) {
            server.getResponse().addMessage("Nothing has been removed (" + e.getMessage() + ")");
        }
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "integer");
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        RemoveKeyCommand command = new RemoveKeyCommand(getCommandName());
        command.key = key;
        request.addCommand(command);
    }
}
