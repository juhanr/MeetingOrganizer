package ee.juhan.meetingorganizer.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.HistoryFragment;
import ee.juhan.meetingorganizer.fragments.InvitationsFragment;
import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantInfoFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantsListFragment;
import ee.juhan.meetingorganizer.fragments.RegistrationFragment;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.UIUtil;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private DrawerLayout drawer;
	private TextView drawerEmailView;
	private TextView drawerNameView;
	private SharedPreferences sharedPref;
	private Toast toast;
	private boolean isLoggedIn;
	private int backStackCounter = 0;
	private boolean resetBackStackCounter;
	private ActionBar actionBar;
	private Class currentFragmentClass;
	private NavigationView navigationView;
	private View progressView;
	private View fragmentContainer;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		progressView = findViewById(R.id.progress_bar);
		fragmentContainer = findViewById(R.id.fragment_container);
		setupListeners();
		checkIfLoggedIn();
	}

	private void setupListeners() {
		// set up floating action button listener
		FloatingActionButton newMeetingFAB =
				(FloatingActionButton) findViewById(R.id.fab_new_meeting);
		if (newMeetingFAB != null) {
			newMeetingFAB.setOnClickListener(view -> {
				Intent myIntent = new Intent(getBaseContext(), NewMeetingActivity.class);
				//					myIntent.putExtra("key", value); //Optional parameters
				startActivityForResult(myIntent, 1);
			});
		}

		// set up fragment manager listener
		final FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.addOnBackStackChangedListener(() -> {
			Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
			if (fragment != null) {
				currentFragmentClass = fragment.getClass();
			}
		});
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

	@Override
	public final void setTitle(CharSequence title) {
		actionBar.setTitle(title);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				if (resultCode == RESULT_OK) {
					Bundle res = data.getExtras();
					String result = res.getString("param_result");
				}
				break;
		}
	}

	private void checkIfLoggedIn() {
		resetBackStack();
		if (getSID() == null) { // logged out
			setUpDrawer();
			setEmail(getString(R.string.textview_not_logged_in));
			changeFragmentToLogIn();
		} else { // logged in
			RestClient.setSID(getSID());
			isLoggedIn = true;
			setUpDrawer();
			setEmail(sharedPref.getString("email", ""));
			changeFragmentToMeetings();
		}
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
		resetBackStack();
		changeFragmentToMeetings();
	}

	private void logOut() {
		sharedPref.edit().putString("email", null).putString("sid", null).putInt("userId", 0)
				.apply();
		RestClient.setSID(null);
		setEmail(getString(R.string.textview_not_logged_in));
		isLoggedIn = false;
		setUpDrawer();
		resetBackStack();
		changeFragmentToLogIn();
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
		} else if (id == R.id.nav_settings) {

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

	public final void resetBackStack() {
		resetBackStackCounter = true;
	}

	private void changeFragment(Fragment fragment) {
		if (fragment.getClass().equals(currentFragmentClass)) {
			return;
		}
		currentFragmentClass = fragment.getClass();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right,
				R.animator.slide_in_right, R.animator.slide_out_left);
		ft.replace(R.id.fragment_container, fragment);
		if (resetBackStackCounter) {
			backStackCounter = getFragmentManager().getBackStackEntryCount();
			resetBackStackCounter = false;
		} else {
			ft.addToBackStack(null);
		}
		ft.commit();
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

	public final void changeFragmentToMeetingInfo(MeetingDTO meeting) {
		changeFragment(MeetingInfoFragment.newInstance(meeting));
	}

	public final void changeFragmentToParticipantInfo(ParticipantDTO participant) {
		changeFragment(new ParticipantInfoFragment(participant));
	}

	public final void changeFragmentToParticipantsList(List<ParticipantDTO> participantList) {
		changeFragment(new ParticipantsListFragment(participantList));
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

}
