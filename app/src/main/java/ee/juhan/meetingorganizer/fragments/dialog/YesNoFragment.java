package ee.juhan.meetingorganizer.fragments.dialog;


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

    private final YesNoFragment yesNoFragment = this;
    private LinearLayout dialogLayout;
    private String messageText;
    private String inputText;
    private String inputHint;
    private boolean hideInput;
    private String positiveButtonText = "Yes";
    private String negativeButtonText = "No";
    private View.OnClickListener positiveButtonListener;
    private View.OnClickListener negativeButtonListener;

    public YesNoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialogLayout = (LinearLayout) inflater.inflate(R.layout.fragment_yes_no, container, false);
        refreshDialog();
        return dialogLayout;
    }

    public void refreshDialog() {
        if (dialogLayout != null) {
            TextView messageTextView = (TextView) dialogLayout.findViewById(R.id.dialog_message);
            EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.dialog_input);
            Button positiveButton = (Button) dialogLayout.findViewById(R.id.dialog_positive_button);
            Button negativeButton = (Button) dialogLayout.findViewById(R.id.dialog_negative_button);

            messageTextView.setText(messageText);
            positiveButton.setText(positiveButtonText);
            negativeButton.setText(negativeButtonText);
            positiveButton.setOnClickListener(positiveButtonListener);
            negativeButton.setOnClickListener(negativeButtonListener);
            if (inputEditText != null) {
                if (!hideInput) {
                    inputEditText.setText(inputText);
                    inputEditText.setHint(inputHint);
                } else
                    inputEditText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        return dialog;
    }

    public YesNoFragment setMessage(String message) {
        messageText = message;
        return this;
    }

    public YesNoFragment setInputText(String text) {
        inputText = text;
        return this;
    }

    public YesNoFragment setInputHint(String hint) {
        inputHint = hint;
        return this;
    }

    public String getInputValue() {
        EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.dialog_input);
        return inputEditText.getText().toString();
    }

    public YesNoFragment hideInput() {
        hideInput = true;
        return this;
    }

    public YesNoFragment setPositiveButton(String buttonText,
                                           android.view.View.OnClickListener buttonClickListener) {
        positiveButtonText = buttonText;
        positiveButtonListener = buttonClickListener;
        return this;
    }

    public YesNoFragment setPositiveButton(
            android.view.View.OnClickListener buttonClickListener) {
        positiveButtonListener = buttonClickListener;
        return this;
    }

    public YesNoFragment setPositiveButton(String buttonText) {
        positiveButtonText = buttonText;
        return this;
    }

    public YesNoFragment setNegativeButton(String buttonText,
                                           android.view.View.OnClickListener buttonClickListener) {
        negativeButtonText = buttonText;
        negativeButtonListener = buttonClickListener;
        return this;
    }

    public YesNoFragment setNegativeButton(
            android.view.View.OnClickListener buttonClickListener) {
        negativeButtonListener = buttonClickListener;
        return this;
    }

    public YesNoFragment setNegativeButton(String buttonText) {
        negativeButtonText = buttonText;
        return this;
    }

}
