package Commands;

import MovieObjects.Movie;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Hashtable;

/**
 * Command, which prints short info about the collection (type, init date, length etc.)
 * @see NameableCommand
 */
public class InfoCommand extends NameableCommand {

    public InfoCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        Hashtable<Integer, Movie> collection = server.getMovieCollection();
        server.getResponseBuilder()
                .add("Collection type: " + collection.getClass().getName())
                .add("Collection length: " + collection.size());
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
