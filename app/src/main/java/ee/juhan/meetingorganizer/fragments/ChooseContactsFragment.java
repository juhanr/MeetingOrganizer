package ee.juhan.meetingorganizer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.ContactAdapter;

public class ChooseContactsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static HashMap<String, ArrayList<String>> contactsMap = new HashMap<String, ArrayList<String>>();
    private static ContactAdapter adapter;
    private static ArrayList<String> contactNames;
    private LinearLayout chooseContactsLayout;

    public ChooseContactsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        chooseContactsLayout = (LinearLayout) inflater.inflate(R.layout.fragment_choose_contacts, container, false);
        createContactsMap();
        contactNames = new ArrayList<String>(contactsMap.keySet());

        refreshListView();
        setButtonListeners();

        ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                // do something
            }
        });
        return chooseContactsLayout;
    }

    private void setButtonListeners() {
        Button continueButton = (Button) chooseContactsLayout
                .findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashSet<String> checkedContacts = adapter.getCheckedContacts();
                Log.d("DEBUG", checkedContacts.toString());
            }
        });

    }

    public void createContactsMap() {
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor numbersCur = cr.query(uri, // URI
                projection, // Which columns to return
                null, // Which rows to return (all rows)
                null, // Selection arguments (none)
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        if (numbersCur.moveToFirst()) {
            String name;
            String phoneNumber;
            String email;
            int nameColumn = numbersCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneColumn = numbersCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (numbersCur.moveToNext()) {
                name = numbersCur.getString(nameColumn);
                phoneNumber = numbersCur.getString(phoneColumn);
                ArrayList<String> list = new ArrayList<String>();
                list.add(phoneNumber);
                contactsMap.put(name, list);
            }
        }
        numbersCur.close();
        insertEmailsToMap();
    }

    public void insertEmailsToMap() {
        Context context = getActivity();
        ContentResolver cr = context.getContentResolver();
        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
        if (cur.moveToFirst()) {
            while (cur.moveToNext()) {
                String name = cur.getString(1);
                String email = cur.getString(3);
                ArrayList<String> list = contactsMap.get(name);
                if (list != null) {
                    list.add(email);
                    contactsMap.put(name, list);
                }
            }
        }
        cur.close();
    }

    public void refreshListView() {
        Collections.sort(contactNames);
        ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.listview);
        adapter = new ContactAdapter(getActivity(),
                contactNames);
        listview.setAdapter(adapter);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
