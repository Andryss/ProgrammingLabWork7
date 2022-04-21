package Client;

import Commands.Command;
import Commands.CommandException;
import Commands.NameableCommand;
import MovieObjects.UserProfile;

/**
 * Global class, which build one request from client (especially for not sending one Request through all methods)
 */
public class RequestBuilder {
    private static UserProfile userProfile;
    private static Request request;

    private RequestBuilder() {}

    public static void createNewRequest(Request.RequestType requestType) {
        request = new Request(requestType, userProfile);
    }

    public static void createNewRequest(Request.RequestType requestType, String commandName) {
        request = new Request(requestType, userProfile, commandName);
    }

    public static void setUserProfile(UserProfile userProfile) {
        RequestBuilder.userProfile = userProfile;
    }

    public static void add(Command command) throws CommandException {
        if (request.getCommandQueue().size() > 20) {
            throw new CommandException("", "ERROR: Possible limit of command queue (20) exceeded");
        }
        request.addCommand(command);
    }

    public static Request getRequest() {
        return request;
    }

}
