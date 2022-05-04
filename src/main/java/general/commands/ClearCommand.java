package general.commands;

import general.ClientINFO;
import general.ServerINFO;

/**
 * Command, which clears the collection
 * @see NameableCommand
 */
public class ClearCommand extends NameableCommand {

    @ParseCommand(name = "clear", example = "clear")
    public ClearCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        try {
            server.removeAllMovies();
        } catch (IllegalAccessException e) {
            //ignore
        }
        server.getResponse().addMessage("All your elements deleted");
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
