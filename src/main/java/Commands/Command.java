package Commands;

import Client.Request;
import Server.ServerINFO;

import java.io.Serializable;

/**
 * interface Command represents all required command methods
 */
public interface Command extends Serializable {
    /**
     * Makes command executing
     */
    void execute(ServerINFO server) throws CommandException;

    /**
     * Validate and set arguments for command
     * @param args String array with arguments
     * @throws BadArgumentsException if arguments are incorrect
     */
    void setArgs(String... args) throws BadArgumentsException;

    /**
     * Build Request depending on command type
     * @throws CommandException if something wrong with building Request
     */
    default void buildRequest(Request request) throws CommandException {
        request.addCommand(this);
    }
}
