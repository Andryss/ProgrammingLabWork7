package general.element;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * One element of the collection
 * @see Person
 * @see Coordinates
 * @see FieldSetter
 * @see general.commands.ElementCommand
 */
public class Movie implements Comparable<Movie>, Serializable, Cloneable {
    private String owner;
    /**
     * Identical number (must be more than 0 and unique, generates automatically)
     */
    private final long id;
    /**
     * Name of movie (can't be null, String can't be empty)
     */
    private String name;
    /**
     * Coordinates (what?) of movie (can't be null)
     * @see Coordinates
     */
    private Coordinates coordinates;
    /**
     * Creation date of movie (can't be null, generates automatically)
     */
    private final java.time.ZonedDateTime creationDate;
    /**
     * Oscars count of movie (must be more than 0)
     */
    private long oscarsCount;
    /**
     * Length of movie (must be more than 0)
     */
    private int length;
    /**
     * Genre of movie (can be null)
     * @see MovieGenre
     */
    private MovieGenre genre;
    /**
     * Mpaa rating of movie (can't be null)
     * @see MpaaRating
     */
    private MpaaRating mpaaRating;
    /**
     * Screenwriter of movie
     * @see Person
     */
    private Person screenwriter;


    public Movie() {
        this(0, ZonedDateTime.now());
    }

    public Movie(long id, java.time.ZonedDateTime creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Parse name from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "name", example = "for example \"Terminator\"", index = 0)
    public void setName(String name) throws FieldException {
        if (name == null || name.equals("null")) {
            throw new FieldException(null, "Field can't be null, String can't be empty");
        }
        if (name.length() > 20) {
            throw new FieldException(name, "Name must have less than 20 characters");
        }
        this.name = name;
    }

    /**
     * Set coordinates
     */
    @FieldSetter(fieldName = "coordinates", example = "", index = -1)
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Parse oscars count from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "oscarsCount", example = "for example \"" + Long.MAX_VALUE + "\"", index = 3)
    public void setOscarsCount(String oscarsCountString) throws FieldException {
        try {
            long oscarsCount = Long.parseLong(oscarsCountString);
            if (oscarsCount <= 0) {
                throw new FieldException(String.valueOf(oscarsCount), "Value must be more than 0");
            }
            this.oscarsCount = oscarsCount;
        } catch (NumberFormatException | NullPointerException e) {
            throw new FieldException(oscarsCountString, "Value must be long");
        }
    }

    /**
     * Parse length from string and set
     * @throws FieldException if string is incorrect
     */
    @FieldSetter(fieldName = "length", example = "for example \"6420\"", index = 4)
    public void setLength(String lengthString) throws FieldException {
        try {
            int length = Integer.parseInt(lengthString);
            if (length <= 0) {
                throw new FieldException(String.valueOf(length), "Value must be more than 0");
            }
            this.length = length;
        } catch (NumberFormatException | NullPointerException e) {
            throw new FieldException(lengthString, "Value must be integer");
        }
    }

    /**
     * Parse genre from string and set
     * @throws FieldException if string is incorrect
     * @see MovieGenre
     */
    @FieldSetter(fieldName = "genre", example = "it must be one of: [ACTION, WESTERN, DRAMA, COMEDY, HORROR]", index = 5)
    public void setGenre(String genre) throws FieldException {
        if (genre == null) {
            this.genre = null;
            return;
        }
        try {
            this.genre = MovieGenre.valueOf(genre.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FieldException(genre, "Value must be one of: " + Arrays.toString(MovieGenre.values()));
        }
    }

    /**
     * Parse mpaa rating from string and set
     * @throws FieldException if string is incorrect
     * @see MpaaRating
     */
    @FieldSetter(fieldName = "mpaaRating", example = "it must be one of: [G, PG, PG_13, R, NC_17]", index = 6)
    public void setMpaaRating(String mpaaRating) throws FieldException {
        try {
            this.mpaaRating = MpaaRating.valueOf(mpaaRating.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new FieldException(mpaaRating, "Value must be one of: " + Arrays.toString(MpaaRating.values()));
        }
    }

    /**
     * Set screenwriter
     */
    @FieldSetter(fieldName = "screenwriter", example = "", index = -1)
    public void setScreenwriter(Person screenwriter) {
        this.screenwriter = screenwriter;
    }

    public String getOwner() {
        return owner;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public long getOscarsCount() {
        return oscarsCount;
    }

    public int getLength() {
        return length;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public Person getScreenwriter() {
        return screenwriter;
    }

    /**
     * Enum with possible mpaa rating labels
     */
    public enum MpaaRating {
        G,
        PG,
        PG_13,
        R,
        NC_17
    }

    /**
     * Enum with possible movie genres
     */
    public enum MovieGenre {
        ACTION,
        WESTERN,
        DRAMA,
        COMEDY,
        HORROR
    }

    /**
     * Comparing method (comparing movies by name)
     */
    @Override
    public int compareTo(Movie anotherMovie) {
        return name.compareTo(anotherMovie.getName());
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", oscarsCount=" + oscarsCount +
                ", length=" + length +
                ", genre=" + genre +
                ", mpaaRating=" + mpaaRating +
                ", screenwriter=" + screenwriter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id &&
                owner.equals(movie.owner) &&
                oscarsCount == movie.oscarsCount &&
                length == movie.length &&
                name.equals(movie.name) &&
                coordinates.equals(movie.coordinates) &&
                creationDate.equals(movie.creationDate) &&
                genre == movie.genre &&
                mpaaRating == movie.mpaaRating &&
                screenwriter.equals(movie.screenwriter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, name, coordinates, creationDate, oscarsCount, length, genre, mpaaRating, screenwriter);
    }

    @Override
    public Movie clone() {
        Movie clone = new Movie(id, creationDate);
        clone.owner = owner;
        clone.name = name;
        clone.coordinates = coordinates.clone();
        clone.oscarsCount = oscarsCount;
        clone.length = length;
        clone.genre = genre;
        clone.mpaaRating = mpaaRating;
        clone.screenwriter = screenwriter.clone();
        return clone;
    }
}
