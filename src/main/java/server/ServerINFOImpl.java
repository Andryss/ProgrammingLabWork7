package server;

import general.element.Movie;
import general.element.UserProfile;
import general.Response;
import general.ServerINFO;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * @see ServerINFO
 */
public class ServerINFOImpl implements ServerINFO {
    protected final UserProfile userProfile;
    private final Response response;


    public ServerINFOImpl(UserProfile userProfile, Response response) {
        this.userProfile = userProfile;
        this.response = response;
    }

    @Override
    public Movie getMovie(Integer key) {
        return ServerCollectionManager.getInstance().getMovie(key);
    }

    @Override
    public Movie putMovie(Integer key, Movie movie) throws IllegalAccessException {
        return ServerCollectionManager.getInstance().putMovie(key, movie, userProfile);
    }

    @Override
    public Movie updateMovie(Integer key, Movie movie) throws IllegalAccessException {
        return ServerCollectionManager.getInstance().updateMovie(key, movie, userProfile);
    }

    @Override
    public Movie removeMovie(Integer key) throws IllegalAccessException {
        return ServerCollectionManager.getInstance().removeMovie(key, userProfile);
    }

    @Override
    public void removeAllMovies() throws IllegalAccessException {
        ServerCollectionManager.getInstance().removeAllMovies(userProfile);
    }

    @Override
    public Hashtable<Integer,Movie> getMovieCollection() {
        return ServerCollectionManager.getInstance().getMovieCollection();
    }

    @Override
    public LinkedList<String> getUserHistory() {
        return ServerHistoryManager.getInstance().getUserHistory(userProfile);
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public ServerINFO validationClone() {
        return new ServerINFOClone(userProfile);
    }


    private static class ServerINFOClone extends ServerINFOImpl {
        private final Hashtable<Integer, Movie> movieCollection = ServerCollectionManager.getInstance().getMovieCollection();

        public ServerINFOClone(UserProfile userProfile) {
            super(userProfile, ResponseImpl.getEmptyResponse());
        }

        @Override
        public Movie getMovie(Integer key) {
            return movieCollection.get(key);
        }
        @Override
        public Movie putMovie(Integer key, Movie movie) throws IllegalAccessException {
            if (movieCollection.containsKey(key)) {
                throw new IllegalAccessException("Movie already exists");
            } else if (movieCollection.size() >= ServerCollectionManager.getInstance().getCollectionElementsLimit()) {
                throw new IllegalAccessException("Collection limit (" + ServerCollectionManager.getInstance().getCollectionElementsLimit() + ") exceeded");
            } else if (movieCollection.values().stream()
                    .filter(m -> m.getOwner().equals(userProfile.getName()))
                    .count() >= ServerCollectionManager.getInstance().getUserElementsLimit()) {
                throw new IllegalAccessException(userProfile.getName() + "'s elements count limit (" + ServerCollectionManager.getInstance().getUserElementsLimit() + ") exceeded");
            }
            movie.setOwner(userProfile.getName());
            return movieCollection.put(key, movie);
        }
        @Override
        public Movie updateMovie(Integer key, Movie movie) throws IllegalAccessException {
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
        @Override
        public void removeAllMovies() {
            movieCollection.entrySet().stream()
                    .filter(e -> e.getValue().getOwner().equals(userProfile.getName()))
                    .map(Map.Entry::getKey)
                    .forEach(movieCollection::remove);
        }
        @Override
        public Hashtable<Integer, Movie> getMovieCollection() {
            return movieCollection;
        }
    }
}
