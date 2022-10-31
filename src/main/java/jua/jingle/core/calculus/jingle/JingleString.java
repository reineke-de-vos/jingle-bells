package jua.jingle.core.calculus.jingle;

public class JingleString extends JingleObject<String> {

    @Override
    public void load(String value) {
        this.value = value;
    }

    @Override
    public String format() {
        return value;
    }

}
