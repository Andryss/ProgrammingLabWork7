package general;

/**
 * Interface of server response to the client
 */
public interface Response {

    Response addMessage(String line);

    ResponseType getResponseType();

    String getMessage();

    /**
     * Represent type of server response
     */
    enum ResponseType {
        WRONG_REQUEST_FORMAT,

        CONNECTION_SUCCESSFUL,

        LOGIN_SUCCESSFUL,
        LOGIN_FAILED,

        REGISTER_SUCCESSFUL,
        REGISTER_FAILED,

        ELEMENT_NOT_PRESENTED,
        PERMISSION_DENIED,
        CHECKING_SUCCESSFUL,
        CHECKING_FAILED,

        EXECUTION_SUCCESSFUL,
        EXECUTION_FAILED
    }
}