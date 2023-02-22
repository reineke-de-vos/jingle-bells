package jua.jingle.core.compiler.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

public class JingleClassLoader {

    private ClassLoader loader;
    private Method definer;

    public JingleClassLoader() throws NoSuchMethodException {
        ClassLoader.class.getModule().addOpens(ClassLoader.class.getPackageName(), this.getClass().getModule());
        loader = ClassLoader.getSystemClassLoader();
        definer = ClassLoader.class.getDeclaredMethod(
                "defineClass",
                new Class[] { String.class, byte[].class, int.class, int.class });
        definer.setAccessible(true); // TODO consider set to false after all classes are instantiated
    }

    public Class<?> loadClass(byte[] bytes) throws Exception {
        return (Class<?>) definer.invoke(loader, new Object[] { null, bytes, 0, bytes.length });
    }

    public void save(String className, byte[] classBytes) {
        try {
            File classFile = new File(className + ".class");
            Files.write(classFile.toPath(), classBytes);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save class " + className);
        }
    }

}
