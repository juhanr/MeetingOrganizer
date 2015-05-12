package ee.juhan.meetingorganizer.adapters;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.HashSet;
import java.util.List;

import ee.juhan.meetingorganizer.R;

public abstract class CheckBoxAdapter<T> extends GeneralAdapter<T>
        implements CompoundButton.OnCheckedChangeListener {

    private HashSet<T> checkedItems = new HashSet<>();
    private String checkBoxText = "";
    private CheckBox checkBox;

    public CheckBoxAdapter(Context context, List<T> objects) {
        super(context, R.layout.list_item_checkbox, objects);
    }

    @Override
    protected void populateLayout() {
        checkBox = (CheckBox) super.getLayout().findViewById(R.id.checkbox);
        setUpCheckBox();
        checkBox.setText(checkBoxText);
        checkBox.setOnCheckedChangeListener(this);
    }

    public HashSet<T> getCheckedItems() {
        return checkedItems;
    }

    protected void setCheckBoxText(String text) {
        this.checkBoxText = text;
    }

    protected abstract void setUpCheckBox();

    protected CheckBox getCheckBox() {
        return checkBox;
    }
}