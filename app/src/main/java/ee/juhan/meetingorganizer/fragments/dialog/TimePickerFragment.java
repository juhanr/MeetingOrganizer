package ee.juhan.meetingorganizer.fragments.dialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private static int hour;
    private static int minute;

    public TimePickerFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (hour == -1) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), DatePickerDialog.THEME_HOLO_DARK,
                this, hour, minute, true);
        dialog.setTitle("Choose a time");
        return dialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }

}