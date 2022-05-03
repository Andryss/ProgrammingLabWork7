package general.element;

import java.io.Serializable;
import java.util.Objects;

/**
 * Part of Movie object (Movie with Coordinates? Why now?)
 * @see Movie
 */
public class Coordinates implements Serializable, Cloneable {
    /**
     * X coordinate
     */
    private float x;
    /**
     * Y coordinate (can't be null)
     */
    private Float y;

    public Coordinates() {}

    /**
     * Parse X from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "x", example = "for example \"3.1415926\"", index = 1)
    public void setX(String xString) throws FieldException {
        try {
            float x = Float.parseFloat(xString);
            if (!Float.isFinite(x)) {
                throw new NumberFormatException();
            }
            this.x = x;
        } catch (NumberFormatException | NullPointerException e) {
            throw new FieldException(xString, "Value must be float");
        }
    }

    /**
     * Parse Y from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "y", example = "for example \"1\" or \"-14.9\"", index = 2)
    public void setY(String yString) throws FieldException {
        try {
            float y = Float.parseFloat(yString);
            if (!Float.isFinite(y)) {
                throw new NumberFormatException();
            }
            this.y = y;
        } catch (NumberFormatException | NullPointerException e) {
            throw new FieldException(yString, "Value must be float");
        }
    }

    public float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Float.compare(that.x, x) == 0 &&
                y.compareTo(that.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    protected Coordinates clone() {
        Coordinates clone = new Coordinates();
        clone.x = x;
        clone.y = y;
        return clone;
    }
}
