package general.commands;

/**
 * <p>Command implementation with the name. Especially for fewer mistakes.</p>
 * <p>You need to give it a name. So if there ane any troubles it will say that "command with *given name* have some troubles"</p>
 * @see Command
 */
public abstract class NameableCommand implements Command {

    private final String commandName;

    /**
     * Constructor with name of command
     * @param commandName name you want
     */
    public NameableCommand(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
