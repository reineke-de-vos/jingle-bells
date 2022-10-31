package jua.jingle.core.compiler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jua.jingle.core.calculus.jingle.Jingle;
import jua.jingle.core.calculus.jingle.JingleInt;
import jua.jingle.core.parser.JingleBellsBaseListener;
import jua.jingle.core.parser.JingleBellsParser;
import jua.jingle.core.server.Bell;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;
import org.objectweb.asm.Type;

/**
 * Class structure:
 *  1. constructor ?
 *  2. calculate methods
 */
public class JinglesGenerator extends JingleBellsBaseListener {

    private ClassLoader cloader;
    private Method loader;

    private Map<String, JingleDescriptor> descriptors = new HashMap<>();
    private Map<String, Jingle> jingles = new HashMap<>();

    public JinglesGenerator() throws Exception {
        ClassLoader.class.getModule().addOpens(ClassLoader.class.getPackageName(), this.getClass().getModule());
        cloader = ClassLoader.getSystemClassLoader();
        loader = ClassLoader.class
                .getDeclaredMethod(
                        "defineClass",
                        new Class[] { String.class, byte[].class, int.class, int.class });
        loader.setAccessible(true); // TODO consider set to false after all classes are instantiated
    }

    public Map<String, Jingle> jingles() {
        return jingles; // TODO consider illegal state exception if jingles are not ready yet
    }

    @Override
    public void enterProgram(JingleBellsParser.ProgramContext ctx) {
        for (JingleBellsParser.DeclarationContext declaration : ctx.declaration()) {
            String name = declaration.NAME().getText();
            descriptors.put(
                    name,
                    new JingleDescriptor(name, JingleType.resolveType(declaration.TYPE().getText())));
        }
    }
    
    @Override
    public void exitProgram(JingleBellsParser.ProgramContext ctx) {
        // TODO probably do not need to do something
    }

    // internal block
    JingleDescriptor desc;
    private String className;
    private Class<?> parent;
    private MethodVisitor initv; // init (constructor)
    private Set<String> sources;
    private Map<Long, Set<String>> calcDeps;

    @Override
    public void enterDeclaration(JingleBellsParser.DeclarationContext ctx) {
        desc = descriptors.get(ctx.NAME().getText());

        // start class
        parent = desc.type.parentClass();
        className = parent.getSimpleName() + "_" + desc.name;
        desc.classw.visit(V11, ACC_PUBLIC, "jb/" + className, null, Type.getInternalName(parent), null);

        sources = new HashSet<>();
        calcDeps = new HashMap<>();

        // create value field
        // defaultValue should be initialized in constructor because field is not static
        // TODO choose type accurately for different jingle types
        desc.classw.visitField(ACC_PUBLIC, "value", Type.LONG_TYPE.getDescriptor(), null, null).visitEnd();

        // start constructor
        initv = desc.classw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        initv.visitCode();
        initv.visitVarInsn(ALOAD, 0);
        initv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(parent), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        initv.visitVarInsn(ALOAD, 0);
        initv.visitLdcInsn(desc.name);
        initv.visitFieldInsn(PUTFIELD, Type.getInternalName(Jingle.class), "name", Type.getDescriptor(String.class));
    }

    @Override
    public void exitDeclaration(JingleBellsParser.DeclarationContext ctx) {
        checkNegative(null);
        // finish <init>
        initv.visitInsn(RETURN);
        initv.visitMaxs(0, 0);
        initv.visitEnd();

        // finish class
        desc.classw.visitEnd();

        instantiateCurrent();

        // DEBUG
        save();
    }

    private void instantiateCurrent() {
        try {
            Jingle jingle = (Jingle) loadClass(desc.classw.toByteArray())
                    .getConstructor()
                    .newInstance();
            jingles.put(desc.name, jingle);
            jingle.init(jingles, sources, calcDeps, null);
        } catch (Exception e) {
                throw new RuntimeException("Instantiation problem", e);
        }
    }

    @Override
    public void enterDefaultValue(JingleBellsParser.DefaultValueContext ctx) {
        // TODO do not do it if there are no default value!!!
        initv.visitVarInsn(ALOAD, 0);
        // TODO actual type and value for different jingle types
        initv.visitLdcInsn(Long.valueOf(ctx.INTEGER().getText()));
        initv.visitFieldInsn(PUTFIELD, Type.getInternalName(parent), "value", Type.LONG_TYPE.getDescriptor());
    }

    private Long calcNumber = 0L;
    private Set<String> calcSources;
    private MethodVisitor calcv; // current calc method

    @Override
    public void enterCalculator(JingleBellsParser.CalculatorContext ctx) {
        calcNumber = calcNumber == 0 ? 0 : calcNumber + 1;
        calcSources = new HashSet<>();

        calcv = desc.classw.visitMethod(
                        ACC_PUBLIC,
                        "calculator_" + calcNumber,
                        Type.getMethodDescriptor(Type.VOID_TYPE),
                        null,
                        null);
        calcv.visitCode();
        calcv.visitVarInsn(ALOAD, 0); // because of void return
    }

    @Override
    public void exitCalculator(JingleBellsParser.CalculatorContext ctx) {
        calcv.visitFieldInsn(PUTFIELD, Type.getInternalName(parent), "value", Type.LONG_TYPE.getDescriptor());

        calcv.visitInsn(RETURN);
        calcv.visitMaxs(0, 0);
        calcv.visitEnd();

    }

    @Override
    public void enterNameList(JingleBellsParser.NameListContext ctx) {
        for (TerminalNode nameNode : ctx.NAME()) {
            String name = nameNode.getText();
            sources.add(name);
            calcSources.add(name);
        }
        if (calcNumber == 0) {
            // default calculator for touch command
            calcSources.add(Jingle.TOUCH);
        }
        calcDeps.put(calcNumber, calcSources);
    }

    //
    // Expression generation
    //

    @Override
    public void exitExpOpSumSub(JingleBellsParser.ExpOpSumSubContext ctx) {
        checkNegative(null);
        switch (ctx.operation.getText()) {
            case "+":
                calcv.visitInsn(LADD);
                break;
            case "-":
                calcv.visitInsn(LSUB);
        }
    }

    @Override
    public void exitExpOpNeg(JingleBellsParser.ExpOpNegContext ctx) {
        neg += 1;
    }

    @Override
    public void exitExpJingleName(JingleBellsParser.ExpJingleNameContext ctx) {
        checkNegative(null);
        retrieveField(ctx.NAME().getText());
    }

    @Override
    public void exitExpOpMulDiv(JingleBellsParser.ExpOpMulDivContext ctx) {
        checkNegative(null);
        switch(ctx.operation.getText()) {
            case "*":
                calcv.visitInsn(LMUL);
                break;
            case "/":
                calcv.visitInsn(LDIV);
                break;
            case "%":
                calcv.visitInsn(LREM);
                break;
        }
    }

    @Override
    public void exitExpValue(JingleBellsParser.ExpValueContext ctx) {
        checkNegative(Long.valueOf(ctx.INTEGER().getText()));
    }

    //
    // Gen utils
    //

    long neg = 0;
    Long store = null;
    private void checkNegative(Long value) {
        if (store != null) {
            calcv.visitLdcInsn(neg % 2 == 1 ? - store : store);
        } else {
            if (neg % 2 == 1) {
                calcv.visitInsn(LNEG);
            }
        }
        neg = 0;
        store = value;
    }

    private void retrieveField(String name) {
        calcv.visitVarInsn(ALOAD, 0);
        if (desc.name.equals(name)) {
            // TODO choose type accurately for different jingle types
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(parent), "value", Type.LONG_TYPE.getDescriptor());
        } else {
            // get value from all map
            // 6: getfield      #5                  // Field all:Ljava/util/Map;
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(parent), "all", Type.getDescriptor(Map.class));
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
            calcv.visitTypeInsn(CHECKCAST, Type.getInternalName(JingleInt.class));
            // 19: getfield      #20                 // Field Base.value:J
            calcv.visitFieldInsn(GETFIELD, Type.getInternalName(JingleInt.class), "value", Type.LONG_TYPE.getDescriptor());
        }
    }

    //
    // Infrastructure
    //

    private Class<?> loadClass(byte[] bytes) throws Exception {
        return (Class<?>) loader.invoke(cloader, new Object[] { null, bytes, 0, bytes.length });
    }

    private void save() {
        try {
            File f = new File(className + ".class");
            Files.write(f.toPath(), desc.classw.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Cannot save class " + className);
        }
    }

}
