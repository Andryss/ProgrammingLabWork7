package general;

import client.file.FileExecutor;

import java.io.IOException;

/**
 * Represents all methods which cat be used by commands before sending it to the server
 */
public interface ClientINFO {

    Response sendToServer(Request request) throws IOException, ClassNotFoundException;

    Request createNewRequest(Request.RequestType requestType, Integer checkingIndex);

    FileExecutor getCaller();

    void println(String line);

    void print(String line);

    String nextLine();

}
