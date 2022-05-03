package general.element;

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

    public UserProfile(String name, String password) throws IllegalArgumentException {
        this.name = checkLogin(name);
        this.encryptedPassword = encryptPassword(checkPassword(password));
    }

    public UserProfile(String name, String encryptedPassword, long id) {
        this.name = name;
        this.encryptedPassword = encryptedPassword;
        this.id = id;
    }

    private static String checkLogin(String login) throws IllegalArgumentException {
        if (login.length() < 3) {
            throw new IllegalArgumentException("Login must have at least 3 characters");
        }
        if (login.length() > 20) {
            throw new IllegalArgumentException("Login must have less than 20 characters");
        }
        if (!login.chars().allMatch(Character::isAlphabetic)) {
            throw new IllegalArgumentException("Login must contains of only alphabetic characters");
        }
        return login;
    }
    private static String checkPassword(String password) throws IllegalArgumentException {
        if (password.length() < 3) {
            throw new IllegalArgumentException("Password must have at least 3 characters");
        }
        if (password.length() > 20) {
            throw new IllegalArgumentException("Login must have less than 20 characters");
        }
        return password;
    }

    private static String encryptPassword(String userPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-384");
            byte[] bytes = messageDigest.digest(("~;&a$2%" + userPassword).getBytes());
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
