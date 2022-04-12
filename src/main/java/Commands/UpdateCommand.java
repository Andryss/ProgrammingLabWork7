package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Command, which updates an element with given id
 * @see NameableCommand
 */
public class UpdateCommand extends ElementCommand {

    public UpdateCommand(String commandName, Scanner reader) {
        this(commandName, reader, false);
    }

    public UpdateCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName, reader, readingFromFile);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        try {
            server.updateMovie(key, readMovie);
        } catch (SQLException | IllegalAccessException e) {
            throw new CommandException(getCommandName(), e.getMessage());
        }
        if (state == ServerExecutor.ExecuteState.EXECUTE) {
            server.getResponseBuilder().add("The movie has been updated");
        }
        return true;
    }
}
