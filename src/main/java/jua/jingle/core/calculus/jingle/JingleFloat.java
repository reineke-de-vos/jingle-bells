package jua.jingle.core.calculus.jingle;

public class JingleFloat extends Jingle {

    public double value;

    @Override
    public void load(String value) throws ValueException {
        try {
            this.value = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ValueException("Illegal integer value: " + value);
        }
    }

    @Override
    public String format() {
        return String.format(format, value);
    }

}
