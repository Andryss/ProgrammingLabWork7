package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;

/**
 * Command, which removes all elements whose key is less than given
 * @see NameableCommand
 */
public class RemoveLowerKeyCommand extends NameableCommand {
    private Integer key;

    public RemoveLowerKeyCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        server.getMovieCollection().keySet().stream()
                .filter(key -> key < this.key)
                .forEach(key -> {
                    try {
                        server.removeMovie(key);
                    } catch (SQLException | IllegalAccessException e) {
                        //ignore
                    }
                });
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("All your elements with key lower than \"" + key + "\" has been removed");
        }
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            this.key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "integer");
        }
    }
}
