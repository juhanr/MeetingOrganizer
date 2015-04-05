package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import ee.juhan.meetingorganizer.models.Time;

@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private int viewId;
    private static int hour = 12;
    private static int minute = 0;

    public TimePickerFragment(int viewId) {
        this.viewId = viewId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), DatePickerDialog.THEME_HOLO_DARK,
                this, hour, minute, true);
        dialog.setTitle("Choose a time");
        return dialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        NewMeetingFragment.changeTime(new Time(hourOfDay, minute), viewId);
    }

}