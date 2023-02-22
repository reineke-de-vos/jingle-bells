package jua.jingle.core.compiler.util;

import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.calculus.jingle.JingleFloat;
import jua.jingle.core.calculus.jingle.JingleInt;
import jua.jingle.core.compiler.tree.FormulaTree;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.FLOAT;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.INT;
import org.objectweb.asm.Type;

public class Types {

    public static FormulaTree.Type resolveType(String type) {
        switch (type) {
            case "int":
                return INT;
            case "float":
                return FLOAT;
            default:
                return null; // should be unreacheable
        }
    }

    public static String typeDescriptor(FormulaTree.Type type) {
        switch (type) {
            case INT:
                return Type.LONG_TYPE.getDescriptor();
            case FLOAT:
                return Type.DOUBLE_TYPE.getDescriptor();
        }
        throw new RuntimeException("Cannot convert type " + type);
    }

    public static Class<? extends Jingle> baseClass(FormulaTree.Type type) {
        switch (type) {
            case INT:
                return JingleInt.class;
            case FLOAT:
                return JingleFloat.class;
            default:
                return null; // should be unreacheable
        }
    }

}
