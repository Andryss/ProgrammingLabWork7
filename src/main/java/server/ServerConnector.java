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
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerConnector receiving Request and starting Thread which executing this Request
 */
public class ServerConnector {
    private static final ServerConnector instance = new ServerConnector(); // Follow "Singleton" pattern
    private DatagramChannel channel;
    private int serverPort;
    private Selector selector;

    private ServerConnector() {}

    static ServerConnector getInstance() {
        return instance;
    }

    void initialize() throws IOException {
        bindChannel();
        ServerByteBufferManager.getInstance().initialize();
    }

    void setProperties(Properties properties) {
        try {
            serverPort = Integer.parseInt(properties.getProperty("serverPort", "52927"));
            if (serverPort < 0 || serverPort > 65535) {
                throw new NumberFormatException("property \"serverPort\" must be in range 1-65536");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"serverPort\": " + e.getMessage());
        }
        ServerByteBufferManager.getInstance().setProperties(properties);
    }

    private void bindChannel() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        channel.bind(new InetSocketAddress(serverPort));
    }

    void close() {
        try {
            channel.close();
            selector.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    public void run() throws IOException {
        ServerController.getInstance().info("------------------------------- Ready for receiving -------------------------------");
            while (true) {
                try {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (Iterator<SelectionKey> keyIterator = keys.iterator(); keyIterator.hasNext(); keyIterator.remove()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isValid() && key.isReadable()) {
                            ServerConnectorWorker worker = new ServerConnectorWorker();
                            SocketAddress client = channel.receive(worker.dataBuffer);
                            new Thread(() -> worker.receiveRequest(client), "ReceiveThreadJr").start();
                            ServerController.getInstance().info("------------------------------- Ready for receiving -------------------------------");
                        }
                    }
                } catch (ClosedSelectorException e) {
                    break;
                }
            }
    }

    void sendToClient(SocketAddress client, Response response) {
        new ServerConnectorWorker().sendToClient(client, response);
    }


    /**
     * ServerConnectorWorker implements "receiving request from client" and "sending response to client" methods.
     * Use ServerByteBufferManager to get available buffer. Can be created by ServerConnector only
     */
    private class ServerConnectorWorker {
        final ByteBuffer dataBuffer = ServerByteBufferManager.getInstance().getAvailableBuffer(); // Follow "Singleton" pattern

        ServerConnectorWorker() {}

        void receiveRequest(SocketAddress client) {
            try {
                if (dataBuffer == null) {
                    ServerController.getInstance().error(Thread.currentThread().getName() + " waiting for available buffer (while receiving) doesn't complete");
                } else {
                    Request request = ConnectorHelper.objectFromBuffer(dataBuffer.array());

                    ServerController.getInstance().info("Received " + dataBuffer.position() + " bytes buffer with request " + request);

                    ServerExecutor.getService().submit(() -> new ServerExecutor(client, request).executeRequest());
                }
            } catch (Throwable e) {
                ServerController.getInstance().error(e.getMessage());
            } finally {
                ServerByteBufferManager.getInstance().returnBuffer(dataBuffer);
            }
        }

        void sendToClient(SocketAddress client, Response response) {
            try {
                if (dataBuffer == null) {
                    ServerController.getInstance().error(Thread.currentThread().getName() + " waiting for available buffer (while sending) doesn't complete");
                } else {
                    dataBuffer.put(ConnectorHelper.objectToBuffer(response));
                    dataBuffer.flip();
                    ServerController.getInstance().info("Sending " + dataBuffer.limit() + " bytes to client " + client.toString() + " starts");
                    channel.send(dataBuffer, client);

                    ServerController.getInstance().info("Sending to client completed");
                }
            } catch (Throwable e) {
                ServerController.getInstance().error(e.getMessage());
            } finally {
                ServerByteBufferManager.getInstance().returnBuffer(dataBuffer);
            }
        }
    }


    /**
     * ServerByteBufferManager implements ByteBuffer pool, which manage available buffers for ServerConnector
     * (especially for not to create new buffer for each ServerConnector)
     */
    private static class ServerByteBufferManager {
        private static final ServerByteBufferManager instance = new ServerByteBufferManager(); // Follow "Singleton" pattern
        private int byteBufferPoolSize;
        private ArrayList<ByteBuffer> buffers; // Follow "Object pool" pattern
        private LinkedList<Integer> availableBuffers;
        private final ReentrantLock getBufferLock = new ReentrantLock();
        private final Condition hasAvailableBuffers = getBufferLock.newCondition();

        private ServerByteBufferManager() {}

        static ServerByteBufferManager getInstance() {
            return instance;
        }

        void initialize() {
            buffers = new ArrayList<>(byteBufferPoolSize);
            availableBuffers = new LinkedList<>();
            for (int i = 0; i < byteBufferPoolSize; i++) {
                buffers.add(i, ByteBuffer.allocate(15_000));
                availableBuffers.add(i);
            }
        }

        void setProperties(Properties properties) {
            try {
                byteBufferPoolSize = Integer.parseInt(properties.getProperty("byteBufferPoolSize", "5"));
                if (byteBufferPoolSize < 1) {
                    throw new NumberFormatException("property \"byteBufferPoolSize\" must be positive");
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Can't parse property \"byteBufferPoolSize\": " + e.getMessage());
            }
        }

        ByteBuffer getAvailableBuffer() {
            getBufferLock.lock();
            try {
                if (availableBuffers.isEmpty()) {
                    hasAvailableBuffers.await();
                }
                return buffers.get(availableBuffers.removeFirst());
            } catch (InterruptedException e) {
                return null;
            } finally {
                getBufferLock.unlock();
            }
        }

        void returnBuffer(ByteBuffer buffer) {
            getBufferLock.lock();
            try {
                if (buffers.contains(buffer)) {
                    buffer.clear();
                    availableBuffers.addFirst(buffers.indexOf(buffer));
                    hasAvailableBuffers.signal();
                }
            } finally {
                getBufferLock.unlock();
            }
        }

    }
}
