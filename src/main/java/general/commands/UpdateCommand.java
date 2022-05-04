package general.commands;

import general.Request;
import general.Response;
import general.ServerINFO;

/**
 * Command, which updates an element with given id
 * @see NameableCommand
 */
public class UpdateCommand extends ElementCommand {

    @ParseCommand(name = "update", example = "update 30")
    public UpdateCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        try {
            server.updateMovie(key, readMovie);
        } catch (IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        server.getResponse().addMessage("The movie has been updated");
    }

    @Override
    protected void checkElement(Response response) throws BadArgumentsException {
        if (response.getResponseType() != Response.ResponseType.CHECKING_SUCCESSFUL) {
            throw new BadArgumentsException(getCommandName(), response.getMessage());
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        UpdateCommand command = new UpdateCommand(getCommandName());
        command.key = key; command.readMovie = readMovie;
        request.addCommand(command);
    }
}
