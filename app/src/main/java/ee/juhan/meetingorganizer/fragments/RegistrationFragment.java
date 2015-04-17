package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.core.communications.loaders.RegistrationLoader;
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import ee.juhan.meetingorganizer.models.server.ServerResult;
import ee.juhan.meetingorganizer.util.PatternMatcherUtil;

public class RegistrationFragment extends Fragment {

    private MainActivity activity;
    private final String title = "Registration";
    private LinearLayout registrationLayout;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        registrationLayout = (LinearLayout) inflater.inflate(R.layout.fragment_registration, container, false);
        setButtonListeners();
        return registrationLayout;
    }

    private void setButtonListeners() {
        Button registerButton = (Button) registrationLayout
                .findViewById(R.id.register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailTextBox = (EditText) registrationLayout
                        .findViewById(R.id.email_textbox);
                EditText password1TextBox = (EditText) registrationLayout
                        .findViewById(R.id.password_textbox);
                EditText password2TextBox = (EditText) registrationLayout
                        .findViewById(R.id.password_confirmation_textbox);
                EditText areaNrTextBox = (EditText) registrationLayout
                        .findViewById(R.id.area_number_textbox);
                EditText phoneNrTextBox = (EditText) registrationLayout
                        .findViewById(R.id.phone_number_textbox);

                String email = emailTextBox.getText().toString();
                String password1 = password1TextBox.getText().toString();
                String password2 = password2TextBox.getText().toString();
                String areaNr = areaNrTextBox.getText().toString();
                String phoneNr = phoneNrTextBox.getText().toString();

                if (!PatternMatcherUtil.isValidEmail(email)) {
                    activity.showToastMessage("Invalid email address!");
                } else if (password1.length() < 5) {
                    activity.showToastMessage("Password must be at least 5 characters long!");
                } else if (!password1.equals(password2)) {
                    activity.showToastMessage("The passwords don't match!");
                } else if (!PatternMatcherUtil.isValidAreaNumber(areaNr)) {
                    activity.showToastMessage("Invalid area number!");
                } else if (!PatternMatcherUtil.isValidPhoneNumber(phoneNr)) {
                    activity.showToastMessage("Invalid phone number!");
                } else {
                    sendRegistrationRequest(email, password1, areaNr + phoneNr);
                }
            }
        });

    }

    private void sendRegistrationRequest(final String email, String password, String phoneNr) {
        RegistrationLoader registrationLoader = new RegistrationLoader(
                new AccountDTO(email, password, phoneNr)) {
            @Override
            public void handleResponse(final ServerResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.dismissLoadingFragment();
                        if (response != null) {
                            ServerResult result = response.getResult();
                            if (result == ServerResult.SUCCESS) {
                                activity.showToastMessage("Registration successful!");
                                activity.logIn(email, response.getSid(), response.getUserId());
                            } else if (result == ServerResult.EMAIL_IN_USE) {
                                activity.showToastMessage("Email is already in use");
                            } else {
                                activity.showToastMessage("Server response fail.");
                            }
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                    }
                });
            }
        };
        activity.showLoadingFragment();
        registrationLoader.retrieveResponse();
    }

}
