package Commands;

import Client.ClientConnector;
import Client.Request;
import Client.RequestBuilder;
import Server.Response;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
            } catch (IllegalAccessException e) {
                throw new CommandException(getCommandName(), e.getMessage());
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

    @Override
    protected void checkElement(Response response) throws BadArgumentsException {
        if (response.getResponseType() != Response.ResponseType.CHECKING_SUCCESSFUL) {
            throw new BadArgumentsException(getCommandName(), response.getMessage());
        }
    }

    @Override
    public void buildRequest() throws CommandException {
        ReplaceIfGreaterCommand command = new ReplaceIfGreaterCommand(getCommandName(), reader);
        command.key = key; command.readMovie = readMovie;
        RequestBuilder.add(command);
    }
}
