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

public class ContactAdapter extends ArrayAdapter<String> {

    private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    private HashSet<String> checkedContacts = new HashSet<>();
    private final Context context;

    public ContactAdapter(Context context,
                          List<String> objects) {
        super(context, R.layout.list_item_choose_contacts, objects);
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
                R.layout.list_item_choose_contacts, parent, false);

        CheckBox contactNameView = (CheckBox) listItemView
                .findViewById(R.id.contact_name);
        contactNameView.setText(getItem(position));
        contactNameView.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        CheckBox contactNameView = (CheckBox) buttonView
                                .findViewById(R.id.contact_name);
                        String contactName = (String) ((CheckBox) buttonView).getText();
                        if (isChecked)
                            checkedContacts.add(contactName);
                        else checkedContacts.remove(contactName);
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

    public HashSet<String> getCheckedContacts() {
        return checkedContacts;
    }

}