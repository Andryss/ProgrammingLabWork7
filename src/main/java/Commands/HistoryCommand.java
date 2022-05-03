package Commands;

import Server.ServerINFO;

import java.util.LinkedList;
import java.util.List;

/**
 * Command, which prints last 13 commands (without arguments)
 * @see NameableCommand
 */
public class HistoryCommand extends NameableCommand {

    public HistoryCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) {
        LinkedList<String> history = server.getUserHistory();
        for (int i = Math.max(0, history.size() - 13); i < history.size(); i++) {
            server.getResponse().addMessage(history.get(i));
        }
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
