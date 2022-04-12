package MovieObjects;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Class UserProfile contains of main info about user (login + password)
 */
public class UserProfile implements Serializable {
    private final String name;
    private final String encryptedPassword;
    private transient long id;

    public UserProfile(String name, String password) {
        this.name = name;
        this.encryptedPassword = encryptPassword(password);
    }

    public UserProfile(String name, String encryptedPassword, long id) {
        this.name = name;
        this.encryptedPassword = encryptedPassword;
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
                ", encryptedPassword='" + encryptedPassword.substring(0, 15) + "...'" +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(name, that.name) && Objects.equals(encryptedPassword, that.encryptedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, encryptedPassword);
    }
}
