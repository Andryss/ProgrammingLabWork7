package general;

import general.element.Movie;

import java.util.Hashtable;
import java.util.LinkedList;

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
