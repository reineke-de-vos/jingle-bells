package jua.jingle.core.calculus.jingle;

public class JingleFloat extends Jingle {

    public double value;

    @Override
    public void load(String value) throws ValueException {
        try {
            this.value = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ValueException("Illegal float value: " + value);
        }
    }

    @Override
    public String format() {
        return String.format("%f", value);
    }

}
