package jua.jingle.core.compiler.tree;

import java.util.List;

public class JingleDescriptor implements TheTree {
    public String name;
    public FormulaTree.Type type;
    public FormulaTree.Value defaultValue;
    public List<CalculatorDescriptor> calculators;

    public JingleDescriptor(
            String name,
            FormulaTree.Type type) {
        this.name = name;
        this.type = type;
    }

}
