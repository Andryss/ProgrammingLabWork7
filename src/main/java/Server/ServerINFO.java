package Server;

import MovieObjects.Movie;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class ServerINFO contains of all the information, that can be useful for commands (collection, name of file etc.)
 */
public class ServerINFO {
    private final String userName;
    private final String userPassword;
    private final ResponseBuilder responseBuilder;


    public ServerINFO(String userName, String userPassword, ResponseBuilder responseBuilder) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.responseBuilder = responseBuilder;
    }

    public Movie getMovie(Integer key) {
        return ServerCollectionManager.getMovie(key);
    }

    public Movie putMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.putMovie(key, movie, userName, userPassword);
    }

    public Movie updateMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.updateMovie(key, movie, userName, userPassword);
    }

    public Movie removeMovie(Integer key) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.removeMovie(key, userName, userPassword);
    }

    public Hashtable<Integer,Movie> getMovieCollection() {
        return ServerCollectionManager.getMovieCollection();
    }

    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

    public ServerINFO validationClone() {
        return new ServerINFOClone(userName, userPassword, responseBuilder);
    }

    private static class ServerINFOClone extends ServerINFO {
        private final Hashtable<Integer, Movie> movieCollection = ServerCollectionManager.getMovieCollection();

        public ServerINFOClone(String userName, String userPassword, ResponseBuilder responseBuilder) {
            super(userName, userPassword, responseBuilder);
        }

        @Override
        public Movie getMovie(Integer key) {
            return movieCollection.get(key);
        }
        @Override
        public Movie putMovie(Integer key, Movie movie) {
            return movieCollection.put(key, movie);
        }
        @Override
        public Movie updateMovie(Integer key, Movie movie) {
            return putMovie(key, movie);
        }
        @Override
        public Movie removeMovie(Integer key) {
            return movieCollection.remove(key);
        }
        @Override
        public Hashtable<Integer, Movie> getMovieCollection() {
            return movieCollection;
        }
    }
}
