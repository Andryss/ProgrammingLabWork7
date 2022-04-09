package Client;

import Commands.Command;
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

    public static void add(Command command) {
        request.addCommand(command);
    }

    public static Request getRequest() {
        return request;
    }

}
