package Lib;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OurDate {
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
    private Date m_Date;

    public OurDate(String dateString) throws ParseException {
        this.m_Date = formatter.parse(dateString);
    }

    public OurDate() {
        this.m_Date = new Date();
    }

    public OurDate(OurDate date) throws ParseException {
        m_Date =formatter.parse(date.toString());
    }

    @Override
    public String toString() {
        return formatter.format(m_Date);
    }

    public Date getDate() {
        return m_Date;
    }
}
