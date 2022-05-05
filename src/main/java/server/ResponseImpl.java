package server;

import general.Response;

import java.util.ArrayList;

/**
 * @see Response
 */
public class ResponseImpl implements Response {
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
