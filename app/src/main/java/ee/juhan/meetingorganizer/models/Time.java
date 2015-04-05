package ee.juhan.meetingorganizer.models;

public class Time {
    private int hour;
    private int minute;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public String toString() {
        return hour + ":" + getString(minute);
    }

    private String getString(int x) {
        if (x < 10)
            return "0" + x;
        return "" + x;
    }
}
