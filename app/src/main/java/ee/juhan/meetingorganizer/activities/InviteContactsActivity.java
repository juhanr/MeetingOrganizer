package ee.juhan.meetingorganizer.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter;
import ee.juhan.meetingorganizer.models.server.Contact;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class InviteContactsActivity extends AppCompatActivity {

	private static final int CURSOR_NAME_INDEX = 1;
	private static final int CURSOR_EMAIL_INDEX = 3;
	private static ContactsAdapter contactsAdapter;
	private List<Contact> contactsList = new ArrayList<>();
	private ViewGroup chooseContactsLayout;
	private ListView contactsListView;
	private int participantsWithoutAccount = 0;
	private View progressView;
	private Activity activity = this;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.title_invite_contacts));
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		createContactsMap();
		if (contactsList.isEmpty()) {
			setContentView(R.layout.fragment_no_data);
			chooseContactsLayout = (ViewGroup) findViewById(R.id.layout_no_data);
			if (chooseContactsLayout != null) {
				TextView infoText = (TextView) chooseContactsLayout.findViewById(R.id.info_text);
				infoText.setText(getString(R.string.contacts_no_contacts));
			}
			progressView = findViewById(R.id.progress_bar);
		} else {
			setContentView(R.layout.activity_invite_contacts);
			chooseContactsLayout = (ViewGroup) findViewById(R.id.activity_invite_contacts);
			contactsListView = (ListView) findViewById(R.id.contacts_list);
			progressView = findViewById(R.id.progress_bar);
			checkContactsFromServer();
		}
		setButtonListeners();
	}

	@Override
	public void onPause() {
		addContactsAsParticipants();
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	public void showProgress(final boolean show) {
		UIUtil.showProgress(this, progressView, contactsListView, show);
	}

	public final int getAccountId() {
		return PreferenceManager.getDefaultSharedPreferences(this).getInt("accountId", 0);
	}

	private void checkContactsFromServer() {
		showProgress(true);
		RestClient.get()
				.checkContactsRequest(contactsList, getAccountId(), new Callback<List<Contact>>() {
					@Override
					public void success(final List<Contact> serverResponse, Response response) {
						showProgress(false);
						removeCurrentUserFromContacts(serverResponse);
						refreshListView();
					}

					@Override
					public void failure(RetrofitError error) {
						showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

	private void removeCurrentUserFromContacts(List<Contact> newList) {
		int userId = getAccountId();
		for (int i = 0; i < newList.size(); i++) {
			Contact contact = newList.get(i);
			if (contact.getAccountId() == userId) {
				newList.remove(i);
				break;
			}
		}
		contactsList = newList;
	}

	private void setButtonListeners() {
		FloatingActionButton confirmFAB = (FloatingActionButton) findViewById(R.id.fab_confirm);
		if (confirmFAB != null) {
			confirmFAB.setOnClickListener(view -> finish());
		}
	}

	private void addContactsAsParticipants() {
		if (contactsAdapter == null) {
			return;
		}
		NewMeetingActivity.getNewMeetingModel().getParticipants().clear();
		participantsWithoutAccount = 0;
		for (Contact checkedContact : contactsAdapter.getCheckedItems()) {
			if (checkedContact.getAccountId() == 0) {
				participantsWithoutAccount++;
			}
			Participant participant =
					new Participant(checkedContact.getAccountId(), checkedContact.getName(),
							checkedContact.getEmail(), checkedContact.getPhoneNumber());
			NewMeetingActivity.getNewMeetingModel().addParticipant(participant);
		}
	}

	private void createContactsMap() {
		ContentResolver cr = getContentResolver();
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER};

		Cursor numbersCur = cr.query(uri, projection, null, null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

		assert numbersCur != null;
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
				contactsList.add(new Contact(name, null, phoneNumber));
			}
		}
		numbersCur.close();
		insertEmailsToMap();
	}

	private void insertEmailsToMap() {
		ContentResolver cr = getContentResolver();
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
		assert cur != null;
		if (!cur.moveToFirst()) {
			cur.close();
			return;
		}
		while (cur.moveToNext()) {
			String name = cur.getString(CURSOR_NAME_INDEX);
			String email = cur.getString(CURSOR_EMAIL_INDEX);
			for (int i = 0; i < contactsList.size(); i++) {
				Contact contact = contactsList.get(i);
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
		ListView listview = (ListView) chooseContactsLayout.findViewById(R.id.contacts_list);
		listview.setItemsCanFocus(true);
		listview.setOnItemClickListener((parent, view, position, id) -> {
			GroupedListAdapter.GroupedListItem item = contactsAdapter.getItem(position);
			if (!item.isGroupItem()) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.chk_contact);
				contactsAdapter.checkContact(checkBox, (Contact) item.getObject());
			}
		});
		if (contactsAdapter == null) {
			contactsAdapter = new ContactsAdapter(this, getGroupedListItems());
		}
		listview.setAdapter(contactsAdapter);
	}

	private List<GroupedListAdapter.GroupedListItem> getGroupedListItems() {
		List<GroupedListAdapter.GroupedListItem> contactsWithAccount = new ArrayList<>();
		contactsWithAccount.add(new GroupedListAdapter.GroupedListItem("Contacts with account"));
		List<GroupedListAdapter.GroupedListItem> contactsWithoutAccount = new ArrayList<>();
		contactsWithoutAccount
				.add(new GroupedListAdapter.GroupedListItem("Contacts without account"));
		for (Contact contact : contactsList) {
			if (contact.getAccountId() == 0) {
				contactsWithoutAccount.add(new GroupedListAdapter.GroupedListItem<>(contact));
			} else {
				contactsWithAccount.add(new GroupedListAdapter.GroupedListItem<>(contact));
			}
		}

		List<GroupedListAdapter.GroupedListItem> groupedListItems = new ArrayList<>();
		if (contactsWithAccount.size() > 1) {
			groupedListItems.addAll(contactsWithAccount);
		}
		if (contactsWithoutAccount.size() > 1) {
			groupedListItems.addAll(contactsWithoutAccount);
		}
		return groupedListItems;
	}

	private class ContactsAdapter extends GroupedListAdapter implements View.OnTouchListener {

		private Set<Contact> checkedItems = new HashSet<>();
		private Map<CheckBox, Integer> checkBoxPositionMap = new HashMap<>();

		public ContactsAdapter(Context context, List<GroupedListItem> listItems) {
			super(context, R.layout.list_item_contact, listItems);
		}

		@Override
		protected void populateLayout() {
			Contact contact = (Contact) super.getCurrentItem().getObject();
			CheckBox checkBox = (CheckBox) super.getLayout().findViewById(R.id.chk_contact);
			checkBoxPositionMap.put(checkBox, getPosition(super.getCurrentItem()));
			checkBox.setOnTouchListener(this);
			if (checkedItems.contains(contact)) {
				checkBox.setCheckedImmediately(true);
			}
			TextView nameTextView =
					(TextView) super.getLayout().findViewById(R.id.txt_contact_name);
			nameTextView.setText(contact.getName());
			TextView phoneTextView =
					(TextView) super.getLayout().findViewById(R.id.txt_contact_phone);
			phoneTextView.setText(contact.getPhoneNumber());
		}

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				CheckBox checkBox = (CheckBox) view;
				int position = checkBoxPositionMap.get(checkBox);
				Contact contact = (Contact) contactsAdapter.getItem(position).getObject();
				checkContact(checkBox, contact);
				return true;
			}
			return false;
		}

		public Set<Contact> getCheckedItems() {
			return checkedItems;
		}

		public void checkContact(CheckBox checkBox, Contact contact) {
			if (checkBox.isChecked()) {
				checkBox.setChecked(false);
				checkedItems.remove(contact);
			} else {
				checkBox.setChecked(true);
				checkedItems.add(contact);
			}
		}
	}

}
