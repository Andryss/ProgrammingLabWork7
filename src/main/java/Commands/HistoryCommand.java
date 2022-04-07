package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

import java.util.List;

/**
 * Command, which prints last 13 commands (without arguments)
 * @see NameableCommand
 */
public class HistoryCommand extends NameableCommand {

    private final List<String> history;

    public HistoryCommand(String commandName, List<String> history) {
        super(commandName);
        this.history = history;
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) {
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        for (int i = Math.max(0, history.size() - 13); i < history.size(); i++) {
            server.getResponseBuilder().add(history.get(i));
        }
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
