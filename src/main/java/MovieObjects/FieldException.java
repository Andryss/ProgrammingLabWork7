package MovieObjects;

/**
 * Exception, when field is incorrect
 */
public class FieldException extends Exception {
    /**
     * Given value for field
     */
    private final String value;
    /**
     * Required value for field
     */
    private final String required;

    /**
     * Constructor with value and required value
     * @param value given value
     * @param required required value
     */
    public FieldException(String value, String required) {
        this.value = value;
        this.required = required;
    }

    @Override
    public String getMessage() {
        return "ERROR: Not supported value \"" + value + "\". " + required;
    }
}
