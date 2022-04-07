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
        if (state == ServerExecutor.ExecuteState.VALIDATE) {
            return true;
        }
        serverINFO.getResponseBuilder()
                .add("*List of available commands*")
                .add("help : print a list of available commands")
                .add("info : print short info about the collection (type, init date, length etc)")
                .add("show : print all elements in the collection")
                .add("insert null {element} : add new element with given key")
                .add("update null {element} : update an element with given key")
                .add("remove_key null : delete an element with given key")
                .add("clear : clear the collection")
                .add("execute_script file_name : read and execute script from file")
                .add("exit : end the program (without saving)")
                .add("history : print last 13 commands (without arguments)")
                .add("replace_if_greater null {element} : replace an element by key if the new value is greater than the old one")
                .add("remove_lower_key null : remove all elements whose key is less than given")
                .add("group_counting_by_length : group the elements by the value of the \"length\" field, print the number of elements in each group")
                .add("count_less_than_length length : print the number of elements whose \"length\" less than the given")
                .add("filter_by_mpaa_rating mpaaRating : print an elements whose \"mpaaRating\" is equal to the given");
        return true;
    }

    @Override
    public void setArgs(String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
