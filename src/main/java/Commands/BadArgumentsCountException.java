package Commands;

/**
 * Exception, when count of arguments are incorrect (for example "insert 12 12 12 12 12" instead of "insert 12")
 */
public class BadArgumentsCountException extends BadArgumentsException {
    /**
     * Required count of arguments
     */
    private final int requiredCount;

    /**
     * Constructor with name and required count of arguments
     * @param command - name of command
     * @param requiredCount - required count of args
     */
    public BadArgumentsCountException(String command, int requiredCount) {
        super(command);
        this.requiredCount = requiredCount;
    }

    /**
     * Constructor with base count of arguments
     * @param command - name of command
     */
    public BadArgumentsCountException(String command) {
        this(command, 0);
    }

    @Override
    public String getMessage() {
        return "ERROR: amount of arguments for command \"" + getCommand() + "\" must equals " + requiredCount +
                " (example: \"" + getExamples().get(getCommand()) + "\")";
    }
}
