package general.commands;

import general.ClientINFO;
import client.file.FileExecutor;
import client.file.FileManager;
import general.Request;
import general.ServerINFO;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Command, which reads and executes script from file
 * @see NameableCommand
 * @see FileManager
 */
public class ExecuteScriptCommand extends NameableCommand {
    private File file;
    private FileExecutor caller;

    @ParseCommand(name = "execute_script", example = "execute_script someScript")
    public ExecuteScriptCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        throw new CommandException(getCommandName(), "\"" + getCommandName() + "\" can't be executed");
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        file = new File(args[0]);
        if (!file.exists() || !file.isFile()) {
            throw new BadArgumentsException(getCommandName(), "script with name \"" + args[0] + "\" doesn't exists");
        }
        caller = client.getCaller();
        for (FileExecutor curCaller = caller; curCaller != null; curCaller = curCaller.getCaller()) {
            if (curCaller.getFileName().equals(args[0])) {
                throw new BadArgumentsException(getCommandName(), "recursion is not supported");
            }
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        try {
            FileManager fileManager = new FileManager(file, caller, request);
            fileManager.buildRequest();
        } catch (FileNotFoundException ignore) {
            //ignore
        }
    }
}
