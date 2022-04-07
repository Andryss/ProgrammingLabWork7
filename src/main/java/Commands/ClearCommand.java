package Commands;

import MovieObjects.Movie;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;
import java.util.Hashtable;

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
        Hashtable<Integer, Movie> collection = server.getMovieCollection();
        for (Integer key : collection.keySet()) {
            try {
                server.removeMovie(key);
            } catch (SQLException | IllegalAccessException e) {
                //ignore
            }
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("All possible elements deleted");
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
