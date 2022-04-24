package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

/**
 * Command, which clears the collection
 * @see NameableCommand
 */
public class ClearCommand extends NameableCommand {

    public ClearCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.removeAllMovies();
        } catch (IllegalAccessException e) {
            //ignore
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("All your elements deleted");
        }
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
