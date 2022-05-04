package general.commands;

import general.ClientINFO;
import general.Request;
import general.ServerINFO;

/**
 * Command, which prints the number of elements whose "length" less than the given
 * @see NameableCommand
 */
public class CountLessThenLengthCommand extends NameableCommand {

    private int length;

    @ParseCommand(name = "count_less_than_length", example = "count_less_than_length 90")
    public CountLessThenLengthCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(ServerINFO server) throws CommandException {
        server.getResponse().addMessage("Found " +
                server.getMovieCollection().values().stream()
                        .filter(movie -> movie.getLength() < length)
                        .count()
                + " movies with length less than " + length);
    }

    @Override
    public void setArgs(ClientINFO client, String... args) throws BadArgumentsException {
        if (args.length != 1) {
            throw new BadArgumentsCountException(getCommandName(), 1);
        }
        try {
            length = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new BadArgumentsFormatException(getCommandName(), "value must be integer");
        }
    }

    @Override
    public void buildRequest(Request request) throws CommandException {
        CountLessThenLengthCommand command = new CountLessThenLengthCommand(getCommandName());
        command.length = length;
        request.addCommand(command);
    }
}
