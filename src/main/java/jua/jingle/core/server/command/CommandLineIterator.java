package jua.jingle.core.server.command;

public class CommandLineIterator {

    private String[] cwp; // command with parameters

    public CommandLineIterator(String command) {
        cwp = command.trim().split("\\s+");
    }

    private int position = -1;
    public String next() {
        position++;
        return (position < cwp.length) ? cwp[position] : null;
    }

}
