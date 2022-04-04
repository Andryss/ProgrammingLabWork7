package Client.File;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * <p>FileController implements (1) step in FileManager</p>
 * <p>The main idea of this class is just reading from the given file</p>
 */
public class FileController {

    private final Scanner reader;
    private final File file;

    public FileController(File file) throws FileNotFoundException {
        this.file = file;
        this.reader = new Scanner(file);
    }

    public String readLine() {
        return reader.nextLine();
    }

    public boolean hasNextLine() {
        return reader.hasNextLine();
    }

    public Scanner getReader() {
        return reader;
    }

    public String getFileName() {
        return file.getName();
    }
}
