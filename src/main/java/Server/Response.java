package Server;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Response class contains all information the server can send to client in one class
 */
public class Response implements Serializable {
    private ResponseType responseType;
    private ArrayList<String> message;

    public Response(ResponseType responseType) {
        this.responseType = responseType;
    }

    void addMessage(String message) {
        if (this.message == null) {
            this.message = new ArrayList<>();
        } else {
            this.message.add("\n");
        }
        this.message.add(message.intern());
    }

    void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }
    public ResponseType getResponseType() {
        return responseType;
    }
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        message.forEach(builder::append);
        return builder.toString();
    }

    public enum ResponseType {
        WRONG_REQUEST_FORMAT,

        CONNECTION_SUCCESSFUL,

        LOGIN_SUCCESSFUL,
        LOGIN_FAILED,

        REGISTER_SUCCESSFUL,
        REGISTER_FAILED,

        ELEMENT_ABSENTED,
        PERMISSION_DENIED,
        CHECKING_SUCCESSFUL,
        CHECKING_FAILED,

        EXECUTION_SUCCESSFUL,
        EXECUTION_FAILED
    }
}
