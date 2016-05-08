package ee.juhan.meetingorganizer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.widget.Spinner;

import java.util.Calendar;
import java.util.Date;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.dialogs.YesNoFragment;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewMeetingActivity extends AppCompatActivity {

	private static MeetingDTO newMeetingModel = new MeetingDTO();
	private ViewGroup newMeetingLayout;
	private View progressView;
	private Activity activity = this;

	public static MeetingDTO getNewMeetingModel() {
		return NewMeetingActivity.newMeetingModel;
	}

	public static void setNewMeetingModel(MeetingDTO meetingModel) {
		NewMeetingActivity.newMeetingModel = meetingModel;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_meeting);
		setTitle(getString(R.string.title_new_meeting));
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		newMeetingLayout = (ViewGroup) findViewById(R.id.layout_content);
		progressView = findViewById(R.id.progress_bar);
		setButtonListeners();
		addLeaderInfo();
	}

	@Override
	public void onResume() {
		super.onResume();
		setSavedDataViews();
	}

	@Override
	public void onPause() {
		saveData();
		super.onPause();
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

	private void setButtonListeners() {
		String[] items = new String[]{"Not set."};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn, items);
		adapter.setDropDownViewResource(R.layout.row_spn_dropdown);

		Spinner dateButton = (Spinner) newMeetingLayout.findViewById(R.id.date_button);
		Spinner startTimeButton = (Spinner) newMeetingLayout.findViewById(R.id.start_time_button);
		Spinner endTimeButton = (Spinner) newMeetingLayout.findViewById(R.id.end_time_button);
		Spinner locationInfo = (Spinner) newMeetingLayout.findViewById(R.id.location_info);
		Spinner participantsInfo = (Spinner) newMeetingLayout.findViewById(R.id.participants_info);
		Button createButton = (Button) newMeetingLayout.findViewById(R.id.create_button);

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
		locationInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getBaseContext(), LocationActivity.class);
				startActivity(myIntent);
			}
		});
		participantsInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getBaseContext(), InviteContactsActivity.class);
				startActivity(myIntent);
			}
		});

		createButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isValidData()) {
					sendNewMeetingRequest();
				}
			}
		});

		UIUtil.setupEditTextFocusListeners(activity, newMeetingLayout);
	}

	private void setSavedDataViews() {
		if (newMeetingModel.getTitle() != null) {
			setViewText(R.id.title_textbox, newMeetingModel.getTitle());
			setViewText(R.id.description_textbox, newMeetingModel.getDescription());
		}
		if (newMeetingModel.getStartDateTime() != null) {
			setViewText(R.id.date_button, DateUtil.formatDate(newMeetingModel.getStartDateTime()));
			setViewText(R.id.start_time_button,
					DateUtil.formatTime(newMeetingModel.getStartDateTime()));
		}
		if (newMeetingModel.getEndDateTime() != null) {
			setViewText(R.id.date_button, DateUtil.formatDate(newMeetingModel.getStartDateTime()));
			setViewText(R.id.end_time_button,
					DateUtil.formatTime(newMeetingModel.getEndDateTime()));
		}
		if (newMeetingModel.getLocationType() == LocationType.SPECIFIC_LOCATION &&
				newMeetingModel.getLocation() != null || newMeetingModel.getLocationType() ==
				LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS &&
				!newMeetingModel.getPredefinedLocations().isEmpty()) {
			setViewText(R.id.location_info, "Click to show.");
		} else {
			setViewText(R.id.location_info, "Not set.");
		}
		if (newMeetingModel.getParticipants().isEmpty()) {
			setViewText(R.id.participants_info, "Not set.");
		} else {
			setViewText(R.id.participants_info,
					newMeetingModel.getParticipants().size() + " contacts invited. Click to show.");
		}

	}

	private boolean isValidData() {
		if (getViewText(R.id.title_textbox).length() == 0) {
			UIUtil.showToastMessage(this, getString(R.string.toast_please_enter_title));
		} else if (getViewText(R.id.date_button).equals(getString(R.string.textview_not_set_u))) {
			UIUtil.showToastMessage(this, getString(R.string.toast_please_set_date));
		} else if (newMeetingModel.getStartDateTime() == null) {
			UIUtil.showToastMessage(this, getString(R.string.toast_please_set_start_time));
		} else if (newMeetingModel.getEndDateTime() == null) {
			UIUtil.showToastMessage(this, getString(R.string.toast_please_set_end_time));
		} else if (newMeetingModel.getStartDateTime().after(newMeetingModel.getEndDateTime())) {
			UIUtil.showToastMessage(this, getString(R.string.toast_end_time_after_start));
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
		newMeetingModel.setTitle(getViewText(R.id.title_textbox));
		newMeetingModel.setDescription(getViewText(R.id.description_textbox));
		newMeetingModel.setStartDateTime(DateUtil.parseDateTime(
				getViewText(R.id.date_button) + " " + getViewText(R.id.start_time_button)));
		newMeetingModel.setEndDateTime(DateUtil.parseDateTime(
				getViewText(R.id.date_button) + " " + getViewText(R.id.end_time_button)));
	}

	private void addLeaderInfo() {
		NewMeetingActivity.getNewMeetingModel().setLeaderId(getUserId());
		ParticipantDTO participant =
				new ParticipantDTO(NewMeetingActivity.getNewMeetingModel().getLeaderId(),
						ParticipationAnswer.PARTICIPATING, MyLocationListener.getMyLocation());
		NewMeetingActivity.getNewMeetingModel().addParticipant(participant);
	}

	public final Integer getUserId() {
		return PreferenceManager.getDefaultSharedPreferences(this).getInt("userId", 0);
	}

	public void showProgress(final boolean show) {
		UIUtil.showProgress(this, progressView, newMeetingLayout, show);
	}


/*	private void checkParticipantsWithoutAccount() {
		if (participantsWithoutAccount > 0) {
			showAskSMSDialog();
		} else {
			sendNewMeetingRequest();
		}
	}*/

	private void showAskSMSDialog() {
		final YesNoFragment dialog = new YesNoFragment();
		//		dialog.setMessage(
		//				participantsWithoutAccount + getString(R.string.textview_info_invite_via_sms));
		dialog.setPositiveButton(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showWriteSMSDialog();
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNewMeetingRequest();
				dialog.dismiss();
			}
		});
		dialog.hideInput();
		dialog.show(getFragmentManager(), "YesNoFragment");
	}

	private void showWriteSMSDialog() {
		final YesNoFragment dialog = new YesNoFragment();
		dialog.setMessage(getString(R.string.textview_please_write_sms));
		dialog.setInputText(getString(R.string.message_invite_via_sms));
		dialog.setPositiveButton(getString(R.string.button_send_sms), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendInvitationSMS(dialog.getInputValue());
				sendNewMeetingRequest();
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(getString(R.string.button_cancel), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show(getFragmentManager(), "YesNoFragment");
	}

	private void sendInvitationSMS(String smsMessage) {
		SmsManager smsManager = SmsManager.getDefault();
		for (ParticipantDTO participant : NewMeetingActivity.getNewMeetingModel()
				.getParticipants()) {
			if (participant.getAccountId() == 0) {
				smsManager.sendTextMessage(participant.getPhoneNumber(), null, smsMessage, null,
						null);
			}
		}
	}

	private void sendNewMeetingRequest() {
		showProgress(true);
		RestClient.get().newMeetingRequest(NewMeetingActivity.getNewMeetingModel(),
				new Callback<MeetingDTO>() {
					@Override
					public void success(MeetingDTO meeting, Response response) {
						meeting.toUTCTimeZone();
						showProgress(false);
						UIUtil.showToastMessage(activity,
								getString(R.string.toast_meeting_created));
						//						activity.changeFragmentToMeetingInfo(meeting);
						NewMeetingActivity.setNewMeetingModel(new MeetingDTO());
						finishWithResult();
					}

					@Override
					public void failure(RetrofitError error) {
						showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.toast_server_fail));
					}
				});
	}

	private void finishWithResult() {
		Bundle conData = new Bundle();
		conData.putString("param_result", "Thanks Thanks");
		Intent intent = new Intent();
		intent.putExtras(conData);
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
			if (!getViewText(view).equals(getString(R.string.textview_not_set_u))) {
				Date viewDate = DateUtil.parseDate(getViewText(view));
				if (viewDate != null) {
					c.setTime(viewDate);
				}
			}
			int view_day = c.get(Calendar.DAY_OF_MONTH);
			int view_month = c.get(Calendar.MONTH);
			int view_year = c.get(Calendar.YEAR);

			Dialog.Builder builder =
					new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker_Light,
							current_day, current_month, current_year, 1, 1, 2020, view_day,
							view_month, view_year) {
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
			if (!getViewText(view).equals(getString(R.string.textview_not_set_u))) {
				Date viewDate = DateUtil.parseTime(getViewText(view));
				assert viewDate != null;
				c.setTime(viewDate);
			}
			Dialog.Builder builder =
					new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light,
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
