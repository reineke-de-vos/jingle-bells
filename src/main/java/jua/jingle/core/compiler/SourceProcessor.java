package jua.jingle.core.compiler;

import java.util.Map;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.parser.JingleBellsLexer;
import jua.jingle.core.parser.JingleBellsParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class SourceProcessor {

    public Map<String, Jingle> processSource(String path) {
        try {
            JingleBellsLexer lexer = new JingleBellsLexer(CharStreams.fromFileName(path));
            JingleBellsParser parser = new JingleBellsParser(new CommonTokenStream(lexer));
            ParseTree tree = parser.program();
            // TODO process parse errors here before compilation

            JinglesGenerator generator = new JinglesGenerator();
            ParseTreeWalker.DEFAULT.walk(generator, tree);

            return generator.jingles();
        } catch (Exception e) {
            throw new RuntimeException("File processing error", e);
        }
    }

}
