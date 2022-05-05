package client;

import general.element.UserProfile;
import general.Request;

/**
 * <p>Global class, which build requests from client</p>
 * <p>Follow "Builder" pattern</p>
 */
public class RequestBuilder {
    private static UserProfile userProfile;
    private final RequestImpl request = new RequestImpl(userProfile);

    private RequestBuilder() {}

    public static RequestBuilder createNewRequest() {
        return new RequestBuilder();
    }

    public RequestBuilder setRequestType(Request.RequestType requestType) {
        request.setRequestType(requestType);
        return this;
    }

    public RequestBuilder setCheckingIndex(Integer checkingIndex) {
        request.setCheckingIndex(checkingIndex);
        return this;
    }

    public RequestBuilder setCommandName(String commandName) {
        request.setCommandName(commandName);
        return this;
    }

    public Request build() {
        return request;
    }

    public static void setUserProfile(UserProfile userProfile) {
        RequestBuilder.userProfile = userProfile;
    }

    public static UserProfile getUserProfile() {
        return userProfile;
    }

}
