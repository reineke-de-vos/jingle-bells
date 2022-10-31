package jua.ringer.console;

import java.io.Console;
import jua.jingle.plugin.ringer.RingerPlugin;

public class ConsoleRinger implements RingerPlugin {

    private Console console = System.console();

    @Override
    public String command() {
        return console.readLine();
    }

    @Override
    public void response(String response) {
        console.printf("%s\n", response);
    }

}
