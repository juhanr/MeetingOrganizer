package ee.juhan.meetingorganizer.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.ContactsAdapter;
import ee.juhan.meetingorganizer.models.server.ContactDTO;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.models.server.ServerResult;
import ee.juhan.meetingorganizer.rest.RestClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChooseContactsFragment extends Fragment {

    private final String title = "Invite contacts";
    private MainActivity activity;
    private ContactsAdapter adapter;
    private List<ContactDTO> contactsList = new ArrayList<ContactDTO>();
    private LinearLayout chooseContactsLayout;

    private boolean participantsWithoutAccount;

    public ChooseContactsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        createContactsMap();
        if (contactsList.size() == 0) {
            chooseContactsLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) chooseContactsLayout
                    .findViewById(R.id.info_text);
            infoText.setText("No contacts found.");
        } else {
            chooseContactsLayout = (LinearLayout) inflater.inflate(R.layout.fragment_choose_contacts, container, false);
            checkContactsFromServer();
            setButtonListeners();

            ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.listView);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    // do something
                }
            });
        }
        return chooseContactsLayout;
    }

    private void checkContactsFromServer() {
        activity.showLoadingFragment();
        RestClient.get().checkContactsRequest(contactsList, activity.getUserId(),
                new Callback<List<ContactDTO>>() {
                    @Override
                    public void success(final List<ContactDTO> serverResponse, Response response) {
                        activity.dismissLoadingFragment();
                        contactsList = serverResponse;
                        refreshListView();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.dismissLoadingFragment();
                        activity.showToastMessage("Server response fail.");
                    }
                });
    }

    private void setButtonListeners() {
        Button continueButton = (Button) chooseContactsLayout
                .findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetParticipants();
                addContactsAsParticipants();
                addLeaderInfo();
                checkParticipantsWithoutAccount();
            }
        });

    }

    private void resetParticipants() {
        NewMeetingFragment.newMeetingModel.setParticipants(new HashSet<ParticipantDTO>());
        participantsWithoutAccount = false;
    }

    private void addContactsAsParticipants() {
        for (ContactDTO checkedContact : adapter.getCheckedItems()) {
            participantsWithoutAccount = checkedContact.getAccountId() == 0;
            ParticipantDTO participant = new ParticipantDTO(
                    checkedContact.getAccountId(), checkedContact.getName(),
                    checkedContact.getEmail(), checkedContact.getPhoneNumber());
            NewMeetingFragment.newMeetingModel.addParticipant(participant);
        }
    }

    private void addLeaderInfo() {
        NewMeetingFragment.newMeetingModel.setLeaderId(activity.getUserId());
        ParticipantDTO participant = new ParticipantDTO(
                NewMeetingFragment.newMeetingModel.getLeaderId(),
                ParticipationAnswer.PARTICIPATING);
        NewMeetingFragment.newMeetingModel.addParticipant(participant);
    }

    private void checkParticipantsWithoutAccount() {
        if (participantsWithoutAccount) {
            showSMSDialog();
        } else {
            sendNewMeetingRequest();
        }
    }

    private void showSMSDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
//                        sendInvitationSMS();
                        sendNewMeetingRequest();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        sendNewMeetingRequest();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Some of the contacts don't have an account.\n" +
                "Would you like to invite them via SMS?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void sendInvitationSMS() {
        String message = "I would like to invite you to a meeting via Meeting Organizer. " +
                "Please register to see the invitation.";
        for (ParticipantDTO participant : NewMeetingFragment.newMeetingModel.getParticipants()) {
            if (participant.getAccountId() == 0) {
                SmsManager.getDefault().sendTextMessage(
                        participant.getPhoneNumber(), null, message, null, null);
            }
        }
    }

    private void sendNewMeetingRequest() {
        activity.showLoadingFragment();
        RestClient.get().newMeetingRequest(NewMeetingFragment.newMeetingModel,
                new Callback<ServerResult>() {
                    @Override
                    public void success(ServerResult serverResponse, Response response) {
                        activity.dismissLoadingFragment();
                        if (serverResponse != null && serverResponse == ServerResult.SUCCESS) {
                            activity.showToastMessage("New meeting created!");
                            activity.changeFragment(new MeetingInfoFragment(
                                    NewMeetingFragment.newMeetingModel), false);
                            NewMeetingFragment.newMeetingModel = new MeetingDTO();
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.dismissLoadingFragment();
                        activity.showToastMessage("Server response fail.");
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
            int nameColumn = numbersCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneColumn = numbersCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (numbersCur.moveToNext()) {
                name = numbersCur.getString(nameColumn);
                phoneNumber = numbersCur.getString(phoneColumn);
                contactsList.add(new ContactDTO(name, null, phoneNumber));
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
                for (int i = 0; i < contactsList.size(); i++) {
                    ContactDTO contact = contactsList.get(i);
                    if (contact.getName().equals(name)) {
                        contact.setEmail(email);
                        contactsList.set(i, contact);
                        break;
                    }
                }
            }
        }
        cur.close();
    }

    public void refreshListView() {
        ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.listView);
        adapter = new ContactsAdapter(getActivity(), contactsList);
        listview.setAdapter(adapter);
    }

}