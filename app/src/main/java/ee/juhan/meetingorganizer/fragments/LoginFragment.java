package ee.juhan.meetingorganizer.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.core.communications.loaders.LoginLoader;
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import ee.juhan.meetingorganizer.models.server.ServerResult;
import ee.juhan.meetingorganizer.util.PatternMatcherUtil;

public class LoginFragment extends Fragment {

    private MainActivity activity;
    private final String title = "Log in";
    private LinearLayout loginLayout;

    public LoginFragment() {
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
        loginLayout = (LinearLayout) inflater.inflate(R.layout.fragment_login, container, false);
        setButtonListeners();
        return loginLayout;
    }

    private void setButtonListeners() {
        Button loginButton = (Button) loginLayout
                .findViewById(R.id.login_button);
        TextView createAccountButton = (TextView) loginLayout
                .findViewById(R.id.create_account_textbutton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailTextBox = (EditText) loginLayout
                        .findViewById(R.id.email_textbox);
                EditText passwordTextBox = (EditText) loginLayout
                        .findViewById(R.id.password_textbox);
                String email = emailTextBox.getText().toString();
                String password = passwordTextBox.getText().toString();

                if (!PatternMatcherUtil.isValidEmail(email)) {
                    activity.showToastMessage("Invalid email address!");
                } else if (password.length() < 5) {
                    activity.showToastMessage("Password must be at least 5 characters long!");
                } else {
                    sendLoginRequest(email, password);
                }
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.changeFragment(new RegistrationFragment());
            }
        });

    }

    private void sendLoginRequest(String email, String password) {
        LoginLoader loginLoader = new LoginLoader(new AccountDTO(email, password)) {
            @Override
            public void handleResponse(final ServerResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response != null) {
                            ServerResult result = response.getResult();
                            if (result == ServerResult.SUCCESS) {
                                activity.showToastMessage("Log in successful!");
                                activity.logIn(response.getSid(), response.getUserId());
                            } else if (result == ServerResult.WRONG_PASSWORD) {
                                activity.showToastMessage("Wrong password!");
                            } else if (result == ServerResult.NO_ACCOUNT_FOUND) {
                                activity.showToastMessage("No such account found!");
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
        loginLoader.retrieveResponse();
    }

}
