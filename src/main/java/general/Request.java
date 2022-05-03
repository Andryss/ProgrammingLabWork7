package general;

import general.commands.Command;
import general.commands.CommandException;
import general.element.UserProfile;

import java.util.Queue;

public interface Request {

    RequestType getRequestType();

    UserProfile getUserProfile();

    Integer getCheckingIndex();

    String getCommandName();

    Queue<Command> getCommandQueue();

    void addCommand(Command command) throws CommandException;

    enum RequestType {
        CHECK_CONNECTION,

        LOGIN_USER,

        LOGOUT_USER,

        REGISTER_USER,

        CHECK_ELEMENT,

        EXECUTE_COMMAND
    }
}
