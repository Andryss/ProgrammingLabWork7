package Client;

import Commands.Command;
import MovieObjects.UserProfile;

import javax.jws.soap.SOAPBinding;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Request class contains all information client can send to the server in one class
 */
public class Request implements Serializable {
    private final RequestType requestType;
    private final UserProfile userProfile;
    private String commandName;
    private final Queue<Command> commandQueue = new LinkedList<>();

    public Request(RequestType requestType, UserProfile userProfile) {
        this.requestType = requestType;
        this.userProfile = userProfile;
    }

    public Request(RequestType requestType, UserProfile userProfile, String commandName) {
        this(requestType, userProfile);
        this.commandName = commandName;
    }

    public RequestType getRequestType() {
        return requestType;
    }
    public UserProfile getUserProfile() {
        return userProfile;
    }
    public String getCommandName() {
        return commandName;
    }
    public Queue<Command> getCommandQueue() {
        return commandQueue;
    }

    void addCommand(Command command) {
        commandQueue.add(command);
    }

    public enum RequestType {
        CHECK_CONNECTION,
        LOGIN_USER,
        LOGOUT_USER,
        REGISTER_USER,
        EXECUTE_COMMAND,
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestType=" + requestType +
                ", userProfile='" + userProfile + '\'' +
                ", commandName='" + commandName + '\'' +
                ", commandQueue=" + commandQueue +
                '}';
    }
}
