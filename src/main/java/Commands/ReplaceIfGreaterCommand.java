package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Command, which replaces an element by key if the new value is greater than the old one
 * @see NameableCommand
 */
public class ReplaceIfGreaterCommand extends ElementCommand {

    public ReplaceIfGreaterCommand(String commandName, Scanner reader) {
        this(commandName, reader, false);
    }

    public ReplaceIfGreaterCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName, reader, readingFromFile);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        if (readMovie.compareTo(server.getMovieCollection().get(key)) > 0) {
            try {
                server.putMovie(key, readMovie);
            } catch (SQLException e) {
                throw new CommandException(getCommandName(), "movie with key \"" + key + "\" doesn't exists");
            } catch (IllegalAccessException e) {
                //ignore
            }
            if (state == ServerExecutor.ExecuteState.EXECUTE) {
                server.getResponseBuilder().add("Element greater than the old one has been inserted");
            }
        } else {
            if (state == ServerExecutor.ExecuteState.EXECUTE) {
                server.getResponseBuilder().add("Nothing was happened");
            }
        }
        return true;
    }
}
