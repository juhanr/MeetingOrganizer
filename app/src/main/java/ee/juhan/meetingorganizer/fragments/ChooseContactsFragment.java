package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.CheckBoxAdapter;
import ee.juhan.meetingorganizer.fragments.dialogs.YesNoFragment;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.models.server.ContactDTO;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.rest.RestClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChooseContactsFragment extends Fragment {

	private static final int CURSOR_NAME_INDEX = 1;
	private static final int CURSOR_EMAIL_INDEX = 3;
	private String title;
	private MainActivity activity;
	private ContactsAdapter adapter;
	private List<ContactDTO> contactsList = new ArrayList<>();
	private ViewGroup chooseContactsLayout;
	private int participantsWithoutAccount = 0;

	public ChooseContactsFragment() {}

	public static ChooseContactsFragment newInstance() {
		return new ChooseContactsFragment();
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_invite_contacts);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		createContactsMap();
		if (contactsList.isEmpty()) {
			chooseContactsLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
			TextView infoText = (TextView) chooseContactsLayout.findViewById(R.id.info_text);
			infoText.setText(getString(R.string.textview_no_contacts));
		} else {
			chooseContactsLayout = (ViewGroup) inflater
					.inflate(R.layout.fragment_choose_contacts, container, false);
			checkContactsFromServer();
			setButtonListeners();
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
						updateContactsList(serverResponse);
						refreshListView();
					}

					@Override
					public void failure(RetrofitError error) {
						activity.dismissLoadingFragment();
						activity.showToastMessage(getString(R.string.toast_server_fail));
					}
				});
	}

	private void updateContactsList(List<ContactDTO> newList) {
		int userId = activity.getUserId();
		for (int i = 0; i < newList.size(); i++) {
			ContactDTO contact = newList.get(i);
			if (contact.getAccountId() == userId) {
				newList.remove(i);
				break;
			}
		}
		contactsList = newList;
	}

	private void setButtonListeners() {
		Button continueButton = (Button) chooseContactsLayout.findViewById(R.id.continue_button);
		continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (adapter.getCheckedItems().size() > 0) {
					resetParticipants();
					addLeaderInfo();
					addContactsAsParticipants();
					checkParticipantsWithoutAccount();
				} else {
					activity.showToastMessage(getString(R.string.toast_please_choose_contacts));
				}
			}
		});

	}

	private void resetParticipants() {
		NewMeetingFragment.getNewMeetingModel().getParticipants().clear();
		participantsWithoutAccount = 0;
	}

	private void addContactsAsParticipants() {
		for (ContactDTO checkedContact : adapter.getCheckedItems()) {
			if (checkedContact.getAccountId() == 0) {
				participantsWithoutAccount++;
			}
			ParticipantDTO participant =
					new ParticipantDTO(checkedContact.getAccountId(), checkedContact.getName(),
							checkedContact.getEmail(), checkedContact.getPhoneNumber());
			NewMeetingFragment.getNewMeetingModel().addParticipant(participant);
		}
	}

	private void addLeaderInfo() {
		NewMeetingFragment.getNewMeetingModel().setLeaderId(activity.getUserId());
		ParticipantDTO participant =
				new ParticipantDTO(NewMeetingFragment.getNewMeetingModel().getLeaderId(),
						ParticipationAnswer.PARTICIPATING, MyLocationListener.getMyLocation());
		NewMeetingFragment.getNewMeetingModel().addParticipant(participant);
	}

	private void checkParticipantsWithoutAccount() {
		if (participantsWithoutAccount > 0) {
			showAskSMSDialog();
		} else {
			sendNewMeetingRequest();
		}
	}

	private void showAskSMSDialog() {
		final YesNoFragment dialog = new YesNoFragment();
		dialog.setMessage(
				participantsWithoutAccount + getString(R.string.textview_info_invite_via_sms));
		dialog.setPositiveButton(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showWriteSMSDialog();
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNewMeetingRequest();
				dialog.dismiss();
			}
		});
		dialog.hideInput();
		dialog.show(getFragmentManager(), "YesNoFragment");
	}

	private void showWriteSMSDialog() {
		final YesNoFragment dialog = new YesNoFragment();
		dialog.setMessage(getString(R.string.textview_please_write_sms));
		dialog.setInputText(getString(R.string.message_invite_via_sms));
		dialog.setPositiveButton(getString(R.string.button_send_sms), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendInvitationSMS(dialog.getInputValue());
				sendNewMeetingRequest();
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(getString(R.string.button_cancel), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show(getFragmentManager(), "YesNoFragment");
	}

	private void sendInvitationSMS(String smsMessage) {
		SmsManager smsManager = SmsManager.getDefault();
		for (ParticipantDTO participant : NewMeetingFragment.getNewMeetingModel()
				.getParticipants()) {
			if (participant.getAccountId() == 0) {
				smsManager.sendTextMessage(participant.getPhoneNumber(), null, smsMessage, null,
						null);
			}
		}
	}

	private void sendNewMeetingRequest() {
		activity.showLoadingFragment();
		RestClient.get().newMeetingRequest(NewMeetingFragment.getNewMeetingModel(),
				new Callback<MeetingDTO>() {
					@Override
					public void success(MeetingDTO serverResponse, Response response) {
						activity.dismissLoadingFragment();
						activity.showToastMessage(getString(R.string.toast_meeting_created));
						activity.changeFragment(MeetingInfoFragment.newInstance(serverResponse),
								false);
						NewMeetingFragment.setNewMeetingModel(new MeetingDTO());
					}

					@Override
					public void failure(RetrofitError error) {
						activity.dismissLoadingFragment();
						activity.showToastMessage(getString(R.string.toast_server_fail));
					}
				});
	}

	private void createContactsMap() {
		ContentResolver cr = getActivity().getContentResolver();
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER};

		Cursor numbersCur = cr.query(uri, projection, null, null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

		if (numbersCur.moveToFirst()) {
			String name;
			String phoneNumber;
			int nameColumn =
					numbersCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
			int phoneColumn =
					numbersCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			while (numbersCur.moveToNext()) {
				name = numbersCur.getString(nameColumn);
				phoneNumber = numbersCur.getString(phoneColumn);
				contactsList.add(new ContactDTO(name, null, phoneNumber));
			}
		}
		numbersCur.close();
		insertEmailsToMap();
	}

	private void insertEmailsToMap() {
		Context context = getActivity();
		ContentResolver cr = context.getContentResolver();
		String[] projection = new String[]{ContactsContract.RawContacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID,
				ContactsContract.CommonDataKinds.Email.DATA,
				ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
		String order = "CASE WHEN " + ContactsContract.Contacts.DISPLAY_NAME +
				" NOT LIKE '%@%' THEN 1 ELSE 2 END, " +
				ContactsContract.Contacts.DISPLAY_NAME + ", " +
				ContactsContract.CommonDataKinds.Email.DATA + " COLLATE NOCASE";
		String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
		Cursor cur =
				cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, filter,
						null, order);
		if (!cur.moveToFirst()) {
			cur.close();
			return;
		}
		while (cur.moveToNext()) {
			String name = cur.getString(CURSOR_NAME_INDEX);
			String email = cur.getString(CURSOR_EMAIL_INDEX);
			for (int i = 0; i < contactsList.size(); i++) {
				ContactDTO contact = contactsList.get(i);
				if (contact.getName().equals(name)) {
					contact.setEmail(email);
					contactsList.set(i, contact);
					break;
				}
			}
		}
		cur.close();
	}

	private void refreshListView() {
		ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.listView);
		adapter = new ContactsAdapter(getActivity(), contactsList);
		listview.setAdapter(adapter);
	}

	private class ContactsAdapter extends CheckBoxAdapter<ContactDTO> {

		public ContactsAdapter(Context context, List<ContactDTO> objects) {
			super(context, objects);
		}

		@Override
		protected void setUpCheckBox() {
			ContactDTO contact = super.getCurrentItem();
			if (super.getCheckedItems().contains(contact)) {
				super.getCheckBox().setChecked(true);
			}
			super.setCheckBoxText(contact.getName());
			if (contact.getAccountId() != 0) {
				super.addIcon(R.drawable.ic_account);
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			String contactName = buttonView.getText().toString();
			ContactDTO chosenContact = new ContactDTO();
			for (ContactDTO contact : super.getObjects()) {
				if (contact.getName().equals(contactName)) {
					chosenContact = contact;
					break;
				}
			}
			if (isChecked) {
				super.getCheckedItems().add(chosenContact);
			} else {
				super.getCheckedItems().remove(chosenContact);
			}
		}
	}

}