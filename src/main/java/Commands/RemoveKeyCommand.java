package Commands;

import Client.RequestBuilder;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;

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
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.removeMovie(key);
            if (state == ServerExecutor.ExecuteState.EXECUTE) {
                server.getResponseBuilder().add("Element with key \"" + key + "\" has been removed");
            }
        } catch (SQLException | IllegalAccessException e) {
            server.getResponseBuilder().add("Nothing has been removed");
        }
        return true;
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
    public void buildRequest() throws CommandException {
        RemoveKeyCommand command = new RemoveKeyCommand(getCommandName());
        command.key = key;
        RequestBuilder.add(command);
    }
}
