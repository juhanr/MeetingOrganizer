package ee.juhan.meetingorganizer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.Spinner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.interfaces.SnackbarActivity;
import ee.juhan.meetingorganizer.models.server.LocationChoice;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.services.LocationService;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.GsonUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewMeetingActivity extends AppCompatActivity implements SnackbarActivity {

	private static final int QUICK_MEETING_DURATION_HOURS = 2;
	private static Meeting newMeetingModel = new Meeting();
	private ViewGroup newMeetingLayout;
	private View progressView;
	private Activity activity = this;

	public static Meeting getNewMeetingModel() {
		return NewMeetingActivity.newMeetingModel;
	}

	public static void setNewMeetingModel(Meeting meetingModel) {
		NewMeetingActivity.newMeetingModel = meetingModel;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			newMeetingModel = GsonUtil.getJsonObjectFromBundle(savedInstanceState, Meeting.class,
					Meeting.class.getSimpleName());
		}
		setContentView(R.layout.activity_new_meeting);
		setTitle(getString(R.string.title_new_meeting));
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		newMeetingLayout = (ViewGroup) findViewById(R.id.layout_content);
		progressView = findViewById(R.id.progress_bar);
		setButtonListeners();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState = GsonUtil.addJsonObjectToBundle(savedInstanceState, newMeetingModel,
				newMeetingModel.getClass().getSimpleName());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onPause() {
		saveData();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		addLeaderParticipant();
		setSavedDataViews();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	@Override
	public void showSnackbar(String message) {
		UIUtil.showSnackBar(newMeetingLayout, message);
	}

	private void setButtonListeners() {
		String[] items = new String[]{getString(R.string.new_meeting_touch_to_set)};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, items);
		adapter.setDropDownViewResource(R.layout.row_spn_dropdown);

		Spinner dateButton = (Spinner) newMeetingLayout.findViewById(R.id.spn_new_date);
		Spinner startTimeButton = (Spinner) newMeetingLayout.findViewById(R.id.spn_new_start_time);
		Spinner endTimeButton = (Spinner) newMeetingLayout.findViewById(R.id.spn_new_end_time);
		Spinner locationInfo = (Spinner) newMeetingLayout.findViewById(R.id.spn_new_location);
		Spinner participantsInfo =
				(Spinner) newMeetingLayout.findViewById(R.id.spn_new_participants);
		FloatingActionButton createButton = (FloatingActionButton) findViewById(R.id.fab_confirm);
		CheckBox quickMeetingCheckBox = (CheckBox) findViewById(R.id.chk_quick_meeting);
		com.rey.material.widget.EditText descriptionEditText =
				(com.rey.material.widget.EditText) findViewById(R.id.edt_description);

		dateButton.setAdapter(adapter);
		dateButton.setClickable(false);
		startTimeButton.setAdapter(adapter);
		startTimeButton.setClickable(false);
		endTimeButton.setAdapter(adapter);
		endTimeButton.setClickable(false);
		locationInfo.setAdapter(adapter);
		locationInfo.setClickable(false);
		participantsInfo.setAdapter(adapter);
		participantsInfo.setClickable(false);

		dateButton.setOnClickListener(new DateClickListener());
		startTimeButton.setOnClickListener(new TimeClickListener());
		endTimeButton.setOnClickListener(new TimeClickListener());
		locationInfo.setOnClickListener(view -> {
			Intent myIntent = new Intent(getBaseContext(), ChooseLocationActivity.class);
			startActivity(myIntent);
		});
		participantsInfo.setOnClickListener(view -> {
			Intent myIntent = new Intent(getBaseContext(), InviteContactsActivity.class);
			startActivity(myIntent);
		});

		createButton.setOnClickListener(view -> {
			if (isValidData()) {
				if (newMeetingModel.isQuickMeeting()) {
					Date currentDateTime = new Date();
					newMeetingModel.setStartDateTime(currentDateTime);
					Calendar cal = Calendar.getInstance();
					cal.setTime(currentDateTime);
					cal.add(Calendar.HOUR_OF_DAY, QUICK_MEETING_DURATION_HOURS);
					newMeetingModel.setEndDateTime(cal.getTime());
				}
				sendNewMeetingRequest();
			}
		});

		quickMeetingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				newMeetingModel.setQuickMeeting(true);
				descriptionEditText.setVisibility(View.GONE);
				dateButton.setVisibility(View.GONE);
				startTimeButton.setVisibility(View.GONE);
				endTimeButton.setVisibility(View.GONE);
			} else {
				newMeetingModel.setQuickMeeting(false);
				descriptionEditText.setVisibility(View.VISIBLE);
				dateButton.setVisibility(View.VISIBLE);
				startTimeButton.setVisibility(View.VISIBLE);
				endTimeButton.setVisibility(View.VISIBLE);

				if (newMeetingModel.getLocationChoice() != LocationChoice.NOT_SET &&
						newMeetingModel.getLocationChoice() != LocationChoice.SPECIFIC_LOCATION) {
					newMeetingModel.setLocationChoice(LocationChoice.NOT_SET);
					newMeetingModel.getUserPreferredLocations().clear();
				}
			}
		});
		quickMeetingCheckBox.setCheckedImmediately(newMeetingModel.isQuickMeeting());

		UIUtil.setupEditTextFocusListeners(activity, newMeetingLayout);
	}

	private void setSavedDataViews() {
		if (newMeetingModel.getTitle() != null) {
			setViewText(R.id.edt_new_title, newMeetingModel.getTitle());
			setViewText(R.id.edt_input_description, newMeetingModel.getDescription());
		}
		if (newMeetingModel.getStartDateTime() != null) {
			setViewText(R.id.spn_new_date, DateUtil.formatDate(newMeetingModel.getStartDateTime()));
			setViewText(R.id.spn_new_start_time,
					DateUtil.formatTime(newMeetingModel.getStartDateTime()));
		}
		if (newMeetingModel.getEndDateTime() != null) {
			setViewText(R.id.spn_new_date, DateUtil.formatDate(newMeetingModel.getStartDateTime()));
			setViewText(R.id.spn_new_end_time,
					DateUtil.formatTime(newMeetingModel.getEndDateTime()));
		}

		List<String> locationOptions =
				Arrays.asList(getResources().getStringArray(R.array.location_options_array));
		switch (newMeetingModel.getLocationChoice()) {
			case NOT_SET:
				setViewText(R.id.spn_new_location, getString(R.string.new_meeting_touch_to_set));
				break;
			case SPECIFIC_LOCATION:
				setViewText(R.id.spn_new_location, locationOptions.get(0));
				break;
			case RECOMMENDED_FROM_PREFERRED_LOCATIONS:
				setViewText(R.id.spn_new_location, locationOptions.get(1));
				break;
			case RECOMMENDED_BY_PLACE_TYPE:
				setViewText(R.id.spn_new_location, locationOptions.get(2));
				break;
		}

		if (newMeetingModel.getParticipants().size() <= 1) {
			setViewText(R.id.spn_new_participants, getString(R.string.new_meeting_touch_to_set));
		} else {
			setViewText(R.id.spn_new_participants,
					newMeetingModel.getParticipants().size() - 1 + " contacts invited. " +
							getString(R.string.new_meeting_touch_to_view));
		}

	}

	private boolean isValidData() {
		saveData();
		if (getViewText(R.id.edt_new_title).length() == 0) {
			showSnackbar(getString(R.string.new_meeting_enter_title));
			return false;
		}
		if (newMeetingModel.isQuickMeeting()) {
			return true;
		}
		if (getViewText(R.id.spn_new_date).equals(getString(R.string.new_meeting_touch_to_set))) {
			showSnackbar(getString(R.string.new_meeting_set_date));
		} else if (newMeetingModel.getStartDateTime() == null) {
			showSnackbar(getString(R.string.new_meeting_set_start_time));
		} else if (newMeetingModel.getEndDateTime() == null) {
			showSnackbar(getString(R.string.new_meeting_set_end_time));
		} else if (newMeetingModel.getStartDateTime().after(newMeetingModel.getEndDateTime())) {
			showSnackbar(getString(R.string.new_meeting_end_time_after_start));
		} else {
			return true;
		}
		return false;
	}

	private String getViewText(View view) {
		if (view instanceof EditText) {
			return ((EditText) view).getText().toString().trim();
		} else if (view instanceof TextView) {
			return ((TextView) view).getText().toString().trim();
		} else if (view instanceof Spinner) {
			TextView textView = (TextView) ((Spinner) view).getChildAt(1);
			return textView.getText().toString().trim();
		} else {
			return "";
		}
	}

	private String getViewText(int viewId) {
		View view = newMeetingLayout.findViewById(viewId);
		return getViewText(view);
	}

	private void setViewText(View view, String text) {
		if (view instanceof EditText) {
			((EditText) view).setText(text);
		} else if (view instanceof TextView) {
			((TextView) view).setText(text);
		} else if (view instanceof Spinner) {
			TextView textView = (TextView) ((Spinner) view).getChildAt(1);
			textView.setText(text);
		}
	}

	private void setViewText(int viewId, String text) {
		View view = newMeetingLayout.findViewById(viewId);
		setViewText(view, text);
	}

	private void saveData() {
		newMeetingModel.setTitle(getViewText(R.id.edt_new_title));
		newMeetingModel.setDescription(getViewText(R.id.edt_input_description));
		newMeetingModel.setStartDateTime(DateUtil.parseDateTime(
				getViewText(R.id.spn_new_date) + " " + getViewText(R.id.spn_new_start_time)));
		newMeetingModel.setEndDateTime(DateUtil.parseDateTime(
				getViewText(R.id.spn_new_date) + " " + getViewText(R.id.spn_new_end_time)));
	}

	private void addLeaderParticipant() {
		int accountId = getAccountId();
		// Check if leader's participant object already exists in participants list.
		for (Participant participant : NewMeetingActivity.getNewMeetingModel().getParticipants()) {
			if (participant.getAccountId() == accountId) {
				return;
			}
		}

		NewMeetingActivity.getNewMeetingModel().setLeaderId(accountId);
		Participant participant = new Participant(accountId, ParticipationAnswer.PARTICIPATING,
				SendGpsLocationAnswer.NO_ANSWER, LocationService.getGpsLocation(), new Date());
		NewMeetingActivity.getNewMeetingModel().addParticipant(participant);
	}

	public final int getAccountId() {
		return PreferenceManager.getDefaultSharedPreferences(this).getInt("accountId", 0);
	}


/*	private void checkParticipantsWithoutAccount() {
		if (participantsWithoutAccount > 0) {
			showAskSmsDialog();
		} else {
			sendNewMeetingRequest();
		}
	}*/

	//	private void showAskSmsDialog() {
	//		final YesNoFragment dialog = new YesNoFragment();
	//		//		dialog.setMessage(
	//		//				participantsWithoutAccount + getString(R.string.textview_info_invite_via_sms));
	//		dialog.setPositiveButton(view -> {
	//			showWriteSmsDialog();
	//			dialog.dismiss();
	//		});
	//		dialog.setNegativeButton(view -> {
	//			sendNewMeetingRequest();
	//			dialog.dismiss();
	//		});
	//		dialog.hideInput();
	//		dialog.show(getFragmentManager(), "YesNoFragment");
	//	}
	//
	//	private void showWriteSmsDialog() {
	//		final YesNoFragment dialog = new YesNoFragment();
	//		dialog.setMessage(getString(R.string.dialog_please_write_sms));
	//		dialog.setInputText(getString(R.string.dialog_msg_invite_via_sms));
	//		dialog.setPositiveButton(getString(R.string.action_send_sms), view -> {
	//			sendInvitationSms(dialog.getInputValue());
	//			sendNewMeetingRequest();
	//			dialog.dismiss();
	//		});
	//		dialog.setNegativeButton(getString(R.string.action_cancel), view -> dialog.dismiss());
	//		dialog.show(getFragmentManager(), "YesNoFragment");
	//	}
	//
	//	private void sendInvitationSms(String smsMessage) {
	//		SmsManager smsManager = SmsManager.getDefault();
	//		for (Participant participant : NewMeetingActivity.getNewMeetingModel().getParticipants()) {
	//			if (participant.getAccountId() == 0) {
	//				smsManager.sendTextMessage(participant.getPhoneNumber(), null, smsMessage, null,
	//						null);
	//			}
	//		}
	//	}

	public void showProgress(final boolean show) {
		UIUtil.showProgress(this, progressView, newMeetingLayout, show);
	}

	private void sendNewMeetingRequest() {
		showProgress(true);
		RestClient.get().newMeetingRequest(NewMeetingActivity.getNewMeetingModel(),
				new Callback<Meeting>() {
					@Override
					public void success(Meeting meeting, Response response) {
						showProgress(false);
						showSnackbar(getString(R.string.contacts_meeting_created));
						//						activity.changeFragmentToMeetingInfo(meeting);
						finishWithResult(meeting);
					}

					@Override
					public void failure(RetrofitError error) {
						showProgress(false);
						showSnackbar(getString(R.string.error_server_fail));
					}
				});
	}

	private void finishWithResult(Meeting meeting) {
		Bundle bundle = new Bundle();
		bundle =
				GsonUtil.addJsonObjectToBundle(bundle, meeting, meeting.getClass().getSimpleName());
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}

	private class DateClickListener implements View.OnClickListener {

		@Override
		public void onClick(final View view) {
			Calendar c = Calendar.getInstance();
			int current_day = c.get(Calendar.DAY_OF_MONTH);
			int current_month = c.get(Calendar.MONTH);
			int current_year = c.get(Calendar.YEAR);
			if (!getViewText(view).equals(getString(R.string.new_meeting_touch_to_set))) {
				Date viewDate = DateUtil.parseDate(getViewText(view));
				if (viewDate != null) {
					c.setTime(viewDate);
				}
			}
			int view_day = c.get(Calendar.DAY_OF_MONTH);
			int view_month = c.get(Calendar.MONTH);
			int view_year = c.get(Calendar.YEAR);

			Dialog.Builder builder =
					new DatePickerDialog.Builder(R.style.DialogTheme_DatePicker, current_day,
							current_month, current_year, 1, 1, 2020, view_day, view_month,
							view_year) {
						@Override
						public void onPositiveActionClicked(DialogFragment fragment) {
							DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
							String date = dialog.getFormattedDate(DateUtil.DATE_FORMAT);
							setViewText(view, date);
							super.onPositiveActionClicked(fragment);
						}
					};

			builder.positiveAction("OK").negativeAction("CANCEL");
			DialogFragment fragment = DialogFragment.newInstance(builder);
			fragment.show(getSupportFragmentManager(), null);
		}
	}


	private class TimeClickListener implements View.OnClickListener {

		@Override
		public void onClick(final View view) {
			Calendar c = Calendar.getInstance();
			if (!getViewText(view).equals(getString(R.string.new_meeting_touch_to_set))) {
				Date viewDate = DateUtil.parseTime(getViewText(view));
				assert viewDate != null;
				c.setTime(viewDate);
			}
			Dialog.Builder builder = new TimePickerDialog.Builder(R.style.DialogTheme_TimePicker,
					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)) {
				@Override
				public void onPositiveActionClicked(DialogFragment fragment) {
					TimePickerDialog dialog = (TimePickerDialog) fragment.getDialog();
					String time = dialog.getFormattedTime(DateUtil.TIME_FORMAT);
					setViewText(view, time);
					super.onPositiveActionClicked(fragment);
				}
			};

			builder.positiveAction("OK").negativeAction("CANCEL");
			DialogFragment fragment = DialogFragment.newInstance(builder);
			fragment.show(getSupportFragmentManager(), null);
		}
	}
}
