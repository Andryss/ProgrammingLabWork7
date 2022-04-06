package Server;

import java.io.Serializable;

/**
 * Response class contains all information the server can send to client in one class
 */
public class Response implements Serializable {
    private ResponseType responseType;
    private StringBuilder message;

    public Response(ResponseType responseType) {
        this.responseType = responseType;
    }

    void addMessage(String message) {
        if (this.message == null) {
            this.message = (new StringBuilder()).append(message);
        } else {
            this.message.append("\n").append(message);
        }
    }

    void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }
    public ResponseType getResponseType() {
        return responseType;
    }
    public String getMessage() {
        return message.toString();
    }

    public enum ResponseType {
        CONNECTION_SUCCESSFUL,
        LOGIN_SUCCESSFUL,
        LOGIN_FAILED,
        REGISTER_SUCCESSFUL,
        REGISTER_FAILED,
        EXECUTION_SUCCESSFUL,
        EXECUTION_FAILED
    }
}
