package Commands;

import Client.File.FileExecutor;
import Client.File.FileManager;
import Server.ServerExecutor;
import Server.ServerINFO;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Command, which reads and executes script from file
 * @see NameableCommand
 * @see FileManager
 */
public class ExecuteScriptCommand extends NameableCommand {
    private File file;
    private final FileExecutor caller;

    public ExecuteScriptCommand(String commandName, FileExecutor caller) {
        super(commandName);
        this.caller = caller;
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO server) throws CommandException {
        throw new CommandException(getCommandName(), "\"" + getCommandName() + "\" can't be executed");
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        file = new File(args[0]);
        if (!file.exists() || !file.isFile()) {
            throw new BadArgumentsException(getCommandName(), "script with name \"" + args[0] + "\" doesn't exists");
        }
        for (FileExecutor curCaller = caller; curCaller != null; curCaller = curCaller.getCaller()) {
            if (curCaller.getFileName().equals(args[0])) {
                throw new BadArgumentsException(getCommandName(), "recursion is not supported");
            }
        }
    }

    @Override
    public void buildRequest() throws CommandException {
        try {
            FileManager fileManager = new FileManager(file, caller);
            fileManager.buildRequest();
        } catch (FileNotFoundException ignore) {
            //ignore
        }
    }
}
