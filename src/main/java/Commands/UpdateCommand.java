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
 * Command, which updates an element with given id
 * @see NameableCommand
 */
public class UpdateCommand extends ElementCommand {

    public UpdateCommand(String commandName, Scanner reader) {
        this(commandName, reader, false);
    }

    public UpdateCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName, reader, readingFromFile);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.updateMovie(key, readMovie);
        } catch (IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("The movie has been updated");
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
        UpdateCommand command = new UpdateCommand(getCommandName(), reader);
        command.key = key; command.readMovie = readMovie;
        RequestBuilder.add(command);
    }
}
