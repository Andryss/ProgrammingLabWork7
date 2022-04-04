package Client;

import Commands.Command;
import Commands.NameableCommand;

/**
 * Global class, which build one request from client (especially for not sending one Request through all methods)
 */
public class RequestBuilder {
    private static String userName;
    private static String userPassword;
    private static Request request;

    private RequestBuilder() {}

    public static void createNewRequest(Request.RequestType requestType) {
        request = new Request(requestType);
        if (userName != null && userPassword != null) {
            request.setUserName(userName).setUserPassword(userPassword);
        }
    }

    public static void createNewRequest(Request.RequestType requestType, String commandName) {
        createNewRequest(requestType);
        request.setCommandName(commandName);
    }

    public static void createNewRequest(Request.RequestType requestType,
                                        String userName,
                                        String userPassword) {
        setUserNamePassword(userName, userPassword);
        createNewRequest(requestType);
    }

    public static void createNewRequest(Request.RequestType requestType,
                                        String userName,
                                        String userPassword,
                                        String commandName) {
        createNewRequest(requestType, userName, userPassword);
        request.setCommandName(commandName);
    }

    public static void setUserNamePassword(String userName, String userPassword) {
        RequestBuilder.userName = userName;
        RequestBuilder.userPassword = userPassword;
    }

    public static void add(Command command) {
        request.addCommand(command);
    }

    public static Request getRequest() {
        return request;
    }

}
