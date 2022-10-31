package jua.jingle.core.compiler;

import jua.jingle.core.calculus.jingle.JingleFloat;
import jua.jingle.core.calculus.jingle.JingleInt;

public enum JingleType {
    INT(JingleInt.class),
    FLOAT(JingleFloat.class);

    private Class<?> parentClass;

    JingleType(Class<?> parentClass) {
        this.parentClass = parentClass;
    }

    public static JingleType resolveType(String type) {
        switch (type) {
            case "int":
                return INT;
            case "float":
                return FLOAT;
            default:
                return null; // should be unreacheable
        }
    }

    public Class<?> parentClass() {
        return parentClass;
    }

}
