package general.commands;

import general.ClientINFO;
import general.Request;
import general.element.Coordinates;
import general.element.FieldSetter;
import general.element.Movie;
import general.element.Person;
import general.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Type of command, which can read elements
 */
public abstract class ElementCommand extends NameableCommand {
    private static final Map<String, String> fieldExamples = new HashMap<>();
    private static final Map<String, Method> methodsSetters = new HashMap<>();
    private static final Map<Integer, String> order = new HashMap<>();
    protected Integer key;
    protected Movie readMovie;
    private transient ClientINFO client;

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
            if (method.getName().startsWith("set") && method.getAnnotation(FieldSetter.class) != null) {
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
     */
    public ElementCommand(String commandName) {
        super(commandName);
    }

    /**
     * Method, which read one field (line)
     * @param fieldName name of field we read
     * @return string - user input or null
     */
    protected String readOneField(String fieldName) {
        client.print("Enter " + fieldName + " (" + fieldExamples.get(fieldName) + "): ");
        String command = client.nextLine().trim();
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
                client.println("\u001B[31m" + e.getCause().getMessage() + "\u001B[0m");
            } catch (IllegalAccessException e) {
                client.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
            }
        }
    }

    /**
     * Main method. Read one element from reader
     * @return reader Movie element
     */
    protected Movie readMovie() {
        client.println("*reading Movie object starts*");
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
        client.println("*reading Movie object complete*");
        return newMovie;
    }

    /**
     * @see Command
     */
    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "value must be integer");
        }

        this.client = client;
        sendRequestAndCheckElement();

        try {
            this.readMovie = readMovie();
        } catch (NoSuchElementException e) {
            throw new BadArgumentsException(getCommandName(), "INVALID INPUT \"EOF\"");
        }
    }

    private void sendRequestAndCheckElement() throws BadArgumentsException {
        try {
            Response response = client.sendToServer(client.createNewRequest(
                    Request.RequestType.CHECK_ELEMENT,
                    key
            ));
            checkElement(response);
        } catch (SocketTimeoutException e) {
            throw new BadArgumentsException(getCommandName(), "Server is not responding, try later or choose another server");
        } catch (IOException | ClassNotFoundException e) {
            throw new BadArgumentsException(getCommandName(), e.getMessage());
        }
    }

    protected abstract void checkElement(Response response) throws BadArgumentsException;
}
