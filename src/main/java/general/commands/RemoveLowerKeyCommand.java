package general.commands;

import general.ClientINFO;
import general.Request;
import general.ServerINFO;

/**
 * Command, which removes all elements whose key is less than given
 * @see NameableCommand
 */
public class RemoveLowerKeyCommand extends NameableCommand {
    private Integer key;

    @ParseCommand(name = "remove_lower_key", example = "remove_lower_key -13")
    public RemoveLowerKeyCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        server.getMovieCollection().keySet().stream()
                .filter(key -> key < this.key)
                .forEach(key -> {
                    try {
                        server.removeMovie(key);
                    } catch (IllegalAccessException e) {
                        //ignore
                    }
                });
        server.getResponse().addMessage("All your elements with key lower than \"" + key + "\" has been removed");
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            this.key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "integer");
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        RemoveLowerKeyCommand command = new RemoveLowerKeyCommand(getCommandName());
        command.key = key;
        request.addCommand(command);
    }
}
