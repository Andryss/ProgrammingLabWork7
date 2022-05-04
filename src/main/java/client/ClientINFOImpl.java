package client;

import client.file.FileController;
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

    @Override
    public void println(String line) {
        ClientController.println(line);
    }
    @Override
    public void print(String line) {
        ClientController.print(line);
    }
    @Override
    public String nextLine() {
        return ClientController.readLine();
    }


    public static class ClientINFOFromFileImpl extends ClientINFOImpl {
        private final FileController controller;

        public ClientINFOFromFileImpl(FileController controller) {
            this.controller = controller;
        }

        @Override
        public void println(String line) {
            // not today
        }
        @Override
        public void print(String line) {
            // not today
        }
        @Override
        public String nextLine() {
            return controller.readLine();
        }
    }
}
