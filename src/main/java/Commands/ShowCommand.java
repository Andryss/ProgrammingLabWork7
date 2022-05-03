package Commands;

import MovieObjects.Movie;
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
    public void execute(ServerINFO server) {
        server.getResponse().addMessage("Collection contains:");
        Hashtable<Integer,Movie> collection = server.getMovieCollection();
        if (collection.size() == 0) {
            server.getResponse().addMessage("*nothing*");
        } else {
            collection.entrySet().stream().sorted((o1, o2) -> {
                double len1 = Math.sqrt(Math.pow(o1.getValue().getCoordinates().getX(), 2) + Math.pow(o1.getValue().getCoordinates().getY(), 2));
                double len2 = Math.sqrt(Math.pow(o2.getValue().getCoordinates().getX(), 2) + Math.pow(o2.getValue().getCoordinates().getY(), 2));
                return Double.compare(len1, len2);
            }).forEach(entry -> server.getResponse().addMessage(entry.getKey() + " - " + entry.getValue()));
        }
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
