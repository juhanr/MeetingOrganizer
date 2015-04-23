package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ee.juhan.meetingorganizer.R;

public class CheckBoxAdapter extends ArrayAdapter<String> {

    private final Context context;
    private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    private HashSet<String> checkedItems = new HashSet<>();

    public CheckBoxAdapter(Context context, List<String> objects) {
        super(context, R.layout.list_item_checkbox, objects);
        this.context = context;
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout listItemView = (LinearLayout) inflater.inflate(
                R.layout.list_item_checkbox, parent, false);

        CheckBox checkBox = (CheckBox) listItemView
                .findViewById(R.id.checkbox);
        checkBox.setText(getItem(position));
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String contactName = (String) ((CheckBox) buttonView).getText();
                        if (isChecked)
                            checkedItems.add(contactName);
                        else checkedItems.remove(contactName);
                    }
                }
        );

        return listItemView;
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public HashSet<String> getCheckedItems() {
        return checkedItems;
    }

}