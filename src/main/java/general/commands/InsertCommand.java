package general.commands;

import general.Request;
import general.Response;
import general.ServerINFO;

/**
 * Command, which adds new element with given key
 * @see NameableCommand
 */
public class InsertCommand extends ElementCommand {

    @ParseCommand(name = "insert", example = "insert 5555")
    public InsertCommand(String commandName) {
        super(commandName);
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
        if (response.getResponseType() == Response.ResponseType.CHECKING_FAILED ||
                response.getResponseType() == Response.ResponseType.USER_LIMIT_EXCEEDED) {
            throw new BadArgumentsException(getCommandName(), response.getMessage());
        } else if (response.getResponseType() != Response.ResponseType.ELEMENT_NOT_PRESENTED) {
            throw new BadArgumentsException(getCommandName(), "Movie with key \"" + key + "\" already exists");
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        InsertCommand command = new InsertCommand(getCommandName());
        command.key = key; command.readMovie = readMovie;
        request.addCommand(command);
    }
}
