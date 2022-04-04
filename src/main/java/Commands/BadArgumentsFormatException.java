package Commands;

/**
 * Exception, when format of arguments are incorrect (for example "insert NewELEMENT" instead of "insert 12")
 */
public class BadArgumentsFormatException extends BadArgumentsException {
    /**
     * Required format of arguments
     */
    private final String requiredFormat;

    /**
     * Constructor with required format of arguments
     * @param command name of command
     * @param requiredFormat required format of args
     */
    public BadArgumentsFormatException(String command, String requiredFormat) {
        super(command);
        this.requiredFormat = requiredFormat;
    }

    @Override
    public String getMessage() {
        return "ERROR: argument for command \"" + getCommand() + "\" - must be " + requiredFormat +
                " (example: \"" + getExamples().get(getCommand()) + "\")";
    }
}
