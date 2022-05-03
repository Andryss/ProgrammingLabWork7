package client;

import general.ClientINFO;
import general.Request;
import general.Response;

import java.io.IOException;

public class ClientINFOImpl implements ClientINFO {

    @Override
    public Response sendToServer(Request request) throws IOException, ClassNotFoundException {
        return ClientConnector.sendToServer(request);
    }

    @Override
    public Request createNewRequest(Request.RequestType requestType, Integer checkingIndex) {
        return RequestBuilder.createNewRequest(requestType, checkingIndex);
    }
}
