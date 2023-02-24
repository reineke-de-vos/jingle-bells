package jua.jingle.core.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.compiler.tree.CalculatorDescriptor;
import jua.jingle.core.compiler.tree.JingleDescriptor;

public class JinglesCompiler {

    Map <String, Jingle> jingles;

    public Map<String, Jingle> compile(String path) {
        Map<String, JingleDescriptor> descriptors = SourceProcessor.process(path);
        jingles = new HashMap<>();
        for (JingleDescriptor descriptor: descriptors.values()) {
            Jingle jingle = JingleGenerator.generate(descriptor);

            jingle.init(jingles, buildDependencies(descriptor), null); // TODO more work with format
            jingles.put(descriptor.name, jingle);
        }
        
        return jingles;
    }

    private Map<String, Set<String>> buildDependencies(JingleDescriptor descriptor) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (CalculatorDescriptor cd : descriptor.calculators) {
            dependencies.put(cd.calcName, cd.sources);
        }
        return dependencies;
    }

}
