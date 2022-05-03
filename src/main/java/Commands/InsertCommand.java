package Commands;

import Client.Request;
import Server.Response;
import Server.ServerINFO;

import java.util.Scanner;

/**
 * Command, which adds new element with given key
 * @see NameableCommand
 */
public class InsertCommand extends ElementCommand {

    public InsertCommand(String commandName, Scanner reader) {
        this(commandName, reader, false);
    }

    public InsertCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName, reader, readingFromFile);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        try {
            server.putMovie(key, readMovie);
        } catch (IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        server.getResponse().addMessage("*put new element in the collection*");
    }

    @Override
    protected void checkElement(Response response) throws BadArgumentsException {
        if (response.getResponseType() == Response.ResponseType.CHECKING_FAILED) {
            throw new BadArgumentsException(getCommandName(), response.getMessage());
        } else if (response.getResponseType() != Response.ResponseType.ELEMENT_NOT_PRESENTED) {
            throw new BadArgumentsException(getCommandName(), "Movie with key \"" + key + "\" already exists");
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        InsertCommand command = new InsertCommand(getCommandName(), reader);
        command.key = key; command.readMovie = readMovie;
        request.addCommand(command);
    }
}
