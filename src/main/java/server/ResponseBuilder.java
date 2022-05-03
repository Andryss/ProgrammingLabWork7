package server;

import general.Response;

/**
 * Global class, which build response to client
 */
public class ResponseBuilder {

    static Response createNewResponse(Response.ResponseType responseType) {
        return new ResponseImpl(responseType);
    }

    static Response createNewResponse(Response.ResponseType responseType, String line) {
        return createNewResponse(responseType).addMessage(line);
    }
}
