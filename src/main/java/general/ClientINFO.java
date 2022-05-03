package general;

import java.io.IOException;

public interface ClientINFO {

    Response sendToServer(Request request) throws IOException, ClassNotFoundException;

    Request createNewRequest(Request.RequestType requestType, Integer checkingIndex);

}
