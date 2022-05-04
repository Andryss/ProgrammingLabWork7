package general.commands;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Exception, when command arguments are incorrect
 */
public class BadArgumentsException extends CommandException {
    /**
     * Map with examples of commands (if user always print "help me please!!!" instead of "help")
     */
    private static final Map<String, String> examples = new HashMap<>();

    /**
     * Constructor without reason
     * @param command name of command
     */
    public BadArgumentsException(String command) {
        this(command, null);
    }

    /**
     * Constructor with reason
     * @param command name of command
     * @param reason reason of exception
     * @see CommandException
     */
    public BadArgumentsException(String command, String reason) {
        super(command, reason);
    }

    static Map<String, String> getExamples() {
        return examples;
    }

    @Override
    public String getMessage() {
        if (getReason() != null) {
            return "ERROR: bad arguments command \"" + getCommand() + "\" (" + getReason() + ")";
        }
        String example = examples.get(getCommand());
        if (example != null) {
            return "ERROR: bad arguments command \"" + getCommand() + "\" (example: \"" + examples.get(getCommand()) + "\")";
        } else {
            return "ERROR: bad arguments command \"" + getCommand() + "\" (try another variations)";
        }
    }
}
