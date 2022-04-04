package Client;

import Commands.Command;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Request class contains all information client can send to the server in one class
 */
public class Request implements Serializable {
    private final RequestType requestType;
    private String userName;
    private String userPassword;
    private String commandName;
    private final Queue<Command> commandQueue = new LinkedList<>();

    public Request(RequestType requestType) {
        this.requestType = requestType;
    }

    Request setUserName(String userName) {
        this.userName = userName; return this;
    }
    Request setUserPassword(String userPassword) {
        this.userPassword = userPassword; return this;
    }
    Request setCommandName(String commandName) {
        this.commandName = commandName; return this;
    }

    public RequestType getRequestType() {
        return requestType;
    }
    public String getUserName() {
        return userName;
    }
    public String getUserPassword() {
        return userPassword;
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
        REGISTER_USER,
        EXECUTE_COMMAND
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestType=" + requestType +
                ", userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", commandName='" + commandName + '\'' +
                ", commandQueue=" + commandQueue +
                '}';
    }
}
