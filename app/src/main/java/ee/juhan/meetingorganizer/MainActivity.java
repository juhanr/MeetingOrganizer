package ee.juhan.meetingorganizer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Build;
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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ee.juhan.meetingorganizer.fragments.ChooseContactsFragment;
import ee.juhan.meetingorganizer.fragments.ChooseLocationFragment;
import ee.juhan.meetingorganizer.fragments.HistoryFragment;
import ee.juhan.meetingorganizer.fragments.InvitationsFragment;
import ee.juhan.meetingorganizer.fragments.LoginFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.fragments.NewMeetingFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantInfoFragment;
import ee.juhan.meetingorganizer.fragments.ParticipantsListFragment;
import ee.juhan.meetingorganizer.fragments.RegistrationFragment;
import ee.juhan.meetingorganizer.fragments.listeners.EditTextFocusListener;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.rest.RestClient;

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
	private FloatingActionButton newMeetingButton;

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
		newMeetingButton = (FloatingActionButton) findViewById(R.id.fab);
		if (newMeetingButton != null) {
			newMeetingButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					changeFragmentToNewMeeting();
				}
			});
		}

		// set up fragment manager listener
		final FragmentManager fragmentManager = getFragmentManager();
		fragmentManager
				.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
					@Override
					public void onBackStackChanged() {
						Fragment fragment =
								fragmentManager.findFragmentById(R.id.fragment_container);
						if (fragment != null) {
							currentFragmentClass = fragment.getClass();
						}
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

	private void checkIfLoggedIn() {
		resetBackStack();
		if (getSID() == null) { // logged out
			setUpDrawer();
			setEmail(getString(R.string.textview_not_logged_in));
			changeFragmentToLogIn();
			showNewMeetingButton(false);
		} else { // logged in
			RestClient.setSID(getSID());
			isLoggedIn = true;
			setUpDrawer();
			setEmail(sharedPref.getString("email", ""));
			changeFragmentToMeetings();
			showNewMeetingButton(true);
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
		showNewMeetingButton(true);
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
		showNewMeetingButton(false);
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
		backStackCounter = getFragmentManager().getBackStackEntryCount();
	}

	private void changeFragment(Fragment fragment) {
		if (fragment.getClass().equals(currentFragmentClass)) {
			return;
		}
		currentFragmentClass = fragment.getClass();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right,
				R.anim.slide_out_left);
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

	public final void changeFragmentToNewMeeting() {
		changeFragment(new NewMeetingFragment());
	}

	public final void changeFragmentToMeetingInfo(MeetingDTO meeting) {
		changeFragment(MeetingInfoFragment.newInstance(meeting));
	}

	public final void changeFragmentToParticipantInfo(ParticipantDTO participant) {
		changeFragment(new ParticipantInfoFragment(participant));
	}

	public final void changeFragmentToChooseContacts() {
		changeFragment(new ChooseContactsFragment());
	}

	public final void changeFragmentToChooseLocation() {
		changeFragment(new ChooseLocationFragment());
	}

	public final void changeFragmentToParticipantsList(List<ParticipantDTO> participantList) {
		changeFragment(new ParticipantsListFragment(participantList));
	}


	public final void refreshFragment(Fragment fragment) {
		try {
			getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
		} catch (IllegalStateException e) {
			Log.e("ERROR", "refreshFragment() called in illegal state.");
		}
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

	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			fragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
			fragmentContainer.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							fragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});

			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							progressView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			fragmentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private void showNewMeetingButton(boolean show) {
		if (show) {
			newMeetingButton.setVisibility(View.VISIBLE);
		} else {
			newMeetingButton.setVisibility(View.GONE);
		}
	}

	public final void setupEditTextFocusListeners(View view) {
		if (view instanceof EditText) {
			view.setOnFocusChangeListener(new EditTextFocusListener(this));
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupEditTextFocusListeners(innerView);
			}
		}
	}

}
