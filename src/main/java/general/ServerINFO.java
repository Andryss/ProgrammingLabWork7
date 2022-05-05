package general;

import general.element.Movie;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * <p>Represents all methods which cat be used by commands while executing</p>
 * <p>Follow "smth like Proxy" pattern</p>
 */
public interface ServerINFO {

    Movie getMovie(Integer key);

    Movie putMovie(Integer key, Movie readMovie) throws IllegalAccessException;

    Movie updateMovie(Integer key, Movie movie) throws IllegalAccessException;

    Movie removeMovie(Integer key) throws IllegalAccessException;

    void removeAllMovies() throws IllegalAccessException;

    Hashtable<Integer,Movie> getMovieCollection();

    ServerINFO validationClone();

    Response getResponse();

    LinkedList<String> getUserHistory();
}
