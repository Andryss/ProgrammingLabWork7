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
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.putMovie(key, readMovie);
        } catch (IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("*put new element in the collection*");
        }
        return false;
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
    public void buildRequest() throws CommandException {
        InsertCommand command = new InsertCommand(getCommandName(), reader);
        command.key = key; command.readMovie = readMovie;
        RequestBuilder.add(command);
    }
}
