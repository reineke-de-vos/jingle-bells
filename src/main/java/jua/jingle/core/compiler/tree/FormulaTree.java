package jua.jingle.core.compiler.tree;

public abstract class FormulaTree implements TheTree {

    public Operation operation;
    public Type type;
    public Type desiredType;

    public FormulaTree(Operation operation, Type type) {
        this.operation = operation;
        this.type = type;
        this.desiredType = type;
    }

    public static class AcccessJingle extends FormulaTree {
        public String name;

        public AcccessJingle(Type type, String name) {
            super(Operation.JINGLE, type);
            this.name = name;
        }

    }

    public static class Value extends FormulaTree {
        public String value;
        public boolean negative;

        public Value(FormulaTree.Type type, String value) {
            super(Operation.VALUE, type);
            this.value = value;
            this.negative = false;
        }

        public Value(FormulaTree.Type type, String value, boolean negative) {
            this(type, value);
            this.negative = negative;
        }

        public void invert() {
            negative = !negative;
        }
    }

    public static class UnaryOperation extends FormulaTree {
        public FormulaTree operand;

        public UnaryOperation(Type type, FormulaTree operand) {
            super(Operation.NEG, type);
            this.operand = operand;
        }

    }

    public static class BinaryOperation extends FormulaTree {
        public FormulaTree left;
        public FormulaTree right;

        public BinaryOperation(Operation operation, Type type, FormulaTree left, FormulaTree right) {
            super(operation, type);
            this.left = left;
            this.right = right;
        }
    }

    public enum Operation {
        NEG,
        MUL,
        DIV,
        REM,
        ADD,
        SUB,
        JINGLE,
        VALUE
    }

    public enum Type {
        BOOLEAN,
        INT,
        FLOAT
    }

}
