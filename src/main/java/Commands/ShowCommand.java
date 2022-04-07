package Commands;

import MovieObjects.Movie;
import Server.ResponseBuilder;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.util.Hashtable;

/**
 * Command, which prints all elements in the collection
 * @see NameableCommand
 */
public class ShowCommand extends NameableCommand {

    public ShowCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) {
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        server.getResponseBuilder().add("Collection contains:");
        Hashtable<Integer,Movie> collection = server.getMovieCollection();
        if (collection.size() == 0) {
            server.getResponseBuilder().add("*nothing*");
        } else {
            collection.entrySet().stream().sorted((o1, o2) -> {
                double len1 = Math.sqrt(Math.pow(o1.getValue().getCoordinates().getX(), 2) + Math.pow(o1.getValue().getCoordinates().getY(), 2));
                double len2 = Math.sqrt(Math.pow(o2.getValue().getCoordinates().getX(), 2) + Math.pow(o2.getValue().getCoordinates().getY(), 2));
                return Double.compare(len1, len2);
            }).forEach(entry -> server.getResponseBuilder().add(entry.getKey() + " - " + entry.getValue()));
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
