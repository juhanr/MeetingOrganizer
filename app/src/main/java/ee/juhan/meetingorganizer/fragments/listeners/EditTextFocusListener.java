package ee.juhan.meetingorganizer.fragments.listeners;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import ee.juhan.meetingorganizer.MainActivity;

public class EditTextFocusListener implements View.OnFocusChangeListener {

	private MainActivity activity;

	public EditTextFocusListener(MainActivity activity) {
		this.activity = activity;
	}

	public final void hideKeyboard(View view) {
		InputMethodManager inputMethodManager =
				(InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			hideKeyboard(v);
		}
	}
}
