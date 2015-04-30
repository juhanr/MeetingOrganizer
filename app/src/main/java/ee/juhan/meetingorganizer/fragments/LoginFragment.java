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
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import ee.juhan.meetingorganizer.models.server.ServerResult;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.PatternMatcherUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginFragment extends Fragment {

    private String title;
    private MainActivity activity;
    private LinearLayout loginLayout;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        title = getString(R.string.title_login);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        activity.setDrawerItem(activity.getDrawerItemPosition(title));
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
                if (isValidData()) {
                    sendLoginRequest(getViewText(R.id.email_textbox),
                            getViewText(R.id.password_textbox));
                }
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.selectDrawerItem(2);
            }
        });

    }

    private boolean isValidData() {
        if (!PatternMatcherUtil.isValidEmail(getViewText(R.id.email_textbox))) {
            activity.showToastMessage(getString(R.string.toast_invalid_email));
        } else if (getViewText(R.id.password_textbox).length() < 5) {
            activity.showToastMessage(getString(R.string.toast_short_password));
        } else {
            return true;
        }
        return false;
    }

    private String getViewText(int viewId) {
        View view = loginLayout.findViewById(viewId);
        if (view instanceof EditText)
            return ((EditText) view).getText().toString().trim();
        else if (view instanceof TextView)
            return ((TextView) view).getText().toString().trim();
        else return null;
    }

    private void sendLoginRequest(final String email, String password) {
        activity.showLoadingFragment();
        RestClient.get().loginRequest(new AccountDTO(email, password), new Callback<ServerResponse>() {
            @Override
            public void success(final ServerResponse serverResponse, Response response) {
                activity.dismissLoadingFragment();
                ServerResult result = serverResponse.getResult();
                if (result == ServerResult.SUCCESS) {
                    activity.showToastMessage(getString(R.string.toast_login_successful));
                    activity.logIn(email, serverResponse.getSid(), serverResponse.getUserId());
                } else if (result == ServerResult.WRONG_PASSWORD) {
                    activity.showToastMessage(getString(R.string.toast_wrong_password));
                } else if (result == ServerResult.NO_ACCOUNT_FOUND) {
                    activity.showToastMessage(getString(R.string.toast_no_account));
                } else {
                    activity.showToastMessage(getString(R.string.toast_server_fail));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                activity.dismissLoadingFragment();
                activity.showToastMessage(getString(R.string.toast_server_fail));
            }
        });
    }

}
