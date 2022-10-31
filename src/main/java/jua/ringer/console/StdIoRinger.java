package jua.ringer.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jua.jingle.plugin.ringer.RingerPlugin;

public class StdIoRinger implements RingerPlugin {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String command() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read std input", e);
        }
    }

    @Override
    public void response(String response) {
        System.out.println(response);
    }

}
