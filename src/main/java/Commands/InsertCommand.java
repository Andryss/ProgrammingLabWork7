package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Command, which adds new element with given key
 * @see NameableCommand
 */
public class InsertCommand extends ElementCommand {

    public InsertCommand(String commandName, Scanner reader) {
        this(commandName, reader, false);
    }

    public InsertCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName, reader, readingFromFile);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.putMovie(key, readMovie);
        } catch (SQLException | IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("*put new element in the collection*");
        }
        return false;
    }
}
