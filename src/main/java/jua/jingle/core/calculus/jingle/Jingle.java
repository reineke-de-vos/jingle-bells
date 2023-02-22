package jua.jingle.core.calculus.jingle;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jua.jingle.core.calculus.calculator.Calculator;

public abstract class Jingle {
    
    public String name;
    public boolean listen = false;

    public static final String TOUCH = "";

    protected String format;

    protected Map<String, Jingle> all;
    protected Set<String> sources = new HashSet<>();
    protected Map<String, Calculator> calculators = new HashMap<>(); // TODO think to handle calculators outside too

    // TODO change init to constructor (condifer performance)
    public void init(
            Map<String, Jingle> jingles,
            Map<String, Set<String>> dependencies,
            String format) {
        this.all = jingles;
        if (format != null) {
            this.format = format;
        }
        initCalculators(dependencies);
    }

    /**
     * Set value from external sources, specific field should parse it from string
     * @param value 
     */
    abstract public void load(String value) throws ValueException;

    // TODO return boolean to detect that calculator was called (will work with predicate implementation)
    public void onBell(String name, long ttl) {
        calculators.get(name).calculate();
    }

    abstract public String format();

    public Set<String> getSources() {
        return sources;
    }

    protected void echoError(String error) {
        // TODO report through bellTower and Bell, probably modify ECHO action to bi-directional
        System.out.println();
    }

    protected void initCalculators(Map<String, Set<String>> dependencies) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> thisClass = this.getClass();
        MethodType calculateMethod = MethodType.methodType(void.class);
        MethodType invokedType = MethodType.methodType(Calculator.class, thisClass);

        try {
            for (String calculatorName : dependencies.keySet()) {                
                Calculator calculator = (Calculator) LambdaMetafactory.metafactory(
                        lookup,
                        "calculate",
                        invokedType,
                        calculateMethod,
                        lookup.findVirtual(thisClass, calculatorName, calculateMethod), calculateMethod).getTarget().invoke(this);
                for (String name : dependencies.get(calculatorName)) {
                    sources.add(name);
                    calculators.put(name, calculator);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot initiate calculators", t);
        }
    }
}
