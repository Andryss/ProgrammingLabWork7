package Server;

import java.io.*;

/**
 * <p>Class helper for connector classes.</p>
 * <p>Consist of just "reading object from byte buffer" and "write object to byte buffer"</p>
 */
public abstract class ConnectorHelper {

    public static <T> byte[] objectToBuffer(T obj) throws IOException {
        ByteArrayOutputStream byteStream;
        try (ObjectOutputStream stream = new ObjectOutputStream(byteStream = new ByteArrayOutputStream())){
            stream.writeObject(obj);
            stream.flush();
            return byteStream.toByteArray();
        }
    }

    public static <T> T objectFromBuffer(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return  (T) stream.readObject();
        }
    }
}
