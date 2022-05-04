package general.commands;

import client.ClientExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * This class has only one method, which walk through general.commands directory and fill command map with commands
 */
public abstract class CommandFiller {

    public static void fillCommandMap(HashMap<String,Command> commandMap) throws IOException, CommandException {
        try {
            List<String> classNames = new JarFile(new File(ClientExecutor.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())).stream()
                    .map(ZipEntry::toString)
                    .filter(s -> s.startsWith("general/commands/") && s.endsWith(".class"))
                    .map(s -> s.substring(0, s.length() - 6).replaceAll("/", "."))
                    .collect(Collectors.toList());
            for (String className : classNames) {
                try {
                    Class<?> cls = Class.forName(className);
                    if (Arrays.stream(cls.getInterfaces()).noneMatch(c -> c == Command.class)) {
                        continue;
                    }
                    Constructor<?> constructor = cls.getDeclaredConstructor(String.class);
                    ParseCommand command = constructor.getDeclaredAnnotation(ParseCommand.class);
                    if (command == null) {
                        continue;
                    }
                    if (commandMap.containsKey(command.name())) {
                        throw new CommandException(command.name(), "found at least 2 commands in general/commands/ with the same name, what is forbidden");
                    }
                    commandMap.put(command.name(), (Command) constructor.newInstance(command.name()));
                    BadArgumentsException.getExamples().put(command.name(), command.example());
                } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            throw new IOException("Some io problems: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new IOException("Some syntax problems: " + e.getMessage());
        }
    }

}
