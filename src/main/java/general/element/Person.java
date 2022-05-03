package general.element;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Part of the Movie class
 * @see Movie
 */
public class Person implements Serializable, Cloneable {
    /**
     * Name of screenwriter (can't be null, String can't be empty)
     */
    private String name;
    /**
     * Birthday of screenwriter (can be null, must have "DD.MM.YYYY" format)
     */
    private Date birthday;
    /**
     * Hair color of screenwriter (can be null)
     * @see Color
     */
    private Color hairColor;

    public Person() {}

    /**
     * Parse name from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "name", example = "for example \"James Francis Cameron\"", index = 7)
    public void setName(String name) throws FieldException {
        if (name == null) {
            throw new FieldException(null, "Field can't be null, String can't be empty");
        }
        if (name.length() > 20) {
            throw new FieldException(name, "Name must have less than 20 characters");
        }
        this.name = name;
    }

    /**
     * Parse birthday (in format "DD.MM.YYYY") from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "birthday", example = "for example \"16.08.1954\"", index = 8)
    public void setBirthday(String birthday) throws FieldException {
        if (birthday == null) {
            this.birthday = null;
            return;
        }
        try {
            LocalDate date = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            if (date.getYear() < 1000) {
                throw new FieldException(birthday, "Year must be at least 1000 (vampire-screenwriters is not supported)");
            }
            this.birthday = new Date(date.getYear(),date.getMonthValue(),date.getDayOfMonth());
        } catch (DateTimeParseException e) {
            throw new FieldException(birthday, "Field must have \"DD.MM.YYYY\" format");
        }
    }

    /**
     * Parse hair color from string and set
     * @throws FieldException if string is incorrect
     * @see Color
     */
    @FieldSetter(fieldName = "hairColor", example = "it must be one of: [RED, BLACK, BLUE, WHITE, BROWN]", index = 9)
    public void setHairColor(String hairColor) throws FieldException {
        if (hairColor == null) {
            this.birthday = null;
            return;
        }
        try {
            this.hairColor = Color.valueOf(hairColor.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FieldException(hairColor, "Value must be one of: " + Arrays.toString(Color.values()));
        }
    }

    public String getName() {
        return name;
    }

    /**
     * @return Date object of birthday
     * @see Date
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * @return date in "DD.MM.YYYY" format using Date object
     * @see Date
     */
    public String getBirthdayString() {
        if (birthday == null) {
            return "null";
        }
        int day = birthday.getDate();
        int month = (birthday.getMonth() == Calendar.JANUARY ? 12 : birthday.getMonth());
        int year = birthday.getYear();
        return "" + day / 10 + day % 10 + "." +
                month / 10 + month % 10 + "." +
                year / 1000 + year / 100 % 10 + year / 10 % 10 + year % 10;
    }

    public Color getHairColor() {
        return hairColor;
    }

    /**
     * Enum with possible hair colors
     */
    public enum Color {
        RED,
        BLACK,
        BLUE,
        WHITE,
        BROWN
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthday=" + getBirthdayString() +
                ", hairColor=" + hairColor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return name.equals(person.name) &&
                getBirthdayString().equals(person.getBirthdayString()) &&
                hairColor == person.hairColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthday, hairColor);
    }

    @Override
    protected Person clone() {
        Person clone = new Person();
        clone.name = name;
        clone.birthday = (Date) birthday.clone();
        clone.hairColor = hairColor;
        return clone;
    }
}
