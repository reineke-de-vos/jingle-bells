package jua.jingle.core.calculus.jingle;

public class JingleInt extends Jingle {

    public long value;

    @Override
    public void load(String value) throws ValueException {
        try {
            this.value = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValueException("Illegal integer value: " + value);
        }
    }

    @Override
    public String format() {
        return String.format("%d", value);
    }

}
