package jua.jingle.core;

import java.util.Map;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.compiler.JinglesCompiler;
import jua.jingle.core.server.BellTower;
import jua.jingle.core.server.RingerLoop;
import jua.ringer.console.StdIoRinger;

public class JingleBells {

    public static void main(String[] args) throws Exception {

        // TODO init plugins from properties and/or parameters
        // ...

        // TODO init jingles from JB file set by command line params
        JinglesCompiler compiler = new JinglesCompiler();
        Map<String, Jingle> jingles = compiler.compile("examples/series/basel.jb");
        BellTower bellTower = new BellTower(jingles);

        // TODO init ringer from command line parameters
        RingerLoop mainLoop = new RingerLoop(bellTower, new StdIoRinger());
        mainLoop.loop();

    }
}
