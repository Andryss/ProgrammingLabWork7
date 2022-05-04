package client.file;

import general.Request;
import general.commands.CommandException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * <p>FileManager consist of main file reading logic:</p>
 * <p>1) Read line from file</p>
 * <p>2) Try to parse command and build Request</p>
 * <p>3) If something went wrong - throw Exception</p>
 */
public class FileManager {

    private final FileController controller;
    private final FileExecutor executor;

    public FileManager(File file, FileExecutor caller, Request request) throws FileNotFoundException {
        this.controller = new FileController(file);
        this.executor = new FileExecutor(this.controller, caller, request);
    }

    public void buildRequest() throws CommandException {
        while (controller.hasNextLine()) {
            try {
                executor.parseCommand(controller.readLine());
            } catch (CommandException e) {
                throw new CommandException(e.getCommand(), "file \"" + controller.getFileName() + "\" "
                        + "command " + (executor.getCommandNumber() - 1) + " \"" + e.getCommand() + "\" - " + e.getMessage());
            }
        }
    }
}
