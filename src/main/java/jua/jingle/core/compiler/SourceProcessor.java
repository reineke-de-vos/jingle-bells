package jua.jingle.core.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jua.jingle.core.compiler.tree.CalculatorDescriptor;
import jua.jingle.core.compiler.tree.FormulaTree;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.ADD;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.DIV;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.MUL;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.NEG;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.REM;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.SUB;
import static jua.jingle.core.compiler.tree.FormulaTree.Operation.VALUE;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.FLOAT;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.INT;
import jua.jingle.core.compiler.tree.JingleDescriptor;
import jua.jingle.core.compiler.tree.TheTree;
import jua.jingle.core.compiler.util.Types;
import jua.jingle.core.parser.JingleBellsBaseVisitor;
import jua.jingle.core.parser.JingleBellsLexer;
import jua.jingle.core.parser.JingleBellsParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SourceProcessor {

    static final String CALCULATOR_PREFIX = "calculator_";

    public static Map<String, JingleDescriptor> process(String path) {
        try {
            JingleBellsLexer lexer = new JingleBellsLexer(CharStreams.fromFileName(path));
            JingleBellsParser parser = new JingleBellsParser(new CommonTokenStream(lexer));
            JinglesTreeProcessor processor = new JinglesTreeProcessor();
            ParseTree tree = parser.program();
            return (Map<String, JingleDescriptor>) processor.visit(tree);
        } catch (IOException | RecognitionException e) {
            throw new RuntimeException("File processing error", e);
        }
    }

    private static class JinglesTreeProcessor extends JingleBellsBaseVisitor<Object> {

        private Map<String, JingleDescriptor> descriptors = new HashMap<>();

        @Override
        public Map<String, JingleDescriptor> visitProgram(JingleBellsParser.ProgramContext ctx) {
            for (JingleBellsParser.DeclarationContext declaration : ctx.declaration()) {
                String name = declaration.NAME().getText();
                descriptors.put(
                        name,
                        new JingleDescriptor(name, Types.resolveType(declaration.TYPE().getText())));
            }
            visitChildren(ctx); // this invokation will visit all declarations one by one
            return descriptors;
        }

        @Override
        public JingleDescriptor visitDeclaration(JingleBellsParser.DeclarationContext ctx) {
            JingleDescriptor descriptor = descriptors.get(ctx.NAME().getText());

            // TODO think what happens if there are no default value
            FormulaTree.Value defaultValue = (FormulaTree.Value) visit(ctx.defaultValue());
            descriptor.defaultValue = defaultValue;

            long currentCalcIndex = 0;
            List<CalculatorDescriptor> calculators = new ArrayList<>();
            for (JingleBellsParser.CalculatorContext calc : ctx.calculator()) {
                String calcName = CALCULATOR_PREFIX + currentCalcIndex;
                calculators.add(processCalculator(calc, calcName, descriptor.type));
                currentCalcIndex++;
            }
            descriptor.calculators = calculators;
            
            return descriptor;
        }

        @Override
        public FormulaTree.Value visitIntValue(JingleBellsParser.IntValueContext ctx) {
            return new FormulaTree.Value(INT, ctx.INTEGER().getText(), ctx.minus != null);
        }

        @Override
        public FormulaTree.Value visitFloatValue(JingleBellsParser.FloatValueContext ctx) {
            return new FormulaTree.Value(FLOAT, ctx.FLOAT().getText(), ctx.minus != null);
        }

        private CalculatorDescriptor processCalculator(
                JingleBellsParser.CalculatorContext ctx,
                String calcName,
                FormulaTree.Type type) {
            Set<String> sources = visitNameList(ctx.nameList());
            FormulaTree formula = (FormulaTree) visit(ctx.expression());
            formula.desiredType = type;
            return new CalculatorDescriptor(calcName, sources, formula);
        }

        @Override
        public Set<String> visitNameList(JingleBellsParser.NameListContext ctx) {
            Set<String> sources = new HashSet<>();
            // TODO check if source is alrady exists in other calculator
            for (TerminalNode nameNode : ctx.NAME()) {
                sources.add(nameNode.getText());
            }
            return sources;
        }

        //
        // Expressions
        //
        @Override
        public TheTree visitExpOpAddSub(JingleBellsParser.ExpOpAddSubContext ctx) {
            FormulaTree left = (FormulaTree) visit(ctx.left);
            FormulaTree right = (FormulaTree) visit(ctx.right);
            FormulaTree.Operation operation = "+".equals(ctx.operation.getText()) ? ADD : SUB;
            FormulaTree.Type type = INT;
            if (left.type == FLOAT || right.type == FLOAT) {
                type = FLOAT;
                left.desiredType = FLOAT;
                right.desiredType = FLOAT;
            } else {
                left.desiredType = INT;
                right.desiredType = INT;
            }
            return new FormulaTree.BinaryOperation(operation, type, left, right);
        }

        @Override
        public FormulaTree visitExpIntValue(JingleBellsParser.ExpIntValueContext ctx) {
            return new FormulaTree.Value(INT, ctx.INTEGER().getText());
        }

        /**
         * This method is the place for NEG chain reduction
         *
         * @param ctx parse context
         * @return newly created node
         */
        @Override
        public TheTree visitExpOpNeg(JingleBellsParser.ExpOpNegContext ctx) {
            FormulaTree operand = (FormulaTree) visit(ctx.expression());
            if (operand.operation == VALUE) {
                ((FormulaTree.Value) operand).invert();
                return operand;
            } else if (operand.operation == NEG) {
                return ((FormulaTree.UnaryOperation) operand).operand;
            }
            return new FormulaTree.UnaryOperation(operand.type, operand);
        }

        @Override
        public FormulaTree visitExpJingleName(JingleBellsParser.ExpJingleNameContext ctx) {
            String name = ctx.NAME().getText();
            JingleDescriptor descriptor = descriptors.get(name);
            if (descriptor == null) {
                // TODO report position in source
                throw new RuntimeException("Program does not contain jingle " + name);
            }
            return new FormulaTree.AcccessJingle(descriptor.type, name);
        }

        @Override
        public TheTree visitExpOpMulDiv(JingleBellsParser.ExpOpMulDivContext ctx) {
            FormulaTree left = (FormulaTree) visit(ctx.left);
            FormulaTree right = (FormulaTree) visit(ctx.right);
            String op = ctx.operation.getText();
            FormulaTree.Operation operation = "*".equals(op) ? MUL : ("/".equals(op) ? DIV : REM);
            FormulaTree.Type type = INT;
            if (left.type == FLOAT || right.type == FLOAT) {
                type = FLOAT;
                left.desiredType = FLOAT;
                right.desiredType = FLOAT;
            } else {
                left.desiredType = INT;
                right.desiredType = INT;
            }
            return new FormulaTree.BinaryOperation(operation, type, left, right);
        }

    }

}
