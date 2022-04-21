package Server;

import MovieObjects.Movie;
import MovieObjects.UserProfile;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Class ServerINFO contains of all the information, that can be useful for commands (collection, name of file etc.)
 */
public class ServerINFO {
    protected final UserProfile userProfile;
    private final ResponseBuilder responseBuilder;


    public ServerINFO(UserProfile userProfile, ResponseBuilder responseBuilder) {
        this.userProfile = userProfile;
        this.responseBuilder = responseBuilder;
    }

    public Movie getMovie(Integer key) {
        return ServerCollectionManager.getMovie(key);
    }

    public Movie putMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.putMovie(key, movie, userProfile);
    }

    public Movie updateMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.updateMovie(key, movie, userProfile);
    }

    public Movie removeMovie(Integer key) throws SQLException, IllegalAccessException {
        return ServerCollectionManager.removeMovie(key, userProfile);
    }

    public Hashtable<Integer,Movie> getMovieCollection() {
        return ServerCollectionManager.getMovieCollection();
    }

    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

    public ServerINFO validationClone() {
        return new ServerINFOClone(userProfile, responseBuilder);
    }


    private static class ServerINFOClone extends ServerINFO {
        private final Hashtable<Integer, Movie> movieCollection = ServerCollectionManager.getMovieCollection();

        public ServerINFOClone(UserProfile userProfile, ResponseBuilder responseBuilder) {
            super(userProfile, responseBuilder);
        }

        @Override
        public Movie getMovie(Integer key) {
            return movieCollection.get(key);
        }
        @Override
        public Movie putMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
            if (movieCollection.containsKey(key)) {
                throw new SQLException("Movie already exists");
            } else if (movieCollection.size() >= 10) {
                throw new IllegalAccessException("Collection limit (10) exceeded");
            }
            movie.setOwner(userProfile.getName());
            return movieCollection.put(key, movie);
        }
        @Override
        public Movie updateMovie(Integer key, Movie movie) throws SQLException, IllegalAccessException {
            if (movieCollection.get(key) != null) {
                throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
            }
            if (movieCollection.get(key).getOwner().equals(userProfile.getName())) {
                throw new IllegalAccessException("User \"" + userProfile.getName() + "\" doesn't have permission to update movie with key \"" + key + "\"");
            }
            return putMovie(key, movie);
        }
        @Override
        public Movie removeMovie(Integer key) throws IllegalAccessException {
            if (movieCollection.get(key) != null) {
                throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
            }
            if (movieCollection.get(key).getOwner().equals(userProfile.getName())) {
                throw new IllegalAccessException("User \"" + userProfile.getName() + "\" doesn't have permission to remove movie with key \"" + key + "\"");
            }
            return movieCollection.remove(key);
        }
    }
}
