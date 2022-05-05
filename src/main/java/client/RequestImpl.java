package client;

import general.commands.Command;
import general.commands.CommandException;
import general.element.UserProfile;
import general.Request;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @see Request
 */
public class RequestImpl implements Request {
    private RequestType requestType;
    private UserProfile userProfile;
    private Integer checkingIndex;
    private String commandName;
    private Queue<Command> commandQueue;

    public RequestImpl(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    public void setCheckingIndex(Integer checkingIndex) {
        this.checkingIndex = checkingIndex;
    }
    public void setCommandName(String commandName) {
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
        if (commandQueue == null) {
            commandQueue = new LinkedList<>();
        }
        if (commandQueue.size() >= 20) {
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
