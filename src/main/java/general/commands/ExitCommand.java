package general.commands;

import general.ClientINFO;
import general.ServerINFO;

/**
 * Command, which ends the client program (without saving)
 * @see NameableCommand
 */
public class ExitCommand extends NameableCommand {

    @ParseCommand(name = "exit", example = "exit")
    public ExitCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        throw new CommandException(getCommandName(), "command can't be executed");
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
        System.exit(0);
    }
}
