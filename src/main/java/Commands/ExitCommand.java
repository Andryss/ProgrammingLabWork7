package Commands;

import Server.ServerExecutor;
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
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) {
        return false;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
        System.exit(0);
    }
}
