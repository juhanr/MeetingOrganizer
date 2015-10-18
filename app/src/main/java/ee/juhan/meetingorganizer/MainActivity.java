package ee.juhan.meetingorganizer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.NewMeetingFragment;
import ee.juhan.meetingorganizer.fragments.RegistrationFragment;
import ee.juhan.meetingorganizer.fragments.dialogs.LoadingFragment;
import ee.juhan.meetingorganizer.rest.RestClient;

public class MainActivity extends Activity {

	private static final int NEW_MEETING_DRAWER_INDEX = 1;
	private static final int ONGOING_MEETINGS_DRAWER_INDEX = 2;
	private static final int FUTURE_MEETINGS_DRAWER_INDEX = 3;
	private static final int PAST_MEETINGS_DRAWER_INDEX = 4;
	private static final int INVITATIONS_DRAWER_INDEX = 5;
	private static final int LOGIN_DRAWER_INDEX = 1;
	private static final int REGISTRATION_DRAWER_INDEX = 2;
	private Map<String, Integer> drawerItemsHashMap = new HashMap<>();
	private DrawerLayout drawerLayout;
	private ListView drawerListView;
	private ActionBarDrawerToggle drawerToggle;
	private String[] drawerItems;
	private int currentDrawerItemPosition;
	private TextView emailTextView;
	private SharedPreferences sharedPref;
	private Toast toast;
	private boolean isLoggedIn;
	private LoadingFragment loadingFragment;
	private int backStackCounter = 0;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		checkIfLoggedIn();
	}

	@Override
	protected final void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public final void onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawers();
		} else if (getFragmentManager().getBackStackEntryCount() - backStackCounter > 0) {
			getFragmentManager().popBackStack();
		} else {
			moveTaskToBack(true);
		}
	}

	@Override
	public final boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (isLoggedIn) {
			getMenuInflater().inflate(R.menu.menu_main, menu);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (id == R.id.action_logout) {
			logOut();
		} else {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public final void setTitle(CharSequence title) {
		getActionBar().setTitle(title);
	}

	private void checkIfLoggedIn() {
		if (getSID() == null) {
			setUpDrawer();
			setEmail(getString(R.string.textview_not_logged_in));
		} else {
			RestClient.setSID(getSID());
			isLoggedIn = true;
			setUpDrawer();
			setEmail(sharedPref.getString("email", ""));
		}
	}

	private void createDrawerItemsHashMap() {
		drawerItemsHashMap.clear();
		drawerItems =
				isLoggedIn ? getResources().getStringArray(R.array.array_drawer_items_online) :
						getResources().getStringArray(R.array.array_drawer_items_offline);
		Integer position = 1;
		for (String item : drawerItems) {
			drawerItemsHashMap.put(item, position);
			position++;
		}
	}

	public final Integer getDrawerItemPosition(String item) {
		return drawerItemsHashMap.get(item);
	}

	public final String getSID() {
		return sharedPref.getString("sid", null);
	}

	public final Integer getUserId() {
		return sharedPref.getInt("userId", 0);
	}

	public final void logIn(String email, String sid, Integer userId) {
		sharedPref.edit().putString("email", email).putString("sid", sid).putInt("userId", userId)
				.apply();
		RestClient.setSID(sid);
		setEmail(email);
		isLoggedIn = true;
		setUpDrawer();
	}

	private void logOut() {
		sharedPref.edit().putString("email", null).putString("sid", null).putInt("userId", 0)
				.apply();
		RestClient.setSID(null);
		setEmail(getString(R.string.textview_not_logged_in));
		isLoggedIn = false;
		setUpDrawer();
	}

	private void setEmail(String email) {
		if (emailTextView != null) {
			emailTextView.setText(email);
		}
	}

	private void setUpDrawer() {
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerListView = (ListView) findViewById(R.id.left_drawer);
		createDrawerItemsHashMap();

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerListView.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, new Toolbar(this),
				R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu();
			}

			public void onDrawerClosed(View view) {
				invalidateOptionsMenu();
			}
		};

		if (emailTextView == null) {
			LayoutInflater inflater = getLayoutInflater();
			ViewGroup mTop = (ViewGroup) inflater
					.inflate(R.layout.drawer_list_header, drawerListView, false);
			emailTextView = (TextView) mTop.findViewById(R.id.email_textView);
			drawerListView.addHeaderView(mTop);
		}

		drawerLayout.setDrawerListener(drawerToggle);
		drawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerItems));

		currentDrawerItemPosition = 0;
		selectDrawerItem(1, true);
	}

	public final void selectDrawerItem(int position) {
		selectDrawerItem(position, false);
	}

	public final void selectDrawerItem(int position, boolean resetBackStackCounter) {
		if (position == currentDrawerItemPosition || position == 0) {
			setDrawerItem(currentDrawerItemPosition);
			drawerLayout.closeDrawer(drawerListView);
			return;
		}

		if (isLoggedIn) {
			selectOnlineDrawerItem(position, resetBackStackCounter);
		} else {
			selectOfflineDrawerItem(position, resetBackStackCounter);
		}
		drawerLayout.closeDrawer(drawerListView);
	}

	private void selectOnlineDrawerItem(int position, boolean resetBackStackCounter) {
		String title = drawerItems[position - 1];
		switch (position) {
			case NEW_MEETING_DRAWER_INDEX:
				changeFragment(new NewMeetingFragment(), resetBackStackCounter);
				break;
			case ONGOING_MEETINGS_DRAWER_INDEX:
				MeetingsListFragment meetingsListFragment =
						new MeetingsListFragment(title, MeetingsListFragment.ONGOING_MEETINGS);
				changeFragment(meetingsListFragment, resetBackStackCounter);
				break;
			case FUTURE_MEETINGS_DRAWER_INDEX:
				meetingsListFragment =
						new MeetingsListFragment(title, MeetingsListFragment.FUTURE_MEETINGS);
				changeFragment(meetingsListFragment, resetBackStackCounter);
				break;
			case PAST_MEETINGS_DRAWER_INDEX:
				meetingsListFragment =
						new MeetingsListFragment(title, MeetingsListFragment.PAST_MEETINGS);
				changeFragment(meetingsListFragment, resetBackStackCounter);
				break;
			case INVITATIONS_DRAWER_INDEX:
				meetingsListFragment =
						new MeetingsListFragment(title, MeetingsListFragment.INVITATIONS);
				changeFragment(meetingsListFragment, resetBackStackCounter);
				break;
		}
	}

	private void selectOfflineDrawerItem(int position, boolean resetBackStackCounter) {
		switch (position) {
			case LOGIN_DRAWER_INDEX:
				changeFragment(new LoginFragment(), resetBackStackCounter);
				break;
			case REGISTRATION_DRAWER_INDEX:
				changeFragment(new RegistrationFragment(), resetBackStackCounter);
				break;
		}
	}

	public final void setDrawerItem(int position) {
		currentDrawerItemPosition = position;
		drawerListView.setItemChecked(position, true);
	}

	public final void changeFragment(Fragment fragment) {
		changeFragment(fragment, false);
	}

	public final void changeFragment(Fragment fragment, boolean resetBackStackCounter) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right,
				R.anim.slide_out_left);
		ft.replace(R.id.fragment_container, fragment);
		if (resetBackStackCounter) {
			backStackCounter = getFragmentManager().getBackStackEntryCount();
		} else {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

	public final void refreshFragment(Fragment fragment) {
		getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
	}

	/**
	 * Displays a toast message with the given message.
	 *
	 * @param message
	 * 		message string
	 */
	public final void showToastMessage(final String message) {
		cancelToastMessage();
		toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 * Cancels the currently displayed toast message.
	 */
	public final void cancelToastMessage() {
		if (toast != null) {
			toast.cancel();
		}
	}

	public final void showLoadingFragment() {
		loadingFragment = new LoadingFragment();
		loadingFragment.show(getFragmentManager(), "LoaderFragment");
	}

	public final void dismissLoadingFragment() {
		if (loadingFragment != null) {
			loadingFragment.dismiss();
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectDrawerItem(position);
		}
	}

}
