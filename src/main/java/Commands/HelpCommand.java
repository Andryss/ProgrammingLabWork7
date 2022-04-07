package Commands;

import Server.ServerExecutor;
import Server.ServerINFO;

/**
 * Command, which prints a list of available commands
 * @see NameableCommand
 * @see Command
 */
public class HelpCommand extends NameableCommand {

    public HelpCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean execute(ServerExecutor.ExecuteState state, ServerINFO serverINFO) {
        setServerINFO(serverINFO);
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        println("*List of available commands*");
        println("help : print a list of available commands");
        println("info : print short info about the collection (type, init date, length etc)");
        println("show : print all elements in the collection");
        println("insert null {element} : add new element with given key");
        println("update null {element} : update an element with given key");
        println("remove_key null : delete an element with given key");
        println("clear : clear the collection");
        println("execute_script file_name : read and execute script from file");
        println("exit : end the program (without saving)");
        println("history : print last 13 commands (without arguments)");
        println("replace_if_greater null {element} : replace an element by key if the new value is greater than the old one");
        println("remove_lower_key null : remove all elements whose key is less than given");
        println("group_counting_by_length : group the elements by the value of the \"length\" field, print the number of elements in each group");
        println("count_less_than_length length : print the number of elements whose \"length\" less than the given");
        println("filter_by_mpaa_rating mpaaRating : print an elements whose \"mpaaRating\" is equal to the given");
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
