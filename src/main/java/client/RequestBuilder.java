package client;

import general.element.UserProfile;
import general.Request;

/**
 * Global class, which build requests from client
 */
public class RequestBuilder {
    private static UserProfile userProfile;

    private RequestBuilder() {}

    public static Request createNewRequest(RequestImpl.RequestType requestType) {
        return new RequestImpl(requestType, userProfile);
    }

    public static Request createNewRequest(RequestImpl.RequestType requestType, Integer checkingIndex) {
        return new RequestImpl(requestType, userProfile, checkingIndex);
    }

    public static Request createNewRequest(RequestImpl.RequestType requestType, String commandName) {
        return new RequestImpl(requestType, userProfile, commandName);
    }

    public static void setUserProfile(UserProfile userProfile) {
        RequestBuilder.userProfile = userProfile;
    }

    public static UserProfile getUserProfile() {
        return userProfile;
    }

}
