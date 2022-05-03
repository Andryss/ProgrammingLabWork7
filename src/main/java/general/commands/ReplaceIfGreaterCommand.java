package general.commands;

import general.Request;
import general.Response;
import general.ServerINFO;

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
    public void execute(ServerINFO server) throws CommandException {
        if (readMovie.compareTo(server.getMovieCollection().get(key)) > 0) {
            try {
                server.putMovie(key, readMovie);
            } catch (IllegalAccessException e) {
                throw new CommandException(getCommandName(), e.getMessage());
            }
            server.getResponse().addMessage("Element greater than the old one has been inserted");
        } else {
            server.getResponse().addMessage("Nothing was happened");
        }
    }

    @Override
    protected void checkElement(Response response) throws BadArgumentsException {
        if (response.getResponseType() != Response.ResponseType.CHECKING_SUCCESSFUL) {
            throw new BadArgumentsException(getCommandName(), response.getMessage());
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        ReplaceIfGreaterCommand command = new ReplaceIfGreaterCommand(getCommandName(), reader);
        command.key = key; command.readMovie = readMovie;
        request.addCommand(command);
    }
}
