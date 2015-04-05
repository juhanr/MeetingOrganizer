package ee.juhan.meetingorganizer.models;

public class Date {
    private int day;
    private int month;
    private int year;

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @Override
    public String toString() {
        return day + "." + getString(month) + "." + year;
    }


    private String getString(int x) {
        if (x < 10)
            return "0" + x;
        return "" + x;
    }
}
