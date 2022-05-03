package client;

import general.commands.Command;
import general.commands.CommandException;
import general.element.UserProfile;
import general.Request;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Request class contains all information client can send to the server in one class
 */
public class RequestImpl implements Serializable, Request {
    private final RequestType requestType;
    private final UserProfile userProfile;
    private Integer checkingIndex;
    private String commandName;
    private final Queue<Command> commandQueue = new LinkedList<>();

    public RequestImpl(RequestType requestType, UserProfile userProfile) {
        this.requestType = requestType;
        this.userProfile = userProfile;
    }

    public RequestImpl(RequestType requestType, UserProfile userProfile, int checkingIndex) {
        this(requestType, userProfile);
        this.checkingIndex = checkingIndex;
    }

    public RequestImpl(RequestType requestType, UserProfile userProfile, String commandName) {
        this(requestType, userProfile);
        this.commandName = commandName;
    }

    @Override
    public RequestType getRequestType() {
        return requestType;
    }
    @Override
    public UserProfile getUserProfile() {
        return userProfile;
    }
    @Override
    public Integer getCheckingIndex() {
        return checkingIndex;
    }
    @Override
    public String getCommandName() {
        return commandName;
    }
    @Override
    public Queue<Command> getCommandQueue() {
        return commandQueue;
    }

    @Override
    public void addCommand(Command command) throws CommandException {
        if (commandQueue.size() > 20) {
            throw new CommandException("", "ERROR: Possible limit of command queue (20) exceeded");
        }
        commandQueue.add(command);
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestType=" + requestType +
                ", userProfile='" + userProfile + '\'' +
                ", checkingIndex='" + checkingIndex + '\'' +
                ", commandName='" + commandName + '\'' +
                ", commandQueue=" + commandQueue +
                '}';
    }
}
