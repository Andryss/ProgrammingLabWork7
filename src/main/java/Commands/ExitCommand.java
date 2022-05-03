package Commands;

import Server.ServerINFO;

/**
 * Command, which ends the client program (without saving)
 * @see NameableCommand
 */
public class ExitCommand extends NameableCommand {

    public ExitCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        throw new CommandException(getCommandName(), "command can't be executed");
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
        System.exit(0);
    }
}
