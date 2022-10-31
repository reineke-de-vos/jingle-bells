package jua.jingle.core.compiler;

import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class JingleDescriptor {

    public String name;
    public JingleType type;
    public ClassWriter classw;

    public JingleDescriptor(String name, JingleType type) {
        this.name = name;
        this.type = type;
        this.classw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    }

}
