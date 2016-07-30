package ee.juhan.meetingorganizer.fragments.dialogs;


import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.juhan.meetingorganizer.R;

public class YesNoFragment extends DialogFragment {

	private LinearLayout dialogLayout;
	private String messageText;
	private String inputText;
	private String inputHint;
	private boolean hideInput;
	private String positiveButtonText;
	private String negativeButtonText;
	private View.OnClickListener positiveButtonListener;
	private View.OnClickListener negativeButtonListener;

	public YesNoFragment() {}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		dialogLayout = (LinearLayout) inflater.inflate(R.layout.fragment_yes_no, container, false);
		refreshDialog();
		return dialogLayout;
	}

	@Override
	public final Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(false);
		return dialog;
	}

	public final void refreshDialog() {
		if (dialogLayout != null) {
			TextView messageTextView = (TextView) dialogLayout.findViewById(R.id.dialog_message);
			EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.dialog_input);
			Button positiveButton = (Button) dialogLayout.findViewById(R.id.dialog_positive_button);
			Button negativeButton = (Button) dialogLayout.findViewById(R.id.dialog_negative_button);
			messageTextView.setText(messageText);
			if (positiveButtonText != null) {
				positiveButton.setText(positiveButtonText);
			}
			if (negativeButtonText != null) {
				negativeButton.setText(negativeButtonText);
			}
			positiveButton.setOnClickListener(positiveButtonListener);
			negativeButton.setOnClickListener(negativeButtonListener);
			if (inputEditText != null) {
				if (!hideInput) {
					inputEditText.setText(inputText);
					inputEditText.setHint(inputHint);
				} else {
					inputEditText.setVisibility(View.GONE);
				}
			}
		}
	}

	public final YesNoFragment setMessage(String message) {
		messageText = message;
		return this;
	}

	public final YesNoFragment setInputText(String text) {
		inputText = text;
		return this;
	}

	public final YesNoFragment setInputHint(String hint) {
		inputHint = hint;
		return this;
	}

	public final String getInputValue() {
		EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.dialog_input);
		return inputEditText.getText().toString();
	}

	public final YesNoFragment hideInput() {
		hideInput = true;
		return this;
	}

	public final YesNoFragment setPositiveButton(String buttonText,
			View.OnClickListener buttonClickListener) {
		positiveButtonText = buttonText;
		positiveButtonListener = buttonClickListener;
		return this;
	}

	public final YesNoFragment setPositiveButton(View.OnClickListener buttonClickListener) {
		positiveButtonListener = buttonClickListener;
		return this;
	}

	public final YesNoFragment setPositiveButton(String buttonText) {
		positiveButtonText = buttonText;
		return this;
	}

	public final YesNoFragment setNegativeButton(String buttonText,
			View.OnClickListener buttonClickListener) {
		negativeButtonText = buttonText;
		negativeButtonListener = buttonClickListener;
		return this;
	}

	public final YesNoFragment setNegativeButton(View.OnClickListener buttonClickListener) {
		negativeButtonListener = buttonClickListener;
		return this;
	}

	public final YesNoFragment setNegativeButton(String buttonText) {
		negativeButtonText = buttonText;
		return this;
	}

}
