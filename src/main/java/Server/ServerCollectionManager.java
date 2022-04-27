package Server;

import MovieObjects.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerCollectionManager is the main class which working with database and collections
 */
public class ServerCollectionManager {
    private static Connection connection;
    private static Hashtable<Integer, Movie> movieCollection;
    private static Hashtable<String, UserProfile> userCollection;
    private static final ReentrantLock readWriteLock = new ReentrantLock();

    private ServerCollectionManager() {}

    private static final String dbHostName = "pg";
    private static final String dbName = "studs";
    private static String dbUser;
    private static String dbPassword;

    private static final String usersTable = "users_335155";
    private static final String movieTable = "movie_335155";

    public static void initialize() throws ClassNotFoundException, SQLException, FieldException, FileNotFoundException {
        Class.forName("org.postgresql.Driver");
        loadDBPrivates();
        connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s", dbHostName, dbName), dbUser, dbPassword);
        initializeStatements();
        createTables();
        loadCollectionsFromDB();
        printTables();
    }

    private static void loadDBPrivates() throws FileNotFoundException {
        try {
            Scanner scanner = new Scanner(new File(".pgpass"));
            String[] args = scanner.nextLine().split(":");
            dbUser = args[3];
            dbPassword = args[4];
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File \".pgpass\" with login and password to connect to database not found (it must be <>:<>:<>:<username>:<password> format)");
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("File \".pgpass\" has no line (it must be <>:<>:<>:<username>:<password> format)");
        } catch (IndexOutOfBoundsException e) {
            throw new FileNotFoundException("File \".pgpass\" must have first string with <>:<>:<>:<username>:<password> format");
        }

    }

    private static Statement statement;
    private static PreparedStatement getUserStatement;
    private static PreparedStatement insertUserStatement;
    private static PreparedStatement removeUserStatement;
    private static PreparedStatement insertMovieStatement;
    private static PreparedStatement updateMovieStatement;
    private static PreparedStatement removeMovieStatement;
    private static PreparedStatement removeAllMoviesStatement;

    private static void initializeStatements() throws SQLException {
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

    static void createTables() throws SQLException {
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

    static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        }
    }

    static void dropTables() {
        try {
            statement.execute(String.format("DROP TABLE %s", usersTable));
            statement.execute(String.format("DROP TABLE %s", movieTable));
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        }
    }

    public static long getUserID(String userName) {
        return userCollection.get(userName) == null ? -1 : userCollection.get(userName).getId();
    }

    public static long getUserID(UserProfile user) {
        UserProfile userProfile = userCollection.get(user.getName());
        return (userProfile != null && userProfile.getPassword().equals(user.getPassword()) ? userProfile.getId() : -1);
    }

    public static boolean isUserPresented(UserProfile userProfile) {
        return getUserID(userProfile) != -1;
    }

    public static String getUserName(long id) {
        Optional<Map.Entry<String,UserProfile>> user = userCollection.entrySet().stream().filter(e -> e.getValue().getId() == id).findAny();
        return user.map(Map.Entry::getKey).orElse(null);
    }

    public static long registerUser(UserProfile userProfile) {
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
                ServerController.error(e.getMessage(), e);
            } finally {
                readWriteLock.unlock();
            }
        }
        return -1;
    }

    public static UserProfile removeUser(UserProfile userProfile) {
        readWriteLock.lock();
        try {
            if (!userCollection.get(userProfile.getName()).getPassword().equals(userProfile.getPassword())) {
                return null;
            }
            removeUserStatement.setString(1, userProfile.getName());
            removeUserStatement.executeUpdate();
            return userCollection.remove(userProfile.getName());
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    static void removeUser(String userName) {
        readWriteLock.lock();
        try {
            removeUserStatement.setString(1, userName);
            removeUserStatement.executeUpdate();
            userCollection.remove(userName);
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        } finally {
            readWriteLock.unlock();
        }
    }

    public static Movie getMovie(Integer key) {
        try {
            return movieCollection.get(key).clone();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Movie putMovie(Integer key, Movie movie, UserProfile userProfile) throws IllegalAccessException {
        long userID = getUserID(userProfile);
        if (userID == -1) {
            throw new IllegalAccessException("User with name \"" + userProfile.getName() + "\" doesn't exist");
        } else if (movieCollection.get(key) != null) {
            throw new IllegalAccessException("Movie with key \"" + key + "\" already exists");
        } else if (movieCollection.size() >= 10) {
            throw new IllegalAccessException("Collection limit (10) exceeded");
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
            ServerController.error(e.getMessage(), e);
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    private static void checkPermission(Integer key, UserProfile userProfile) throws IllegalAccessException {
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

    public static Movie updateMovie(Integer key, Movie movie, UserProfile userProfile) throws IllegalAccessException {
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
            ServerController.error(e.getMessage(), e);
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    public static Movie removeMovie(Integer key, UserProfile userProfile) throws IllegalAccessException {
        checkPermission(key, userProfile);
        readWriteLock.lock();
        try {
            removeMovieStatement.setInt(1, key);
            removeMovieStatement.executeUpdate();
            return movieCollection.remove(key);
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
            return null;
        } finally {
            readWriteLock.unlock();
        }
    }

    static void removeMovie(Integer key) {
        readWriteLock.lock();
        try {
            removeMovieStatement.setInt(1, key);
            removeMovieStatement.executeUpdate();
            movieCollection.remove(key);
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        } finally {
            readWriteLock.unlock();
        }
    }

    public static void removeAllMovies(UserProfile userProfile) throws IllegalAccessException {
        long id = getUserID(userProfile);
        if (id == -1) {
            throw new IllegalAccessException("Current user doesn't exist");
        }
        readWriteLock.lock();
        try {
            removeAllMoviesStatement.setLong(1, id);
            removeAllMoviesStatement.executeUpdate();
            ((Hashtable<Integer,Movie>) movieCollection.clone()).entrySet().stream()
                    .filter(e -> e.getValue().getOwner().equals(userProfile.getName()))
                    .map(Map.Entry::getKey)
                    .forEach(movieCollection::remove);
        } catch (SQLException e) {
            ServerController.error(e.getMessage(), e);
        } finally {
            readWriteLock.unlock();
        }
    }

    private static void loadCollectionsFromDB() throws SQLException, FieldException {
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

    private static Map.Entry<String,UserProfile> parseUser(ResultSet resultSet) throws SQLException {
        return new AbstractMap.SimpleEntry<>(
                resultSet.getString("user_login"),
                new UserProfile(
                        resultSet.getString("user_login"),
                        resultSet.getString("user_password"),
                        resultSet.getLong("user_id")
                )
        );
    }

    private static Map.Entry<Integer,Movie> parseMovie(ResultSet resultSet) throws SQLException, FieldException {
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

    public static Hashtable<Integer,Movie> getMovieCollection() {
        return (Hashtable<Integer, Movie>) movieCollection.clone();
    }

    static void printTables() {
        ServerController.info("Users: " + userCollection.toString());
        ServerController.info("Movies: " + movieCollection.toString());
    }
}
