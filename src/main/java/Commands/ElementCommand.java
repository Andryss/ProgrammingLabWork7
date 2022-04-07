package Commands;

import Client.ClientController;
import MovieObjects.Coordinates;
import MovieObjects.FieldSetter;
import MovieObjects.Movie;
import MovieObjects.Person;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Type of command, which can read elements
 */
public abstract class ElementCommand extends NameableCommand {
    /**
     * Map with examples of commands (for example, "insert" - "insert 12")
     * @see FieldSetter
     */
    private static final Map<String, String> fieldExamples = new HashMap<>();
    /**
     * Map with methods we need to invoke to get fields of Movie
     * @see FieldSetter
     * @see Movie
     * @see Coordinates
     * @see Person
     */
    private static final Map<String, Method> methodsSetters = new HashMap<>();
    /**
     * Map with the right order of fields we need to read
     * @see FieldSetter
     */
    private static final Map<Integer, String> order = new HashMap<>();
    /**
     * Do we need to ask a questions "Enter *something*:"?
     */
    private final transient boolean readingFromFile;

    /**
     * Scanner for reading elements from something
     */
    protected transient Scanner reader;
    /**
     * Given element key
     */
    protected Integer key;
    /**
     * Read Movie element
     */
    protected Movie readMovie;

    static {
        fillMethodsSetters(methodsSetters, Movie.class);
    }

    /**
     * Here we analyze class and fill Maps: fieldExamples, methodsSetters, order
     * @param emptyMethodsSetters not fully filled methodsSetters
     * @param cls class we need to analyze
     * @see FieldSetter
     * @see Movie
     * @see Coordinates
     * @see Person
     */
    private static void fillMethodsSetters(Map<String, Method> emptyMethodsSetters, Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.getName().startsWith("set")) {
                if (method.getParameters()[0].getType().equals(String.class)) {
                    FieldSetter annotation = method.getAnnotation(FieldSetter.class);
                    String fieldName = cls.getSimpleName() + " " + annotation.fieldName();
                    emptyMethodsSetters.put(fieldName, method);
                    fieldExamples.put(fieldName, annotation.example());
                    order.put(annotation.index(), fieldName);
                } else {
                    fillMethodsSetters(emptyMethodsSetters, method.getParameters()[0].getType());
                }
            }
        }
    }

    /**
     * Constructor with name, Hashtable, Scanner and boolean
     * @param reader for reading elements from something
     * @param readingFromFile flag for asking questions
     */
    public ElementCommand(String commandName, Scanner reader, boolean readingFromFile) {
        super(commandName);
        this.reader = reader;
        this.readingFromFile = readingFromFile;
    }

    /**
     * Method, which read one field (line)
     * @param fieldName name of field we read
     * @return string - user input or null
     */
    protected String readOneField(String fieldName) {
        if (!readingFromFile) {
            ClientController.print("Enter " + fieldName + " (" + fieldExamples.get(fieldName) + "): ");
        }
        String command = reader.nextLine().trim();
        return (command.equals("") ? null : command);
    }

    /**
     * Method, which tries to read and set only one field
     * @param object object, which field we set
     * @param fieldName name of field we set
     * @param method setter we invoke
     */
    protected void setOneField(Object object, String fieldName, Method method) {
        while (true) {
            try {
                method.invoke(object, readOneField(fieldName));
                break;
            } catch (InvocationTargetException e) {
                ClientController.println("\u001B[31m" + e.getCause().getMessage() + "\u001B[0m");
            } catch (IllegalAccessException e) {
                ClientController.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
            }
        }
    }

    /**
     * Main method. Read one element from reader
     * @return reader Movie element
     */
    protected Movie readMovie() {
        if (!readingFromFile) {
            ClientController.println("*reading Movie object starts*");
        }
        Movie newMovie = new Movie();
        Coordinates newCoordinates = new Coordinates();
        Person newScreenwriter = new Person();
        for (Integer step : order.keySet()) {
            String fieldName = order.get(step);
            Method method = methodsSetters.get(fieldName);
            if (method.getDeclaringClass() == Movie.class) {
                setOneField(newMovie, fieldName, method);
            } else if (method.getDeclaringClass() == Coordinates.class) {
                setOneField(newCoordinates, fieldName, method);
            } else if (method.getDeclaringClass() == Person.class) {
                setOneField(newScreenwriter, fieldName, method);
            } else {
                throw new RuntimeException("Some problems with \"methodsSetters\" (find new class?)");
            }
        }
        newMovie.setCoordinates(newCoordinates);
        newMovie.setScreenwriter(newScreenwriter);
        if (!readingFromFile) {
            ClientController.println("*reading Movie object complete*");
        }
        return newMovie;
    }

    /**
     * @see Command
     */
    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            this.key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "value must be integer");
        }
        try {
            this.readMovie = readMovie();
        } catch (NoSuchElementException e) {
            throw new BadArgumentsException(getCommandName(), "INVALID INPUT \"EOF\"");
        }
    }
}
