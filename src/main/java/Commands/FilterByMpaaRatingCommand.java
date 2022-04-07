package Commands;

import MovieObjects.Movie;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command, which prints an elements whose "mpaaRating" is equal to the given
 * @see NameableCommand
 */
public class FilterByMpaaRatingCommand extends NameableCommand {
    private Movie.MpaaRating mpaaRating;

    public FilterByMpaaRatingCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        server.getResponseBuilder().add("Found movies with \"" + mpaaRating + "\" mpaa rating:");
        List<Map.Entry<Integer,Movie>> found = server.getMovieCollection().entrySet().stream()
                .filter(entry -> entry.getValue().getMpaaRating() == mpaaRating)
                .collect(Collectors.toList());
        if (found.size() == 0) {
            server.getResponseBuilder().add("*nothing*");
        } else {
            found.forEach(entry -> server.getResponseBuilder().add(entry.getKey() + " - " + entry.getValue()));
        }
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            mpaaRating = Movie.MpaaRating.valueOf(args[0]);
        } catch (IllegalArgumentException e) {
            throw new BadArgumentsFormatException(getCommandName(), "value must be one of: " + Arrays.toString(Movie.MpaaRating.values()));
        }
    }
}
