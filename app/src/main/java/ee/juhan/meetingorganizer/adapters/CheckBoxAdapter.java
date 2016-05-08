package ee.juhan.meetingorganizer.adapters;

import android.content.Context;
import android.widget.CompoundButton;

import com.rey.material.widget.CheckBox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.juhan.meetingorganizer.R;

public abstract class CheckBoxAdapter<T> extends GeneralAdapter<T>
		implements CompoundButton.OnCheckedChangeListener {

	private Set<T> checkedItems = new HashSet<>();
	private String checkBoxText = "";
	private CheckBox checkBox;

	public CheckBoxAdapter(Context context, List<T> objects) {
		super(context, R.layout.list_item_checkbox, objects);
	}

	@Override
	protected final void populateLayout() {
		checkBox = (CheckBox) super.getLayout().findViewById(R.id.checkbox);
		setUpCheckBox();
		checkBox.setText(checkBoxText);
		checkBox.setOnCheckedChangeListener(this);
	}

	public final Set<T> getCheckedItems() {
		return checkedItems;
	}

	protected final void setCheckBoxText(String text) {
		this.checkBoxText = text;
	}

	protected abstract void setUpCheckBox();

	protected final CheckBox getCheckBox() {
		return checkBox;
	}

}