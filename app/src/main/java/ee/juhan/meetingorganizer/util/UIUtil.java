package ee.juhan.meetingorganizer.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ee.juhan.meetingorganizer.fragments.listeners.EditTextFocusListener;

public class UIUtil {

	/**
	 * Displays a toast message with the given message.
	 *
	 * @param message
	 * 		message string
	 */
	public static void showToastMessage(final Activity activity, final String message) {
		Toast toast = Toast.makeText(activity, message, Toast.LENGTH_LONG);
		toast.show();
	}

	public static void setupEditTextFocusListeners(Activity activity, View view) {
		if (view instanceof EditText) {
			view.setOnFocusChangeListener(new EditTextFocusListener(activity));
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupEditTextFocusListeners(activity, innerView);
			}
		}
	}

	public static void showProgress(Activity activity, final View progressView,
			final View contentView, final boolean show) {
		if (contentView == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime =
					activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

			contentView.setVisibility(show ? View.GONE : View.VISIBLE);
			contentView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							contentView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							progressView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
		} else {
			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			contentView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

}
