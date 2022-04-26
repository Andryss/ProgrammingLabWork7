package Server;

/**
 * Global class, which build response to client
 */
public class ResponseBuilder {
    private final Response response;

    private ResponseBuilder(Response.ResponseType responseType) {
        response = new Response(responseType);
    }

    static ResponseBuilder createNewResponse(Response.ResponseType responseType) {
        return new ResponseBuilder(responseType);
    }

    static ResponseBuilder createNewResponse(Response.ResponseType responseType, String line) {
        return createNewResponse(responseType).add(line);
    }

    public ResponseBuilder add(String line) {
        response.addMessage(line);
        return this;
    }

    void setResponseType(Response.ResponseType responseType) {
        response.setResponseType(responseType);
    }

    Response getResponse() {
        return response;
    }
}
