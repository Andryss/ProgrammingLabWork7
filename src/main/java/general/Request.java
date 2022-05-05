package general;

import general.commands.Command;
import general.commands.CommandException;
import general.element.UserProfile;

import java.io.Serializable;
import java.util.Queue;

/**
 * Interface of client request to the server
 */
public interface Request extends Serializable {

    RequestType getRequestType();

    UserProfile getUserProfile();

    Integer getCheckingIndex();

    String getCommandName();

    Queue<Command> getCommandQueue();

    void addCommand(Command command) throws CommandException;

    /**
     * Represent type of possible client requests
     */
    enum RequestType {
        CHECK_CONNECTION,

        LOGIN_USER,

        LOGOUT_USER,

        REGISTER_USER,

        CHECK_ELEMENT,

        EXECUTE_COMMAND
    }
}
