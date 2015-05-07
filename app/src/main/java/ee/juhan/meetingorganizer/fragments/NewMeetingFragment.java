package ee.juhan.meetingorganizer.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.util.DateParserUtil;

public class NewMeetingFragment extends Fragment {

    private static MeetingDTO newMeetingModel = new MeetingDTO();
    private String title;
    private MainActivity activity;
    private ViewGroup newMeetingLayout;

    public NewMeetingFragment() {

    }

    public static MeetingDTO getNewMeetingModel() {
        return NewMeetingFragment.newMeetingModel;
    }

    public static void setNewMeetingModel(MeetingDTO meetingModel) {
        NewMeetingFragment.newMeetingModel = meetingModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        title = getString(R.string.title_new_meeting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        activity.setDrawerItem(activity.getDrawerItemPosition(title));
        newMeetingLayout = (ViewGroup) inflater.inflate(R.layout.fragment_new_meeting, container, false);
        setButtonListeners();
        setSavedData();
        return newMeetingLayout;
    }

    private void setButtonListeners() {
        TextView dateButton = (TextView) newMeetingLayout
                .findViewById(R.id.date_button);
        TextView startTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.start_time_button);
        TextView endTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.end_time_button);
        Button continueButton = (Button) newMeetingLayout
                .findViewById(R.id.continue_button);

        dateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener onDateSetListener =
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth + "." + formatString(monthOfYear + 1) + "." + year;
                                setDate(date);
                            }
                        };
                Calendar c = Calendar.getInstance();
                if (!getViewText(R.id.date_button).equals(getString(R.string.textview_not_set_u))) {
                    c.setTime(DateParserUtil.parseDate(getViewText(R.id.date_button)));
                }
                DatePickerDialog dialog = new DatePickerDialog(activity,
                        DatePickerDialog.THEME_HOLO_DARK, onDateSetListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        startTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener =
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time = formatString(hourOfDay) + ":" + formatString(minute);
                                setStartTime(time);
                            }
                        };
                Calendar c = Calendar.getInstance();
                if (!getViewText(R.id.start_time_button).equals(getString(R.string.textview_not_set_u))) {
                    c.setTime(DateParserUtil.parseTime(getViewText(R.id.start_time_button)));
                }
                TimePickerDialog dialog = new TimePickerDialog(activity,
                        TimePickerDialog.THEME_HOLO_DARK, onTimeSetListener,
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        endTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener =
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time = formatString(hourOfDay) + ":" + formatString(minute);
                                setEndTime(time);
                            }
                        };
                Calendar c = Calendar.getInstance();
                if (!getViewText(R.id.end_time_button).equals(getString(R.string.textview_not_set_u))) {
                    c.setTime(DateParserUtil.parseTime(getViewText(R.id.end_time_button)));
                }
                TimePickerDialog dialog = new TimePickerDialog(activity,
                        TimePickerDialog.THEME_HOLO_DARK, onTimeSetListener,
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                dialog.show();
            }
        });

        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                if (isValidData()) {
                    activity.changeFragment(new ChooseLocationFragment());
                }
            }
        });

    }

    private String formatString(int n) {
        if (n < 10)
            return "0" + n;
        else
            return "" + n;
    }

    private void setSavedData() {
        if (newMeetingModel.getTitle() != null) {
            setViewText(R.id.title_textbox, newMeetingModel.getTitle());
            setViewText(R.id.description_textbox, newMeetingModel.getDescription());
            setViewText(R.id.date_button, underlineString(
                    DateParserUtil.formatDate(newMeetingModel.getStartDateTime())));
            setViewText(R.id.start_time_button, underlineString(
                    DateParserUtil.formatTime(newMeetingModel.getStartDateTime())));
            setViewText(R.id.end_time_button, underlineString(
                    DateParserUtil.formatTime(newMeetingModel.getEndDateTime())));
        }
    }

    private boolean isValidData() {
        if (getViewText(R.id.title_textbox).length() == 0) {
            activity.showToastMessage(getString(R.string.toast_please_enter_title));
        } else if (getViewText(R.id.date_button).equals(getString(R.string.textview_not_set_u))) {
            activity.showToastMessage(getString(R.string.toast_please_set_date));
        } else if (getViewText(R.id.start_time_button).equals(getString(R.string.textview_not_set_u))) {
            activity.showToastMessage(getString(R.string.toast_please_set_start_time));
        } else if (getViewText(R.id.end_time_button).equals(getString(R.string.textview_not_set_u))) {
            activity.showToastMessage(getString(R.string.toast_please_set_end_time));
        } else if (newMeetingModel.getStartDateTime().before(new Date())) {
            activity.showToastMessage(getString(R.string.toast_start_time_future));
        } else if (newMeetingModel.getStartDateTime().after(newMeetingModel.getEndDateTime())) {
            activity.showToastMessage(getString(R.string.toast_end_time_after_start));
        } else {
            return true;
        }
        return false;
    }

    private String getViewText(int viewId) {
        View view = newMeetingLayout.findViewById(viewId);
        if (view instanceof EditText)
            return ((EditText) view).getText().toString().trim();
        else if (view instanceof TextView)
            return ((TextView) view).getText().toString().trim();
        else return null;
    }

    private void setViewText(int viewId, Spanned text) {
        View view = newMeetingLayout.findViewById(viewId);
        if (view instanceof EditText)
            ((EditText) view).setText(text);
        else if (view instanceof TextView)
            ((TextView) view).setText(text);
    }

    private void setViewText(int viewId, String text) {
        setViewText(viewId, Html.fromHtml(text));
    }

    private void saveData() {
        newMeetingModel.setTitle(getViewText(R.id.title_textbox));
        newMeetingModel.setDescription(getViewText(R.id.description_textbox));
        newMeetingModel.setStartDateTime(DateParserUtil.parseDateTime(
                getViewText(R.id.date_button) + " " +
                        getViewText(R.id.start_time_button)));
        newMeetingModel.setEndDateTime(DateParserUtil.parseDateTime(
                getViewText(R.id.date_button) + " " +
                        getViewText(R.id.end_time_button)));

    }

    private void setDate(String date) {
        TextView dateButton = (TextView) newMeetingLayout
                .findViewById(R.id.date_button);
        dateButton.setText(underlineString(date));
    }

    private void setStartTime(String time) {
        TextView startTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.start_time_button);
        startTimeButton.setText(underlineString(time));
    }

    private void setEndTime(String time) {
        TextView endTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.end_time_button);
        endTimeButton.setText(underlineString(time));
    }

    private Spanned underlineString(String s) {
        return Html.fromHtml("<u>" + s + "</u>");
    }

}
