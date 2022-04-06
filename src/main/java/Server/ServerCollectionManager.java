package Server;

import MovieObjects.Coordinates;
import MovieObjects.FieldException;
import MovieObjects.Movie;
import MovieObjects.Person;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;


public class ServerCollectionManager {
    private static Connection connection;
    private static Hashtable<Integer, Movie> movieCollection;
    private static Hashtable<String, Map.Entry<Long, String>> userCollection;

    private ServerCollectionManager() {}

    private static final String dbHostName = "pg";
    private static final String dbName = "studs";
    private static final String dbUser = "s335155";
    private static final String dbPassword = "wpr492";

    private static final String usersTable = "users_335155";
    private static final String movieTable = "movie_335155";

    public static void initialize() throws ClassNotFoundException, SQLException, FieldException {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s/%s", dbHostName, dbName), dbUser, dbPassword);
        createTables();
        initializeStatements();
        loadCollectionsFromDB();
    }

    private static Statement statement;
    private static PreparedStatement getUserStatement;
    private static PreparedStatement getUserNameStatement;
    private static PreparedStatement insertUserStatement;
    private static PreparedStatement removeUserStatement;
    private static PreparedStatement getMovieStatement;
    private static PreparedStatement insertMovieStatement;
    private static PreparedStatement updateMovieStatement;
    private static PreparedStatement removeMovieStatement;

    private static void initializeStatements() throws SQLException {
        statement = connection.createStatement();
        getUserStatement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE user_login=?", usersTable));
        getUserNameStatement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE user_id=?", usersTable));
        insertUserStatement = connection.prepareStatement(String.format("INSERT INTO %s (" +
                "user_login," +
                "user_password" +
                ") VALUES (?,?)", usersTable));
        removeUserStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE user_login=?", usersTable));
        getMovieStatement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE movie_key=?", movieTable));
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
    }

    private static void createTables() throws SQLException {
        statement.execute(String.format("CREATE TABLE IF NOT EXISTS %s (\n" +
                "user_id BIGSERIAL,\n" +
                "user_login TEXT PRIMARY KEY,\n" +
                "user_password TEXT\n" +
                ")", usersTable));
        statement.execute(String.format("CREATE TABLE If NOT EXISTS %s (\n" +
                "movie_id bigserial,\n" +
                "user_id bigint,\n" +
                "movie_key INT PRIMARY KEY, \n" +
                "name TEXT,\n" +
                "coordinates_x REAL,\n" +
                "coordinates_y REAL,\n" +
                "creation_date TEXT,\n" +
                "oscars_count BIGINT,\n" +
                "length INT,\n" +
                "genre TEXT,\n" +
                "mpaa_rating TEXT,\n" +
                "screenwriter_name TEXT,\n" +
                "screenwriter_birthday TEXT,\n" +
                "screenwriter_hair_color TEXT\n" +
                ")", movieTable));
    }

    private static void dropTables() throws SQLException {
        statement.execute(String.format("DROP TABLE %s", usersTable));
        statement.execute(String.format("DROP TABLE %s", movieTable));
    }

    public static long getUserID(String userName) {
        return userCollection.get(userName) == null ? -1 : userCollection.get(userName).getKey();
    }

    public static long getUserID(String userName, String userPassword) {
        Map.Entry<Long,String> entry = userCollection.get(userName);
        return (entry != null && entry.getValue().equals(userPassword)) ? entry.getKey() : -1;
    }

    public static boolean isUserPresented(String userName, String userPassword) {
        return getUserID(userName, userPassword) != -1;
    }

    public static String getUserName(long id) {
        Optional<Map.Entry<String,Map.Entry<Long,String>>> user = userCollection.entrySet().stream().filter(e -> e.getValue().getKey() == id).findAny();
        return user.map(Map.Entry::getKey).orElse(null);
    }

    public static long registerUser(String userName, String userPassword) throws SQLException {
        if (getUserID(userName, userPassword) == -1) {
            insertUserStatement.setString(1, userName);
            insertUserStatement.setString(2, userPassword);
            insertUserStatement.execute();

            getUserStatement.setString(1, userName);
            ResultSet resultSet = getUserStatement.executeQuery();
            if (resultSet.next()) {
                long userID = resultSet.getLong("user_id");
                userCollection.put(userName, new AbstractMap.SimpleEntry<>(userID, userPassword));
                return userID;
            }
        }
        return -1;
    }

    public static Map.Entry<Long,String> removeUser(String userName, String userPassword) throws SQLException {
        if (userCollection.get(userName).getValue().equals(userPassword)) {
            removeUserStatement.setString(1, userName);
            removeUserStatement.executeUpdate();
            return userCollection.remove(userName);
        }
        return null;
    }

    public static Movie getMovie(Integer key) {
        return movieCollection.get(key).clone();
    }

    public static Movie putMovie(Integer key, Movie movie, String userName, String userPassword) throws SQLException, IllegalAccessException {
        long userID = getUserID(userName, userPassword);
        if (userID == -1) {
            throw new IllegalAccessException("User with name \"" + userName + "\" doesn't exist");
        }
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
        movie.setOwner(userName);
        return movieCollection.put(key, movie);
    }

    private static void checkPermission(Integer key, String userName, String userPassword) throws IllegalAccessException, SQLException {
        long userID = getUserID(userName, userPassword);
        if (userID == -1) {
            throw new IllegalAccessException("User with name \"" + userName + "\" doesn't exist");
        }
        getMovieStatement.setInt(1, key);
        ResultSet resultSet = getMovieStatement.executeQuery();
        if (!resultSet.next()) {
            throw new IllegalAccessException("Movie with key \"" + key + "\" doesn't exist");
        }
        if (resultSet.getLong("user_id") != userID) {
            throw new IllegalAccessException("User \"" + userName + "\" doesn't have permission to update movie with key \"" + key + "\"");
        }
    }

    public static Movie updateMovie(Integer key, Movie movie, String userName, String userPassword) throws SQLException, IllegalAccessException {
        checkPermission(key, userName, userPassword);
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
        return movieCollection.put(key, movie);
    }

    public static Movie removeMovie(Integer key, String userName, String userPassword) throws SQLException, IllegalAccessException {
        checkPermission(key, userName, userPassword);
        removeMovieStatement.setInt(1, key);
        removeMovieStatement.executeUpdate();
        return movieCollection.remove(key);
    }

    private static void loadCollectionsFromDB() throws SQLException, FieldException {
        ResultSet usersResultSet = statement.executeQuery(String.format("SELECT * FROM %s", usersTable));
        userCollection = new Hashtable<>();
        while (usersResultSet.next()) {
            Map.Entry<String,Map.Entry<Long,String>> entry = parseUser(usersResultSet);
            userCollection.put(entry.getKey(), entry.getValue());
        }

        ResultSet moviesResultSet = statement.executeQuery(String.format("SELECT * FROM %s", movieTable));
        movieCollection = new Hashtable<>();
        while (moviesResultSet.next()) {
            Map.Entry<Integer,Movie> entry = parseMovie(moviesResultSet);
            movieCollection.put(entry.getKey(), entry.getValue());
        }
    }

    private static Map.Entry<String,Map.Entry<Long,String>> parseUser(ResultSet resultSet) throws SQLException {
        return new AbstractMap.SimpleEntry<>(
                resultSet.getString("user_login"),
                new AbstractMap.SimpleEntry<>(
                        resultSet.getLong("user_id"),
                        resultSet.getString("user_password")
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
        movie.setMpaaRating("mpaa_rating");
        movie.setScreenwriter(screenwriter);

        Integer key = resultSet.getInt("movie_key");
        return new AbstractMap.SimpleEntry<>(key,movie);
    }

    public static Hashtable<Integer,Movie> getMovieCollection() {
        return (Hashtable<Integer, Movie>) movieCollection.clone();
    }
}
