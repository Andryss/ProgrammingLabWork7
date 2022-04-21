package Server;

import Client.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * ServerConnector receiving Request and starting Thread which executing this Request
 */
public class ServerConnector {
    private static DatagramChannel channel;
    private static Selector selector;
    private static final ByteBuffer dataBuffer = ByteBuffer.allocate(60_000);

    private ServerConnector() {}

    static void initialize(int port) throws IOException {
        bindChannel(port);
    }

    private static void bindChannel(int port) throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        channel.bind(new InetSocketAddress(port));
    }

    static void close() {
        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            ServerController.error(e.getMessage(), e);
        }
    }

    public static void run() throws IOException, ClassNotFoundException {
        ServerController.info("------------------------------- Ready for receiving -------------------------------");
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (Iterator<SelectionKey> keyIterator = keys.iterator(); keyIterator.hasNext(); keyIterator.remove()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid() && key.isReadable()) {
                        receiveRequest();
                        ServerController.info("------------------------------- Ready for receiving -------------------------------");
                    }
                }
            }
        } catch (ClosedSelectorException e) {
            //ignore
        }
    }

    private static void receiveRequest() throws IOException, ClassNotFoundException {
        synchronized (dataBuffer) {
            SocketAddress client = channel.receive(dataBuffer);
            Request request = ConnectorHelper.objectFromBuffer(dataBuffer.array());

            ServerController.info("Received " + dataBuffer.position() + " bytes buffer with request " + request);
            dataBuffer.clear();

            ServerExecutor.getService().submit(() -> new ServerExecutor(client, request).executeRequest());
        }
    }

    static void sendToClient(SocketAddress client, Response response) {

        synchronized (dataBuffer) {
            try {
                dataBuffer.put(ConnectorHelper.objectToBuffer(response));
                dataBuffer.flip();
                ServerController.info("Sending " + dataBuffer.limit() + " bytes to client " + client.toString() + " starts");
                channel.send(dataBuffer, client);
                dataBuffer.clear();
            } catch (IOException e) {
                ServerController.error(e.getMessage(), e);
            }
        }

        ServerController.info("Sending to client completed");
    }
}
