package Server;

import MovieObjects.Movie;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Class ServerINFO contains of all the information, that can be useful for commands (collection, name of file etc.)
 */
public class ServerINFO {
    protected final String userName;
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
        public Movie putMovie(Integer key, Movie movie) throws SQLException {
            if (movieCollection.containsKey(key)) {
                throw new SQLException("Movie already exists");
            }
            movie.setOwner(userName);
            return movieCollection.put(key, movie);
        }
        @Override
        public Movie updateMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
            if (movieCollection.get(key) != null) {
                throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
            }
            if (movieCollection.get(key).getOwner().equals(userName)) {
                throw new IllegalAccessException("User \"" + userName + "\" doesn't have permission to update movie with key \"" + key + "\"");
            }
            return putMovie(key, movie);
        }
        @Override
        public Movie removeMovie(Integer key) throws IllegalAccessException {
            if (movieCollection.get(key) != null) {
                throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
            }
            if (movieCollection.get(key).getOwner().equals(userName)) {
                throw new IllegalAccessException("User \"" + userName + "\" doesn't have permission to remove movie with key \"" + key + "\"");
            }
            return movieCollection.remove(key);
        }
    }
}
