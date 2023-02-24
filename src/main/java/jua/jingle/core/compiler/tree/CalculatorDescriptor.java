package jua.jingle.core.compiler.tree;

import java.util.Set;

public class CalculatorDescriptor implements TheTree {
    public String calcName;
    public Set<String> sources;
    public FormulaTree formula;

    public CalculatorDescriptor(String calcName, Set<String> sources, FormulaTree formula) {
        this.calcName = calcName;
        this.sources = sources;
        this.formula = formula;
    }

}
