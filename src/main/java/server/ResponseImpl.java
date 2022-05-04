package server;

import general.Response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Response class contains all information the server can send to client in one class
 */
public class ResponseImpl implements Serializable, Response {
    private ResponseType responseType;
    private ArrayList<String> message;
    private static final Response emptyResponse = ResponseBuilder.createNewResponse()
            .setResponseType(ResponseType.CONNECTION_SUCCESSFUL)
            .addMessage("*empty response*")
            .build();

    public ResponseImpl() {}

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    @Override
    public Response addMessage(String message) {
        if (this.message == null) {
            this.message = new ArrayList<>();
        } else {
            this.message.add("\n");
        }
        this.message.add(message.intern());
        return this;
    }

    @Override
    public ResponseType getResponseType() {
        return responseType;
    }
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        message.forEach(builder::append);
        return builder.toString();
    }

    public static Response getEmptyResponse() {
        return emptyResponse;
    }

    @Override
    public String toString() {
        return "Response{" +
                "responseType=" + responseType +
                ", message=" + message +
                '}';
    }
}
