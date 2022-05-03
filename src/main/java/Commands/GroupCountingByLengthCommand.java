package Commands;

import MovieObjects.Movie;
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
    public void execute(ServerINFO server) throws CommandException {
        server.getResponse().addMessage("*groups by length*");
        server.getMovieCollection().values().stream()
                .collect(Collectors.groupingBy(Movie::getLength, Collectors.counting()))
                .forEach((length, count) -> server.getResponse().addMessage(count + " movies with length " + length));
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName(), 0);
        }
    }
}
