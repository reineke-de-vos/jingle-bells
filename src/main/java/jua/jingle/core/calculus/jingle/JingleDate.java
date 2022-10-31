package jua.jingle.core.calculus.jingle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JingleDate extends JingleObject<Date> {

    @Override
    public void load(String value) throws ValueException {
        try {
            this.value = DateFormat.getInstance().parse(value);
        } catch (ParseException ex) {
            throw new ValueException("Illegal date format: " + value);
        }
    }

    @Override
    public String format() {
        return new SimpleDateFormat(format).format(value);
    }

}
