package server;

import general.ConnectorHelper;
import general.Request;
import general.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * ServerConnector receiving Request and starting Thread which executing this Request
 */
public class ServerConnector {
    private static DatagramChannel channel;
    private static int serverPort;
    private static Selector selector;
    private final ByteBuffer dataBuffer = ByteBuffer.allocate(15_000);

    ServerConnector() {}

    static void initialize() throws IOException {
        bindChannel();
    }

    static void setProperties(Properties properties) {
        try {
            serverPort = Integer.parseInt(properties.getProperty("serverPort", "52927"));
            if (serverPort < 0 || serverPort > 65535) {
                throw new NumberFormatException("property \"serverPort\" must be in range 1-65536");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"serverPort\": " + e.getMessage());
        }
    }

    private static void bindChannel() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        channel.bind(new InetSocketAddress(serverPort));
    }

    static void close() {
        try {
            channel.close();
            selector.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    public static void run() throws IOException {
        ServerController.info("------------------------------- Ready for receiving -------------------------------");
            while (true) {
                try {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (Iterator<SelectionKey> keyIterator = keys.iterator(); keyIterator.hasNext(); keyIterator.remove()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isValid() && key.isReadable()) {
                            ServerConnector connector = new ServerConnector();
                            SocketAddress client = channel.receive(connector.dataBuffer);
                            new Thread(() -> connector.receiveRequest(client), "ReceiveThreadJr").start();
                            ServerController.info("------------------------------- Ready for receiving -------------------------------");
                        }
                    }
                } catch (ClosedSelectorException e) {
                    break;
                }
            }
    }

    private void receiveRequest(SocketAddress client) {
        try {
            Request request = ConnectorHelper.objectFromBuffer(dataBuffer.array());

            ServerController.info("Received " + dataBuffer.position() + " bytes buffer with request " + request);
            dataBuffer.clear();

            ServerExecutor.getService().submit(() -> new ServerExecutor(client, request).executeRequest());
        } catch (IOException | ClassNotFoundException e) {
            ServerController.error(e.getMessage());
        }
    }

    void sendToClient(SocketAddress client, Response response) {
        try {
            dataBuffer.put(ConnectorHelper.objectToBuffer(response));
            dataBuffer.flip();
            ServerController.info("Sending " + dataBuffer.limit() + " bytes to client " + client.toString() + " starts");
            channel.send(dataBuffer, client);
            dataBuffer.clear();
        } catch (Throwable e) {
            ServerController.error(e.getMessage());
        }

        ServerController.info("Sending to client completed");
    }
}
