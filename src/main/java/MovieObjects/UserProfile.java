package MovieObjects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserProfile {
    private final String name;
    private final String encryptedPassword;
    private long id;

    public UserProfile(String name, String password) {
        this.name = name;
        this.encryptedPassword = encryptPassword(password);
    }

    public UserProfile(String name, String password, long id) {
        this(name, password);
        this.id = id;
    }

    private static String encryptPassword(String userPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-384");
            byte[] bytes = messageDigest.digest(userPassword.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte i : bytes) {
                stringBuilder.append(Integer.toString((i & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException ignore) {
            throw new RuntimeException();
        }
    }

    public String getName() {
        return name;
    }
    public String getPassword() {
        return encryptedPassword;
    }
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "name='" + name + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", id=" + id +
                '}';
    }
}
