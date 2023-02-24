package jua.jingle.core.server;

import jua.jingle.core.server.command.CommandLineIterator;
import jua.jingle.plugin.ringer.RingerPlugin;

public class RingerLoop {

    // TODO: actually RingerLoop should not know about jingles
    // it works with jingles by bellTower and Bell interface
    // so it should be reworked in the future
    private BellTower bellTower;
    private RingerPlugin ringer;


    public RingerLoop(BellTower bellTower, RingerPlugin ringer) {
        this.bellTower = bellTower;
        this.ringer = ringer;
    }

    // TODO create event listener instead of one thread processing
    public void loop() {
        while (true) {
            String command = ringer.command();
            // parse bell (command)
            CommandLineIterator cli = new CommandLineIterator(command);

            switch (cli.next()) {
                case "load":
                    // set value to jingle
                    // USAGE: load jinglename value
                    String name = cli.next();
                    String value = cli.next();
                    if (name == null || value == null ) {
                        ringer.response("Jingle name/value should not be empty");
                    } else {
                        bellTower.bell(BellAction.LOAD, name, value, 0);
                    }
                    break;
                case "read":
                    // report current jingle value
                    // USAGE: read jinglename
                    name = cli.next();
                    if (name == null) {
                        ringer.response("Jingle name/value should not be empty");
                    } else {
                        bellTower.bell(BellAction.ECHO, name, null, 0);
                    }                    
                    break;
                case "listen":
                    // mark jingle to report value on every bell
                    // USAGE: listen jinglename
                    name = cli.next();
                    if (name == null) {
                        ringer.response("Jingle name should not be empty");
                    } else {
                        bellTower.bell(BellAction.LISTEN, name, null, 0);
                    }
                    break;
                case "touch":
                    // invoke default calculator of jingle and then bell targets
                    // USAGE: touch jignlename [ ttl ttlvalue ]
                    name = cli.next();
                    if (name == null) {
                        ringer.response("Jingle name should not be empty");
                    } else {
                        long ttl = getTtl(cli);
                        if (ttl != 0) {
                            bellTower.bell(BellAction.TOUCH, name, null, ttl);
                        }
                    }
                    break;
                case "bell":
                    // Usage: bell jinglename [ ttl ttlvalue ]
                    // bell targets of named jingle
                    name = cli.next();
                    if (name == null) {
                        ringer.response("Jingle name should not be empty");
                    } else {
                        long ttl = getTtl(cli);
                        if (ttl != 0) {
                            bellTower.bell(BellAction.BELL, name, null, ttl);
                        }
                    }
                    break;
                case "stop":
                    // should be implemented in two-thread version to avoid infinite loops
                    // bellTower.bell(BellAction.STOP, null, null, 0);
                    ringer.response("Not supported in one thread version");
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    ringer.response("Unknown command");
                    break;
            }
            bellTower.loop(ringer);
        }
    }

    private long getTtl(CommandLineIterator cli) {
        String ttlKey = cli.next();
        if (ttlKey == null) {
            return Bell.INFINITE_TTL;
        } else if (ttlKey.equals("ttl")) {
            String sttl = cli.next();
            try {
                long ttl = Long.parseLong(sttl);
                if (ttl <= 0) {
                    ringer.response("Ttl should be positive");
                    return 0;
                }
                return ttl;
            } catch (Exception e) {
                ringer.response("Wrong ttl value " + sttl);
                return 0;
            }
        } else {
            ringer.response("Wrong parameter " + ttlKey);
            return Bell.INFINITE_TTL;
        }
    }

    // TODO think how to do it
    public void shutdown() {
    }

}
