package jua.jingle.core.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jua.jingle.core.compiler.tree.JingleDescriptor;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.calculus.jingle.JingleFloat;
import jua.jingle.core.calculus.jingle.JingleInt;
import jua.jingle.core.compiler.tree.CalculatorDescriptor;
import jua.jingle.core.compiler.tree.FormulaTree;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.FLOAT;
import static jua.jingle.core.compiler.tree.FormulaTree.Type.INT;
import jua.jingle.core.compiler.util.JingleClassLoader;
import jua.jingle.core.compiler.util.Types;
import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DNEG;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;
import org.objectweb.asm.Type;

/**
 * Generator should not do something with the tree, just generate the code
 * as it's defined by the tree. For example, it should not reduce NEG sequence.
 */
public class JingleGenerator {

    static final String PATH = "jb/";
    static final String NAME_DELIMITER = "_";

    JingleDescriptor descriptor;
    private Class<? extends Jingle> base;
    private String className;
    private ClassWriter classw;
    JingleClassLoader loader;

    
    public static Jingle generate(JingleDescriptor descriptor) {
        return new JingleGenerator(descriptor).generate();
    }

    private JingleGenerator(JingleDescriptor descriptor) {
        this.descriptor = descriptor;
        this.base = Types.baseClass(descriptor.type);
        this.className = base.getSimpleName() + NAME_DELIMITER + descriptor.name;
        this.classw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    }

    private Jingle generate() {
        try {
            generateClass();
                // DEBUG : TODO think how to debug class generation
                loader.save(className, classw.toByteArray());
            return (Jingle) loader.loadClass(classw.toByteArray()).getConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot load class " + descriptor.name);
        }
    }

    private void generateClass() {
        // create class
        classw.visit(V11, ACC_PUBLIC, PATH + className, null, Type.getInternalName(base), null);

        // create value field
        // defaultValue should be initialized in constructor because field is not static
        classw.visitField(ACC_PUBLIC, "value", Types.typeDescriptor(descriptor.type), null, null).visitEnd();

        generateConstructor();
        
        // generate Calclators
        for (CalculatorDescriptor calcDescriptor : descriptor.calculators) {
            // TODO codegen
            generateCalculator(calcDescriptor);
        }
        
        // finish class
        classw.visitEnd();
    }

    private void generateConstructor() {
        MethodVisitor initv = classw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);

        // invoke super()
        initv.visitCode();
        initv.visitVarInsn(ALOAD, 0);
        initv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(base), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        // set name
        initv.visitVarInsn(ALOAD, 0);
        initv.visitLdcInsn(descriptor.name);
        initv.visitFieldInsn(PUTFIELD, Type.getInternalName(Jingle.class), "name", Type.getDescriptor(String.class));

        // set default value
        if (descriptor.defaultValue != null) {
            initv.visitVarInsn(ALOAD, 0);
            switch (descriptor.type) {
                case INT:
                    initv.visitLdcInsn(retrieveIntValue(descriptor.defaultValue));
                    initv.visitFieldInsn(PUTFIELD, Type.getInternalName(base), "value", Type.LONG_TYPE.getDescriptor());
                    break;
                case FLOAT:
                    initv.visitLdcInsn(retrieveFloatValue(descriptor.defaultValue));
                    initv.visitFieldInsn(PUTFIELD, Type.getInternalName(base), "value", Type.DOUBLE_TYPE.getDescriptor());
                    break;
            }
        }

        // finish
        initv.visitInsn(RETURN);
        initv.visitMaxs(0, 0);
        initv.visitEnd();
    }

    private long retrieveIntValue(FormulaTree.Value value) {
        long longValue = Long.valueOf(value.value);
        if (value.negative) {
            return -longValue;
        }
        return longValue;
    }

    private double retrieveFloatValue(FormulaTree.Value value) {
        double doubleValue = Double.valueOf(value.value);
        if (value.negative) {
            return -doubleValue;
        }
        return doubleValue;
    }

    private void generateCalculator(CalculatorDescriptor calcDescriptor){
        // TODO generate calculator method
        MethodVisitor calcv = classw.visitMethod(
                ACC_PUBLIC,
                calcDescriptor.calcName,
                Type.getMethodDescriptor(Type.VOID_TYPE),
                null, 
                null);
        calcv.visitCode();
        calcv.visitVarInsn(ALOAD, 0); // because of void return

        generateCalculatorBody(calcDescriptor.formula, calcv);

        // save result to "value" and finish calculator
        switch (descriptor.type) {
            case INT:
                calcv.visitFieldInsn(PUTFIELD, Type.getInternalName(base), "value", Type.LONG_TYPE.getDescriptor());
                break;
            case FLOAT:
                calcv.visitFieldInsn(PUTFIELD, Type.getInternalName(base), "value", Type.FLOAT_TYPE.getDescriptor());
                break;
        }
        calcv.visitInsn(RETURN);
        calcv.visitMaxs(0, 0);
        calcv.visitEnd();

    }

    // Recursive function with Tree visiting
    private void generateCalculatorBody(FormulaTree node, MethodVisitor calcv) {
        switch (node.operation) {
            // TODO operation generations with subtree visiting
            case VALUE:
                generateValueAccess((FormulaTree.Value) node, calcv);
                break;
            case JINGLE:
                generateJingleAccess((FormulaTree.AcccessJingle) node, calcv);
                break;
            case NEG:
                generateNegOperation((FormulaTree.UnaryOperation) node, calcv);
                break;
            case ADD:
            case REM:
            case MUL:
            case DIV:
            case SUB:
                generateBinaryOperation((FormulaTree.BinaryOperation) node, calcv);
                break;
        }
    }

    private void generateValueAccess(FormulaTree.Value node, MethodVisitor calcv) {
        try {
            switch (node.desiredType) {
                case INT:
                    long longValue = Long.valueOf(node.value);
                    if (node.negative) {
                        longValue = - longValue;
                    }
                    calcv.visitLdcInsn(longValue);
                    break;
                case FLOAT:
                    double doubleValue = Double.valueOf(node.value);
                    if (node.negative) {
                        doubleValue = - doubleValue;
                    }
                    calcv.visitLdcInsn(doubleValue);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert value " + node.value + " to type " + node.desiredType);
        }
    }

    private void generateJingleAccess(FormulaTree.AcccessJingle node, MethodVisitor calcv) {
        switch (node.type) {
            case INT:
                retrieveNumberField(
                        node.name,
                        descriptor.name.equals(node.name),
                        JingleInt.class,
                        Type.LONG_TYPE,
                        calcv);
                if (FormulaTree.Type.FLOAT.equals(node.desiredType)) {
                    calcv.visitInsn(D2L);
                }
                break;
            case FLOAT:
                retrieveNumberField(
                        node.name,
                        descriptor.name.equals(node.name),
                        JingleFloat.class,
                        Type.DOUBLE_TYPE,
                        calcv);
                if (FormulaTree.Type.INT.equals(node.desiredType)) {
                    calcv.visitInsn(L2D);
                }
                break;
        }
    }

    private void retrieveNumberField(String name, boolean thisJingle, Class<? extends Jingle> base, Type type, MethodVisitor calcv) {
        calcv.visitVarInsn(ALOAD, 0);
        if (thisJingle) {
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(base), "value", type.getDescriptor());
        } else {
            // 6: getfield      #5                  // Field all:Ljava/util/Map;
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(base), "all", Type.getDescriptor(Map.class));
            // 9: ldc           #6                  // String 1
            calcv.visitLdcInsn(name);
            // 11: invokeinterface #18,  2           // InterfaceMethod java/util/Map.get:(Ljava/lang/Object;)Ljava/lang/Object;
            calcv.visitMethodInsn(
                    INVOKEINTERFACE,
                    Type.getInternalName(Map.class),
                    "get",
                    Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class)),
                    true);
            // 16: checkcast     #19                 // class Base
            calcv.visitTypeInsn(CHECKCAST, Type.getInternalName(base));
            // 19: getfield      #20                 // Field Base.value:J
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(base), "value", type.getDescriptor());
        }
    }

    private void generateNegOperation(FormulaTree.UnaryOperation node, MethodVisitor calcv) {
        switch (node.type) {
            case INT:
                calcv.visitInsn(LNEG);
                if (FormulaTree.Type.FLOAT.equals(node.desiredType)) {
                    calcv.visitInsn(L2D);
                }
                break;
            case FLOAT:
                calcv.visitInsn(DNEG);
                if (FormulaTree.Type.INT.equals(node.desiredType)) {
                    calcv.visitInsn(D2L);
                }
                break;
            }
    }

    private void generateBinaryOperation(FormulaTree.BinaryOperation node, MethodVisitor calcv) {
        generateCalculatorBody(node.left, calcv);
        generateCalculatorBody(node.right, calcv);

        switch (node.type) {
            case INT:
                switch (node.operation) {
                    case ADD:
                        calcv.visitInsn(LADD);
                        break;
                    case REM:
                        calcv.visitInsn(LREM);
                        break;
                    case MUL:
                        calcv.visitInsn(LMUL);
                        break;
                    case DIV:
                        calcv.visitInsn(LDIV);
                        break;
                    case SUB:
                        calcv.visitInsn(LSUB);
                        break;
                }
                if (FormulaTree.Type.FLOAT.equals(node.desiredType)) {
                    calcv.visitInsn(L2D);
                }
                break;
            case FLOAT:
                switch (node.operation) {
                    case ADD:
                        calcv.visitInsn(DADD);
                        break;
                    case REM:
                        calcv.visitInsn(DREM);
                        break;
                    case MUL:
                        calcv.visitInsn(DMUL);
                        break;
                    case DIV:
                        calcv.visitInsn(DDIV);
                        break;
                    case SUB:
                        calcv.visitInsn(DSUB);
                        break;
                }
                if (FormulaTree.Type.INT.equals(node.desiredType)) {
                    calcv.visitInsn(D2L);
                }
                break;
        }
    }

}
