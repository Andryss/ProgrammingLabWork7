package general.commands;

import general.ClientINFO;
import general.ServerINFO;

import java.util.LinkedList;

/**
 * Command, which prints last 13 commands (without arguments)
 * @see NameableCommand
 */
public class HistoryCommand extends NameableCommand {

    @ParseCommand(name = "history", example = "history")
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
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
