package Commands;

/**
 * Exception, when this command is unexpected
 * @see CommandException
 */
public class UndefinedCommandException extends CommandException {

    public UndefinedCommandException(String command) {
        super(command);
    }

    @Override
    public String getMessage() {
        return "ERROR: undefined command \"" + getCommand() + "\" (type \"help\" to see list of commands)";
    }
}
