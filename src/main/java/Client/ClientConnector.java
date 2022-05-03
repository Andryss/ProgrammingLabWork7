package Client;

import Server.ConnectorHelper;
import Server.Response;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * <p>ClientConnector implements (3) step in ClientManager</p>
 * <p>There are some methods to send and receive datagrams</p>
 */
public class ClientConnector {
    private static DatagramSocket socket;
    private static InetAddress serverAddress;
    private static int serverPort;
    private static final ByteBuffer dataBuffer = ByteBuffer.allocate(30_000);

    private ClientConnector() {}

    static void initialize(InetAddress serverAddress, int port) throws IOException, ClassNotFoundException {
        setConnection(serverAddress, port);
        checkConnection();
    }

    private static void setConnection(InetAddress serverAddress, int port) throws SocketException {
        socket = new DatagramSocket();
        socket.setSoTimeout(5_000);
        ClientConnector.serverAddress = serverAddress;
        serverPort = port;
    }

    private static void checkConnection() throws IOException, ClassNotFoundException {
        try {
            Response response = sendToServer(RequestBuilder.createNewRequest(Request.RequestType.CHECK_CONNECTION));
            if (response.getResponseType() != Response.ResponseType.CONNECTION_SUCCESSFUL) {
                throw new IOException("Server has wrong logic: expected \"" +
                        Response.ResponseType.CONNECTION_SUCCESSFUL +
                        "\", but not \"" +
                        response.getResponseType() +
                        "\"");
            }
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("Server is not responding, try later or choose another server :(");
        }
    }

    public static Response sendToServer(Request request) throws IOException, ClassNotFoundException {
        try {
            sendRequest(request);
        } catch (IOException e) {
            throw new IOException("Can't send request to server: " + e.getMessage(), e);
        }
        try {
            return acceptResponse();
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("Can't receive response from server: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't find Response class in response from server: " + e.getMessage(), e);
        }
    }

    static void sendRequest(Request request) throws IOException {
        sendPacket(ConnectorHelper.objectToBuffer(request));
    }

    private static void sendPacket(byte[] buf) throws IOException {
        DatagramPacket packetWithData = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
        socket.send(packetWithData);
    }

    private static Response acceptResponse() throws IOException, ClassNotFoundException {
        receivePacket(dataBuffer.array());
        Response response = ConnectorHelper.objectFromBuffer(dataBuffer.array());
        dataBuffer.clear();
        return response;
    }

    private static void receivePacket(byte[] buf) throws IOException {
        DatagramPacket packetWithData = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packetWithData);
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("Server is not responding, try later or choose another server :(");
        }
    }
}
