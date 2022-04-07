package Commands;

import MovieObjects.Movie;
import Server.ResponseBuilder;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.util.stream.Collectors;

/**
 * Command, which groups the elements by the value of the "length" field, prints the number of elements in each group
 * @see NameableCommand
 */
public class GroupCountingByLengthCommand extends NameableCommand {

    public GroupCountingByLengthCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        server.getResponseBuilder().add("*groups by length*");
        server.getMovieCollection().values().stream()
                .collect(Collectors.groupingBy(Movie::getLength, Collectors.counting()))
                .forEach((length, count) -> server.getResponseBuilder().add(count + " movies with length " + length));
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName(), 0);
        }
    }
}
