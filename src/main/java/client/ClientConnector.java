package client;

import general.Request;
import general.ConnectorHelper;
import general.Response;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * <p>ClientConnector implements (3) step in ClientManager</p>
 * <p>There are some methods to send and receive datagrams</p>
 */
public class ClientConnector {
    private static DatagramSocket socket;
    private static InetAddress serverAddress;
    private static int serverPort;
    private static int socketSoTimeout;
    private static final ByteBuffer dataBuffer = ByteBuffer.allocate(30_000);

    private ClientConnector() {}

    static void initialize() throws IOException, ClassNotFoundException {
        ClientController.println("Connecting to server \"" + serverAddress + "\"");
        setConnection();
        checkConnection();
        ClientController.printlnGood("Connection to server was successful");
    }

    static void setProperties(Properties properties) throws UnknownHostException, NumberFormatException {
        serverAddress = InetAddress.getByName(properties.getProperty("serverAddress", "localhost"));
        try {
            serverPort = Integer.parseInt(properties.getProperty("serverPort", "52927"));
            if (serverPort < 0) {
                throw new NumberFormatException("Property \"serverPort\" can't be negative");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Property \"serverPort\" must be integer");
        }
        try {
            socketSoTimeout = Integer.parseInt(properties.getProperty("socketSoTimeout", "5000"));
            if (socketSoTimeout < 0) {
                throw new NumberFormatException("Property \"socketSoTimeout\" can't be negative");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Property \"socketSoTimeout\" must be integer");
        }
    }

    private static void setConnection() throws SocketException {
        socket = new DatagramSocket();
        socket.setSoTimeout(socketSoTimeout);
    }

    private static void checkConnection() throws IOException, ClassNotFoundException {
        try {
            Response response = sendToServer(
                    RequestBuilder.createNewRequest().setRequestType(Request.RequestType.CHECK_CONNECTION).build()
            );
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
