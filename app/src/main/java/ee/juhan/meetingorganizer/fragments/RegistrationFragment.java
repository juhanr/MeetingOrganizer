package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.PatternMatcherUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegistrationFragment extends Fragment {

	private static final int PASSWORD_MIN_LENGTH = 5;
	private String title;
	private MainActivity activity;
	private ViewGroup registrationLayout;

	public RegistrationFragment() {}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_registration);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		activity.checkDrawerItem(R.id.nav_registration);
		registrationLayout =
				(ViewGroup) inflater.inflate(R.layout.fragment_registration, container, false);
		setButtonListeners();
		return registrationLayout;
	}

	private void setButtonListeners() {
		Button registerButton = (Button) registrationLayout.findViewById(R.id.register_button);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isValidData()) {
					sendRegistrationRequest(getViewText(R.id.name_textbox),
							getViewText(R.id.email_textbox), getViewText(R.id.password_textbox),
							"+" + getViewText(R.id.area_number_textbox) +
									getViewText(R.id.phone_number_textbox));
				}
			}
		});

		activity.setupEditTextFocusListeners(registrationLayout);
	}

	private boolean isValidData() {
		if (!PatternMatcherUtil.isValidEmail(getViewText(R.id.email_textbox))) {
			activity.showToastMessage(getString(R.string.toast_invalid_email));
		} else if (getViewText(R.id.password_textbox).length() < PASSWORD_MIN_LENGTH) {
			activity.showToastMessage(getString(R.string.toast_short_password1) +
					PASSWORD_MIN_LENGTH + getString(R.string.toast_short_password2));
		} else if (!getViewText(R.id.password_textbox)
				.equals(getViewText(R.id.password_confirmation_textbox))) {
			activity.showToastMessage(getString(R.string.toast_passwords_dont_match));
		} else if (!PatternMatcherUtil.isValidName(getViewText(R.id.name_textbox))) {
			activity.showToastMessage(getString(R.string.toast_please_enter_full_name));
		} else if (!PatternMatcherUtil.isValidAreaNumber(getViewText(R.id.area_number_textbox))) {
			activity.showToastMessage(getString(R.string.toast_invalid_area_code));
		} else if (!PatternMatcherUtil.isValidPhoneNumber(getViewText(R.id.phone_number_textbox))) {
			activity.showToastMessage(getString(R.string.toast_invalid_phone_number));
		} else {
			return true;
		}
		return false;
	}

	private String getViewText(int viewId) {
		View view = registrationLayout.findViewById(viewId);
		if (view instanceof EditText) {
			return ((EditText) view).getText().toString().trim();
		} else if (view instanceof TextView) {
			return ((TextView) view).getText().toString().trim();
		} else {
			return "";
		}
	}

	private void sendRegistrationRequest(String name, final String email, String password,
			String phoneNr) {
		activity.showProgress(true);
		RestClient.get().registrationRequest(new AccountDTO(name, email, password, phoneNr),
				new Callback<ServerResponse>() {
					@Override
					public void success(final ServerResponse serverResponse, Response response) {
						activity.showProgress(false);
						switch (serverResponse.getResult()) {
							case SUCCESS:
								activity.showToastMessage(
										getString(R.string.toast_registration_successful));
								activity.logIn(email, serverResponse.getSid(),
										serverResponse.getUserId());
								break;
							case EMAIL_IN_USE:
								activity.showToastMessage(getString(R.string.toast_email_in_use));
								break;
							case PHONE_NUMBER_IN_USE:
								activity.showToastMessage(
										getString(R.string.toast_phone_number_in_use));
								break;
							case FAIL:
								activity.showToastMessage(getString(R.string.toast_server_fail));
								break;
						}
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						activity.showToastMessage(getString(R.string.toast_server_fail));
					}
				});
	}

}
