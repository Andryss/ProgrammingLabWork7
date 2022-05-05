package server;

import general.Response;

/**
 * <p>Global class, which build response to client</p>
 * <p>Follow "Builder" pattern</p>
 */
public class ResponseBuilder {
    private final ResponseImpl response = new ResponseImpl();

    private ResponseBuilder() {}

    public static ResponseBuilder createNewResponse() {
        return new ResponseBuilder();
    }

    public ResponseBuilder setResponseType(Response.ResponseType responseType) {
        response.setResponseType(responseType);
        return this;
    }

    public ResponseBuilder addMessage(String line) {
        response.addMessage(line);
        return this;
    }

    public Response build() {
        return response;
    }
}
