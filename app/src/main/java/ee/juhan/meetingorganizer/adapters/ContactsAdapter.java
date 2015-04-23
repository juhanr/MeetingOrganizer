package ee.juhan.meetingorganizer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.ContactDTO;

public class ContactsAdapter extends ArrayAdapter<ContactDTO> {

    private final Context context;
    private HashMap<ContactDTO, Integer> mIdMap = new HashMap<ContactDTO, Integer>();
    private HashSet<ContactDTO> checkedItems = new HashSet<>();

    public ContactsAdapter(Context context, List<ContactDTO> objects) {
        super(context, R.layout.list_item_checkbox, objects);
        this.context = context;
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactDTO contact = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout listItemView = (LinearLayout) inflater.inflate(
                R.layout.list_item_checkbox, parent, false);

        CheckBox checkBox = (CheckBox) listItemView
                .findViewById(R.id.checkbox);
        if (checkedItems.contains(contact)) {
            checkBox.setChecked(true);
        }
        checkBox.setText(contact.getName());
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String contactName = (String) ((CheckBox) buttonView).getText();
                        ContactDTO chosenContact = new ContactDTO();
                        for (ContactDTO contact : mIdMap.keySet()) {
                            if (contact.getName().equals(contactName)) {
                                chosenContact = contact;
                                break;
                            }
                        }
                        if (isChecked)
                            checkedItems.add(chosenContact);
                        else checkedItems.remove(chosenContact);
                    }
                }
        );

        if (contact.getAccountId() != 0) {
            ImageView accountIcon = new ImageView(context);
            accountIcon.setBackgroundResource(R.mipmap.ic_account);
            listItemView.addView(accountIcon);
        }

        return listItemView;
    }

    @Override
    public long getItemId(int position) {
        ContactDTO item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public HashSet<ContactDTO> getCheckedItems() {
        return checkedItems;
    }

}