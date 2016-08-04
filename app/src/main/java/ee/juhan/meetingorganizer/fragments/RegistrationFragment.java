package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.models.server.Account;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.util.PatternMatcherUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
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
		Button registerButton = (Button) registrationLayout.findViewById(R.id.btn_register);
		registerButton.setOnClickListener(view -> {
			if (isValidData()) {
				sendRegistrationRequest(getViewText(R.id.edt_register_name),
						getViewText(R.id.edt_register_email),
						getViewText(R.id.edt_register_password),
						"+" + getViewText(R.id.edt_register_country_code) +
								getViewText(R.id.edt_register_phone_nr));
			}
		});

		UIUtil.setupEditTextFocusListeners(activity, registrationLayout);
	}

	private boolean isValidData() {
		if (!PatternMatcherUtil.isValidEmail(getViewText(R.id.edt_register_email))) {
			UIUtil.showToastMessage(activity, getString(R.string.login_invalid_email));
		} else if (getViewText(R.id.edt_register_password).length() < PASSWORD_MIN_LENGTH) {
			UIUtil.showToastMessage(activity, getString(R.string.login_short_password1) +
					PASSWORD_MIN_LENGTH + getString(R.string.login_short_password2));
		} else if (!getViewText(R.id.edt_register_password)
				.equals(getViewText(R.id.edt_register_password_confirm))) {
			UIUtil.showToastMessage(activity,
					getString(R.string.registration_passwords_dont_match));
		} else if (!PatternMatcherUtil.isValidName(getViewText(R.id.edt_register_name))) {
			UIUtil.showToastMessage(activity, getString(R.string.registration_enter_full_name));
		} else if (!PatternMatcherUtil
				.isValidAreaNumber(getViewText(R.id.edt_register_country_code))) {
			UIUtil.showToastMessage(activity, getString(R.string.registration_invalid_area_code));
		} else if (!PatternMatcherUtil
				.isValidPhoneNumber(getViewText(R.id.edt_register_phone_nr))) {
			UIUtil.showToastMessage(activity,
					getString(R.string.registration_invalid_phone_number));
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
		RestClient.get().registrationRequest(new Account(name, email, password, phoneNr),
				new Callback<ServerResponse>() {
					@Override
					public void success(final ServerResponse serverResponse, Response response) {
						activity.showProgress(false);
						switch (serverResponse.getResult()) {
							case SUCCESS:
								UIUtil.showToastMessage(activity,
										getString(R.string.registration_successful));
								activity.logIn(serverResponse.getSid(),
										serverResponse.getAccount());
								break;
							case EMAIL_IN_USE:
								UIUtil.showToastMessage(activity,
										getString(R.string.registration_email_in_use));
								break;
							case PHONE_NUMBER_IN_USE:
								UIUtil.showToastMessage(activity,
										getString(R.string.registration_phone_number_in_use));
								break;
							case FAIL:
								UIUtil.showToastMessage(activity,
										getString(R.string.error_server_fail));
								break;
						}
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

}
