package jua.jingle.core.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.calculus.jingle.ValueException;
import jua.jingle.plugin.ringer.RingerPlugin;

public class BellTower {

    private BlockingQueue<Bell> queue = new LinkedBlockingQueue<>();
    private Map<String, Jingle> jingles;
    private Map<String, Set<Jingle>> targets;

    public BellTower(Map<String, Jingle> jingles) {
        this.jingles = jingles;

        // link Jingles
        targets = new HashMap<>();
        for (String name : jingles.keySet()) {
            targets.put(name, new HashSet<>());
        }
        for (Jingle jingle : jingles.values()) {
            for (String name : jingle.getSources()) {
                targets.get(name).add(jingle);
            }
        }

    }

    public void loop(RingerPlugin ringer) {
        while (true) {
            try {
                if (queue.peek() == null) return; // TODO look carefully at this place
                Bell bell = queue.take();
                String name = (bell.getName());
                Jingle jingle = jingles.get(name);
                if (jingle == null) {
                    ringer.response("No jingle with name " + name);
                    continue;
                }
                switch (bell.getAction()) {
                    case ECHO:
                        echo(ringer, jingles.get(name));
                        break;
                    case LOAD:
                        try {
                            jingles.get(bell.getName()).load(bell.getValue());
                        } catch (ValueException e) {
                            ringer.response(e.getMessage());
                        }
                        break;
                    case BELL:
                        long ttl = bell.getTtl();
                        if (ttl > 0) {
                            ttl--;
                        }
                        for (Jingle target : targets.get(name)) {
                            processBell(name, target, ttl, ringer);
                        }
                        break;
                    case TOUCH:
                        processBell(Jingle.TOUCH, jingles.get(name), bell.getTtl(), ringer);
                        break;
                    case LISTEN:
                        jingle.listen = true;
                }
            } catch (InterruptedException ex) {
                System.out.println("InterruptedException at " + this.getClass());
            }
        }
    }

    public void bell(BellAction action, String name, String value, long ttl) {
        queue.offer(new Bell(action, name, value, ttl));
    }

    private void processBell(String name, Jingle jingle, long ttl, RingerPlugin ringer) {
        if (ttl != 0) {
            jingle.onBell(name, ttl);
            // TODO check return value (will work with predicate implementation)
            if (jingle.listen) {
                echo(ringer, jingle);
            }
            bell(BellAction.BELL, jingle.name, null, ttl);
        }
    }

    private void echo(RingerPlugin ringer, Jingle jingle) {
        ringer.response(jingle.name + "=" + jingle.format());
    }

}
