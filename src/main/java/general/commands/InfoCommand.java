package general.commands;

import general.ClientINFO;
import general.element.Movie;
import general.ServerINFO;

import java.util.Hashtable;

/**
 * Command, which prints short info about the collection (type, init date, length etc.)
 * @see NameableCommand
 */
public class InfoCommand extends NameableCommand {

    @ParseCommand(name = "info", example = "info")
    public InfoCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        Hashtable<Integer, Movie> collection = server.getMovieCollection();
        server.getResponse()
                .addMessage("Collection type: " + collection.getClass().getName())
                .addMessage("Collection length: " + collection.size());
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
