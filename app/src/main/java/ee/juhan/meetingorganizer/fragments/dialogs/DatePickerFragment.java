package ee.juhan.meetingorganizer.fragments.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import ee.juhan.meetingorganizer.R;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static int year;
    private static int month;
    private static int day;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (year == 0) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                DatePickerDialog.THEME_HOLO_DARK, this, year, month, day);
        dialog.setTitle(getString(R.string.textview_choose_date));
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

    }

}