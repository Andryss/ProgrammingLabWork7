package Commands;

/**
 * Class of all command exceptions with name of command and reason
 */
public class CommandException extends Exception {
    /**
     * Name of command, when the exception is
     */
    private final String command;
    /**
     * Reason of exception
     */
    private final String reason;

    /**
     * Constructor with only name
     * @param command name of command
     */
    public CommandException(String command) {
        this(command, null);
    }

    /**
     * Constructor with name and reason
     * @param command name of command
     * @param reason string with reason of exception
     */
    public CommandException(String command, String reason) {
        this.command = command;
        this.reason = reason;
    }

    public String getCommand() {
        return command;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        if (reason != null) {
            return "ERROR: problem with command \"" + command + "\" (" + reason + ")";
        }
        return "ERROR: some problems with command \"" + command + "\"";
    }
}
