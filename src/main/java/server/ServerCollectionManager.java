package server;

import general.element.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerCollectionManager is the main class which working with database and collections
 */
public class ServerCollectionManager {
    private static final ServerCollectionManager instance = new ServerCollectionManager(); // Follow "Singleton" pattern
    private Connection connection;
    private Hashtable<Integer, Movie> movieCollection;
    private int collectionElementsLimit;
    private int userElementsLimit;
    private Hashtable<String, UserProfile> userCollection;
    private final ReentrantLock readWriteLock = new ReentrantLock();

    private ServerCollectionManager() {}

    static ServerCollectionManager getInstance() {
        return instance;
    }

    private String dbHostName;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private final String usersTable = "users_335155";
    private final String movieTable = "movie_335155";

    void initialize() throws ClassNotFoundException, SQLException, FieldException {
        Class.forName("org.postgresql.Driver");
        try {
            connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s", dbHostName, dbName), dbUser, dbPassword);
        } catch (SQLException e) {
            throw new SQLException("Can't create connection to db: " + e.getMessage());
        }
        initializeStatements();
        createTables();
        loadCollectionsFromDB();
        printTables();
    }

    void setProperties(Properties properties) {
        dbHostName = properties.getProperty("dbHostName", "pg");
        dbName = properties.getProperty("dbName", "studs");
        dbUser = properties.getProperty("dbUser");
        if (dbUser == null) {
            throw new IllegalArgumentException("Property \"dbName\" doesn't set");
        }
        dbPassword = properties.getProperty("dbPassword");
        if (dbPassword == null) {
            throw new IllegalArgumentException("Property \"dbPassword\" doesn't set");
        }
        try {
            collectionElementsLimit = Integer.parseInt(properties.getProperty("collectionElementsLimit", "10"));
            if (collectionElementsLimit < 0) {
                throw new NumberFormatException("property \"collectionElementsLimit\" must be positive");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"collectionElementsLimit\": " + e.getMessage());
        }
        try {
            userElementsLimit = Integer.parseInt(properties.getProperty("userElementsLimit", "3"));
            if (userElementsLimit < 0) {
                throw new NumberFormatException("property \"userElementsLimit\" must be positive");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"userElementsLimit\": " + e.getMessage());
        }
    }

    private Statement statement;
    private PreparedStatement getUserStatement;
    private PreparedStatement insertUserStatement;
    private PreparedStatement removeUserStatement;
    private PreparedStatement insertMovieStatement;
    private PreparedStatement updateMovieStatement;
    private PreparedStatement removeMovieStatement;
    private PreparedStatement removeAllMoviesStatement;

    private void initializeStatements() throws SQLException {
        statement = connection.createStatement();
        getUserStatement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE user_login=?", usersTable));
        insertUserStatement = connection.prepareStatement(String.format("INSERT INTO %s (" +
                "user_login," +
                "user_password" +
                ") VALUES (?,?)", usersTable));
        removeUserStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE user_login=?", usersTable));
        insertMovieStatement = connection.prepareStatement(String.format("INSERT INTO %s (" +
                "user_id," +
                "movie_key," +
                "name," +
                "coordinates_x," +
                "coordinates_y," +
                "creation_date," +
                "oscars_count," +
                "length," +
                "genre," +
                "mpaa_rating," +
                "screenwriter_name," +
                "screenwriter_birthday," +
                "screenwriter_hair_color" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", movieTable));
        updateMovieStatement = connection.prepareStatement(String.format("UPDATE %s SET (" +
                "name," +
                "coordinates_x," +
                "coordinates_y," +
                "creation_date," +
                "oscars_count," +
                "length," +
                "genre," +
                "mpaa_rating," +
                "screenwriter_name," +
                "screenwriter_birthday," +
                "screenwriter_hair_color" +
                ")=(?,?,?,?,?,?,?,?,?,?,?) WHERE " +
                "movie_key=?", movieTable));
        removeMovieStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE movie_key=?", movieTable));
        removeAllMoviesStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE user_id=?", movieTable));
    }

    void createTables() throws SQLException {
        statement.execute(String.format("CREATE TABLE IF NOT EXISTS %s (\n" +
                "user_id BIGSERIAL,\n" +
                "user_login TEXT PRIMARY KEY,\n" +
                "user_password TEXT NOT NULL\n" +
                ")", usersTable));
        statement.execute(String.format("CREATE TABLE If NOT EXISTS %s (\n" +
                "movie_id bigserial,\n" +
                "user_id bigint NOT NULL,\n" +
                "movie_key INT PRIMARY KEY,\n" +
                "name TEXT NOT NULL,\n" +
                "coordinates_x REAL NOT NULL,\n" +
                "coordinates_y REAL NOT NULL,\n" +
                "creation_date TEXT NOT NULL,\n" +
                "oscars_count BIGINT CHECK (oscars_count > 0),\n" +
                "length INT CHECK (oscars_count > 0),\n" +
                "genre TEXT NULL,\n" +
                "mpaa_rating TEXT NOT NULL,\n" +
                "screenwriter_name TEXT NOT NULL,\n" +
                "screenwriter_birthday TEXT NULL,\n" +
                "screenwriter_hair_color TEXT NULL\n" +
                ")", movieTable));
    }

    void close() {
        try {
            connection.close();
        } catch (Throwable e) {
            // ignore
        }
    }

    void dropTables() {
        try {
            statement.execute(String.format("DROP TABLE %s", usersTable));
            statement.execute(String.format("DROP TABLE %s", movieTable));
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
        }
    }

    public long getUserID(String userName) {
        return userCollection.get(userName) == null ? -1 : userCollection.get(userName).getId();
    }

    public long getUserID(UserProfile user) {
        UserProfile userProfile = userCollection.get(user.getName());
        return (userProfile != null && userProfile.getPassword().equals(user.getPassword()) ? userProfile.getId() : -1);
    }

    public boolean isUserPresented(UserProfile userProfile) {
        return getUserID(userProfile) != -1;
    }

    public String getUserName(long id) {
        Optional<Map.Entry<String,UserProfile>> user = userCollection.entrySet().stream().filter(e -> e.getValue().getId() == id).findAny();
        return user.map(Map.Entry::getKey).orElse(null);
    }

    public long registerUser(UserProfile userProfile) {
        if (getUserID(userProfile) == -1) {
            readWriteLock.lock();
            try {
                insertUserStatement.setString(1, userProfile.getName());
                insertUserStatement.setString(2, userProfile.getPassword());
                insertUserStatement.execute();

                getUserStatement.setString(1, userProfile.getName());
                try (ResultSet resultSet = getUserStatement.executeQuery()) {
                    if (resultSet.next()) {
                        long userID = resultSet.getLong("user_id");
                        userCollection.put(userProfile.getName(), new UserProfile(userProfile.getName(), userProfile.getPassword(), userID));
                        return userID;
                    }
                }
            } catch (SQLException e) {
                ServerController.getInstance().error(e.getMessage());
            } finally {
                readWriteLock.unlock();
            }
        }
        return -1;
    }

    public UserProfile removeUser(UserProfile userProfile) {
        readWriteLock.lock();
        try {
            if (!userCollection.get(userProfile.getName()).getPassword().equals(userProfile.getPassword())) {
                return null;
            }
            removeUserStatement.setString(1, userProfile.getName());
            removeUserStatement.executeUpdate();
            return userCollection.remove(userProfile.getName());
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    void removeUser(String userName) {
        readWriteLock.lock();
        try {
            removeUserStatement.setString(1, userName);
            removeUserStatement.executeUpdate();
            userCollection.remove(userName);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
        } finally {
            readWriteLock.unlock();
        }
    }

    public Movie getMovie(Integer key) {
        // return movieCollection.get(key).clone();
        return movieCollection.get(key);
    }

    long countElements(String userName) {
        return getMovieCollection()
                .values().stream()
                .filter(m -> m.getOwner().equals(userName))
                .count();
    }

    public Movie putMovie(Integer key, Movie movie, UserProfile userProfile) throws IllegalAccessException {
        long userID = getUserID(userProfile);
        if (userID == -1) {
            throw new IllegalAccessException("User with name \"" + userProfile.getName() + "\" doesn't exist");
        } else if (movieCollection.get(key) != null) {
            throw new IllegalAccessException("Movie with key \"" + key + "\" already exists");
        } else if (movieCollection.size() >= collectionElementsLimit) {
            throw new IllegalAccessException("Collection limit (" + collectionElementsLimit + ") exceeded");
        } else {
            if (countElements(userProfile.getName()) >= userElementsLimit) {
                throw new IllegalAccessException(userProfile.getName() + "'s elements count limit (" + userElementsLimit + ") exceeded");
            }
        }
        readWriteLock.lock();
        try {
            insertMovieStatement.setLong(1, userID);
            insertMovieStatement.setInt(2, key);
            insertMovieStatement.setString(3, movie.getName());
            insertMovieStatement.setFloat(4, movie.getCoordinates().getX());
            insertMovieStatement.setFloat(5, movie.getCoordinates().getY());
            insertMovieStatement.setString(6, movie.getCreationDate().toString());
            insertMovieStatement.setLong(7, movie.getOscarsCount());
            insertMovieStatement.setInt(8, movie.getLength());
            insertMovieStatement.setString(9, (movie.getGenre() == null ?
                    "null" : movie.getGenre().toString()));
            insertMovieStatement.setString(10, movie.getMpaaRating().toString());
            insertMovieStatement.setString(11, movie.getScreenwriter().getName());
            insertMovieStatement.setString(12, movie.getScreenwriter().getBirthdayString());
            insertMovieStatement.setString(13, (movie.getScreenwriter().getHairColor() == null ?
                    "null" : movie.getScreenwriter().getHairColor().toString()));
            insertMovieStatement.executeUpdate();

            movie.setOwner(userProfile.getName());
            return movieCollection.put(key, movie);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    private void checkPermission(Integer key, UserProfile userProfile) throws IllegalAccessException {
        if (!isUserPresented(userProfile)) {
            throw new IllegalAccessException("Current user doesn't exist");
        }
        Movie movie = movieCollection.get(key);
        if (movie == null) {
            throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
        }
        if (!movie.getOwner().equals(userProfile.getName())) {
            throw new IllegalAccessException("User \"" + userProfile.getName() + "\" doesn't have permission to update movie with key \"" + key + "\"");
        }
    }

    public Movie updateMovie(Integer key, Movie movie, UserProfile userProfile) throws IllegalAccessException {
        checkPermission(key, userProfile);
        readWriteLock.lock();
        try {
            updateMovieStatement.setString(1, movie.getName());
            updateMovieStatement.setFloat(2, movie.getCoordinates().getX());
            updateMovieStatement.setFloat(3, movie.getCoordinates().getY());
            updateMovieStatement.setString(4, movie.getCreationDate().toString());
            updateMovieStatement.setLong(5, movie.getOscarsCount());
            updateMovieStatement.setInt(6, movie.getLength());
            updateMovieStatement.setString(7, (movie.getGenre() == null ?
                    "null" : movie.getGenre().toString()));
            updateMovieStatement.setString(8, movie.getMpaaRating().toString());
            updateMovieStatement.setString(9, movie.getScreenwriter().getName());
            updateMovieStatement.setString(10, movie.getScreenwriter().getBirthdayString());
            updateMovieStatement.setString(11, (movie.getScreenwriter().getHairColor() == null ?
                    "null" : movie.getScreenwriter().getHairColor().toString()));
            updateMovieStatement.setInt(12, key);
            updateMovieStatement.executeUpdate();

            movie.setOwner(movieCollection.get(key).getOwner());
            return movieCollection.put(key, movie);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    public Movie removeMovie(Integer key, UserProfile userProfile) throws IllegalAccessException {
        checkPermission(key, userProfile);
        readWriteLock.lock();
        try {
            removeMovieStatement.setInt(1, key);
            removeMovieStatement.executeUpdate();
            return movieCollection.remove(key);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    void removeMovie(Integer key) {
        readWriteLock.lock();
        try {
            removeMovieStatement.setInt(1, key);
            removeMovieStatement.executeUpdate();
            movieCollection.remove(key);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
        } finally {
            readWriteLock.unlock();
        }
    }

    public void removeAllMovies(UserProfile userProfile) throws IllegalAccessException {
        long id = getUserID(userProfile);
        if (id == -1) {
            throw new IllegalAccessException("Current user doesn't exist");
        }
        readWriteLock.lock();
        try {
            removeAllMoviesStatement.setLong(1, id);
            removeAllMoviesStatement.executeUpdate();
            @SuppressWarnings("unchecked")
            Hashtable<Integer,Movie> hashtable = (Hashtable<Integer,Movie>) movieCollection.clone();
            hashtable.entrySet().stream()
                    .filter(e -> e.getValue().getOwner().equals(userProfile.getName()))
                    .map(Map.Entry::getKey)
                    .forEach(movieCollection::remove);
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
        } finally {
            readWriteLock.unlock();
        }
    }

    void removeAllMovies() {
        readWriteLock.lock();
        try {
            statement.executeQuery(String.format("DELETE FROM %s", movieTable));
        } catch (SQLException e) {
            ServerController.getInstance().error(e.getMessage());
        } finally {
            readWriteLock.unlock();
        }
        movieCollection.clear();
    }

    void loadCollectionsFromDB() throws SQLException, FieldException {
        ResultSet usersResultSet = statement.executeQuery(String.format("SELECT * FROM %s", usersTable));
        userCollection = new Hashtable<>();
        while (usersResultSet.next()) {
            Map.Entry<String,UserProfile> entry = parseUser(usersResultSet);
            userCollection.put(entry.getKey(), entry.getValue());
        }

        ResultSet moviesResultSet = statement.executeQuery(String.format("SELECT * FROM %s", movieTable));
        movieCollection = new Hashtable<>();
        while (moviesResultSet.next()) {
            Map.Entry<Integer,Movie> entry = parseMovie(moviesResultSet);
            movieCollection.put(entry.getKey(), entry.getValue());
        }
    }

    private Map.Entry<String,UserProfile> parseUser(ResultSet resultSet) throws SQLException {
        return new AbstractMap.SimpleEntry<>(
                resultSet.getString("user_login"),
                new UserProfile(
                        resultSet.getString("user_login"),
                        resultSet.getString("user_password"),
                        resultSet.getLong("user_id")
                )
        );
    }

    private Map.Entry<Integer,Movie> parseMovie(ResultSet resultSet) throws SQLException, FieldException {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(String.valueOf(resultSet.getLong("coordinates_x")));
        coordinates.setY(String.valueOf(resultSet.getLong("coordinates_y")));

        Person screenwriter = new Person();
        screenwriter.setName(resultSet.getString("screenwriter_name"));
        screenwriter.setBirthday(resultSet.getString("screenwriter_birthday").equals("null") ?
                null : resultSet.getString("screenwriter_birthday"));
        screenwriter.setHairColor(resultSet.getString("screenwriter_hair_color").equals("null") ?
                null : resultSet.getString("screenwriter_hair_color"));

        Movie movie = new Movie(resultSet.getLong("movie_id"),
                ZonedDateTime.parse(resultSet.getString("creation_date")));
        movie.setOwner(getUserName(resultSet.getLong("user_id")));
        movie.setName(resultSet.getString("name"));
        movie.setCoordinates(coordinates);
        movie.setOscarsCount(String.valueOf(resultSet.getLong("oscars_count")));
        movie.setLength(String.valueOf(resultSet.getInt("length")));
        movie.setGenre(resultSet.getString("genre").equals("null") ?
                null : resultSet.getString("genre"));
        movie.setMpaaRating(resultSet.getString("mpaa_rating"));
        movie.setScreenwriter(screenwriter);

        Integer key = resultSet.getInt("movie_key");
        return new AbstractMap.SimpleEntry<>(key,movie);
    }

    public Hashtable<Integer,Movie> getMovieCollection() {
        @SuppressWarnings("unchecked")
        Hashtable<Integer,Movie> hashtable = (Hashtable<Integer,Movie>) movieCollection.clone();
        return hashtable;
    }

    public int getCollectionElementsLimit() {
        return collectionElementsLimit;
    }
    public int getUserElementsLimit() {
        return userElementsLimit;
    }

    void printTables() {
        ServerController.getInstance().info("Users: " + userCollection.toString());
        ServerController.getInstance().info("Movies: " + movieCollection.toString());
    }
}
