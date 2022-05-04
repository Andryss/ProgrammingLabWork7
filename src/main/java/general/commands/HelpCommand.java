package general.commands;

import general.ClientINFO;
import general.ServerINFO;

/**
 * Command, which prints a list of available commands
 * @see NameableCommand
 * @see ParseCommand
 */
public class HelpCommand extends NameableCommand {

    @ParseCommand(name = "help", example = "help")
    public HelpCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO serverINFO) {
        serverINFO.getResponse()
                .addMessage("*List of available commands*")
                .addMessage("help : print a list of available commands")
                .addMessage("info : print short info about the collection (type, init date, length etc)")
                .addMessage("show : print all elements in the collection")
                .addMessage("insert null {element} : add a new element with the given key")
                .addMessage("update null {element} : update your element with the given key")
                .addMessage("remove_key null : delete your element with the given key")
                .addMessage("clear : delete your elements from the collection")
                .addMessage("execute_script file_name : read and execute script from file")
                .addMessage("exit : end the program")
                .addMessage("history : print the last 13 commands (without arguments)")
                .addMessage("replace_if_greater null {element} : replace your element by the key if the new value is greater than the old one")
                .addMessage("remove_lower_key null : remove all your elements whose key is less than given")
                .addMessage("group_counting_by_length : group the elements by the value of the \"length\" field, print the number of elements in each group")
                .addMessage("count_less_than_length length : print the number of elements whose \"length\" less than the given")
                .addMessage("filter_by_mpaa_rating mpaaRating : print an elements whose \"mpaaRating\" is equal to the given");
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length > 0) {
            throw new BadArgumentsCountException(getCommandName());
        }
    }
}
