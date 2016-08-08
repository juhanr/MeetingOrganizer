package ee.juhan.meetingorganizer.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.HistoryFragment;
import ee.juhan.meetingorganizer.fragments.InvitationsFragment;
import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantInfoFragment;
import ee.juhan.meetingorganizer.fragments.RegistrationFragment;
import ee.juhan.meetingorganizer.interfaces.SnackbarActivity;
import ee.juhan.meetingorganizer.models.server.Account;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.services.LocationService;
import ee.juhan.meetingorganizer.services.MeetingUpdaterService;
import ee.juhan.meetingorganizer.util.GsonUtil;
import ee.juhan.meetingorganizer.util.UIUtil;

public class MainActivity extends AppCompatActivity
		implements SnackbarActivity, NavigationView.OnNavigationItemSelectedListener {

	private static SharedPreferences sharedPref;
	private static int accountId = 0;
	private DrawerLayout drawer;
	private TextView drawerEmailView;
	private TextView drawerNameView;
	private boolean isLoggedIn;
	private int backStackCounter = 0;
	private boolean resetBackStackCounter;
	private ActionBar actionBar;
	private Class currentFragmentClass;
	private NavigationView navigationView;
	private View progressView;
	private View fragmentContainer;
	private CoordinatorLayout coordinatorLayout;

	public static int getAccountId() {
		return accountId;
	}

	public static int getAccountIdFromPrefs() {
		accountId = sharedPref.getInt("accountId", 0);
		return accountId;
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		progressView = findViewById(R.id.progress_bar);
		fragmentContainer = findViewById(R.id.fragment_container);
		accountId = getAccountIdFromPrefs();
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.layout_main);
		setupListeners();
		checkIfLoggedIn();
		showLocationFab(false);
		showEmailFab(false);
		showSmsFab(false);
		showCallFab(false);
		new LocationService(this, false);
		MeetingUpdaterService.setContext(this);
	}

	@Override
	public final void setTitle(CharSequence title) {
		actionBar.setTitle(title);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				if (resultCode == RESULT_OK) {
					NewMeetingActivity.setNewMeetingModel(new Meeting());
					Bundle bundle = data.getExtras();
					Meeting meeting = GsonUtil.getJsonObjectFromBundle(bundle, Meeting.class,
							Meeting.class.getSimpleName());
					changeFragmentToMeetingInfoImmediately(meeting);
				}
				break;
		}
	}

	@Override
	public final void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (getFragmentManager().getBackStackEntryCount() - backStackCounter > 0) {
			getFragmentManager().popBackStack();
		} else {
			moveTaskToBack(true);
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.nav_meetings) {
			changeFragmentToMeetings();
		} else if (id == R.id.nav_invitations) {
			changeFragmentToInvitations();
		} else if (id == R.id.nav_history) {
			changeFragmentToHistory();
			//		} else if (id == R.id.nav_settings) {
		} else if (id == R.id.nav_log_out) {
			logOut();
		} else if (id == R.id.nav_log_in) {
			changeFragmentToLogIn();
		} else if (id == R.id.nav_registration) {
			changeFragmentToRegistration();
		}
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void showSnackbar(String message) {
		UIUtil.showSnackBar(coordinatorLayout, message);
	}

	private void setupListeners() {
		// set up floating action button listener
		FloatingActionButton newMeetingFAB =
				(FloatingActionButton) findViewById(R.id.fab_new_meeting);
		if (newMeetingFAB != null) {
			newMeetingFAB.setOnClickListener(view -> {
				Intent myIntent = new Intent(getBaseContext(), NewMeetingActivity.class);
				startActivityForResult(myIntent, 1);
			});
		}

		// set up fragment manager listener
		final FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.addOnBackStackChangedListener(() -> {
			Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
			if (fragment != null) {
				hideLocationFabIfNotMeetingFragment(fragment);
				currentFragmentClass = fragment.getClass();
			}
		});
	}

	private void checkIfLoggedIn() {
		resetBackStack();
		if (getSid() == null) { // logged out
			setUpDrawer();
			setEmail(getString(R.string.drawer_not_logged_in));
			changeFragmentToLogIn();
			showNewMeetingFab(false);
		} else { // logged in
			RestClient.setSid(getSid());
			isLoggedIn = true;
			setUpDrawer();
			setEmail(sharedPref.getString("email", ""));
			setName(sharedPref.getString("name", ""));
			changeFragmentToMeetings();
			showNewMeetingFab(true);
		}
	}

	public final String getSid() {
		return sharedPref.getString("sid", null);
	}

	public final String getPhoneNumber() {
		return sharedPref.getString("phone", null);
	}

	public final void logIn(String sid, Account account) {
		sharedPref.edit().putString("email", account.getEmail()).putString("sid", sid)
				.putInt("accountId", account.getAccountId()).putString("name", account.getName())
				.putString("phone", account.getPhoneNumber()).apply();
		accountId = getAccountIdFromPrefs();
		RestClient.setSid(sid);
		setEmail(account.getEmail());
		setName(account.getName());
		isLoggedIn = true;
		setUpDrawer();
		resetBackStack();
		changeFragmentToMeetings();
		showNewMeetingFab(true);
	}

	private void logOut() {
		sharedPref.edit().putString("email", null).putString("sid", null).putInt("accountId", 0)
				.putString("name", null).putString("phone", null).apply();
		accountId = getAccountIdFromPrefs();
		RestClient.setSid(null);
		setEmail(getString(R.string.drawer_not_logged_in));
		setName("");
		isLoggedIn = false;
		setUpDrawer();
		resetBackStack();
		changeFragmentToLogIn();
		showNewMeetingFab(false);
	}

	private void setEmail(String email) {
		if (drawerEmailView != null) {
			drawerEmailView.setText(email);
		}
	}

	private void setName(String name) {
		if (drawerNameView != null) {
			drawerNameView.setText(name);
		}
	}

	private void setUpDrawer() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		assert actionBar != null;
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle =
				new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open,
						R.string.drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		navigationView = (NavigationView) findViewById(R.id.nav_view);
		assert navigationView != null;
		navigationView.setNavigationItemSelectedListener(this);

		if (isLoggedIn) {
			navigationView.getMenu().setGroupVisible(R.id.nav_menu_logged_in, true);
			navigationView.getMenu().setGroupVisible(R.id.nav_menu_logged_out, false);
		} else {
			navigationView.getMenu().setGroupVisible(R.id.nav_menu_logged_out, true);
			navigationView.getMenu().setGroupVisible(R.id.nav_menu_logged_in, false);
		}

		LinearLayout headerView = (LinearLayout) navigationView.getHeaderView(0);
		drawerEmailView = (TextView) headerView.findViewById(R.id.nav_email);
		drawerNameView = (TextView) headerView.findViewById(R.id.nav_name);

	}

	public final void checkDrawerItem(int drawerItemId) {
		MenuItem item = navigationView.getMenu().findItem(drawerItemId);
		item.setCheckable(true);
		item.setChecked(true);
	}

	public final void resetBackStack() {
		resetBackStackCounter = true;
	}

	private void changeFragment(Fragment fragment, boolean showAnimation) {
		// Only MeetingInfoFragment is allowed to be replaced with itself.
		if (fragment.getClass().equals(currentFragmentClass) &&
				currentFragmentClass != MeetingInfoFragment.class) {
			return;
		}

		hideLocationFabIfNotMeetingFragment(fragment);
		currentFragmentClass = fragment.getClass();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (showAnimation) {
			ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right,
					R.animator.slide_in_right, R.animator.slide_out_left);
		}
		ft.replace(R.id.fragment_container, fragment);
		if (resetBackStackCounter) {
			backStackCounter = getFragmentManager().getBackStackEntryCount();
			resetBackStackCounter = false;
		} else {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

	private void changeFragment(Fragment fragment) {
		changeFragment(fragment, true);
	}

	public final void changeFragmentToMeetings() {
		changeFragment(new MeetingsListFragment());
	}

	public final void changeFragmentToInvitations() {
		changeFragment(new InvitationsFragment());
	}

	public final void changeFragmentToHistory() {
		changeFragment(new HistoryFragment());
	}

	public final void changeFragmentToLogIn() {
		changeFragment(new LoginFragment());
	}

	public final void changeFragmentToRegistration() {
		changeFragment(new RegistrationFragment());
	}

	public final void changeFragmentToMeetingInfo(Meeting meeting) {
		changeFragment(MeetingInfoFragment.newInstance(meeting));
	}

	public final void changeFragmentToMeetingInfoImmediately(Meeting meeting) {
		changeFragment(MeetingInfoFragment.newInstance(meeting), false);
	}

	public final void changeFragmentToParticipantInfo(Participant participant) {
		changeFragment(new ParticipantInfoFragment(participant));
	}

	public final void refreshFragment(Fragment fragment) {
		if (!fragment.getClass().equals(currentFragmentClass)) {
			return;
		}
		try {
			getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
		} catch (IllegalStateException e) {
			Log.e("ERROR", "refreshFragment() called in illegal state.");
		}
	}

	public void showProgress(final boolean show) {
		UIUtil.showProgress(this, progressView, fragmentContainer, show);
	}

	public void showFab(int fabId, boolean show) {
		FloatingActionButton fab = (FloatingActionButton) findViewById(fabId);
		if (show) {
			fab.show();
		} else {
			fab.hide();
		}
	}

	public void showNewMeetingFab(boolean show) {
		showFab(R.id.fab_new_meeting, show);
	}

	public void showLocationFab(boolean show) {
		showFab(R.id.fab_location, show);
	}

	public void showEmailFab(boolean show) {
		showFab(R.id.fab_email, show);
	}

	public void showSmsFab(boolean show) {
		showFab(R.id.fab_sms, show);
	}

	public void showCallFab(boolean show) {
		showFab(R.id.fab_call, show);
	}

	private void hideLocationFabIfNotMeetingFragment(Fragment fragment) {
		if (!fragment.getClass().equals(currentFragmentClass) &&
				currentFragmentClass == MeetingInfoFragment.class) {
			showLocationFab(false);
		}
	}
}