package Commands;

import Server.ServerINFO;

/**
 * Command, which clears the collection
 * @see NameableCommand
 */
public class ClearCommand extends NameableCommand {

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
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
