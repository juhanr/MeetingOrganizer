package ee.juhan.meetingorganizer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.gms.maps.MapsInitializer;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.models.MarkerData;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.services.MeetingUpdaterService;
import ee.juhan.meetingorganizer.util.GsonUtil;

public class ShowLocationActivity extends AppCompatActivity {

	public static String CHOOSE_LOCATION_ARG = "choose-location";
	private CustomMapFragment customMapFragment;
	private Meeting currentMeeting;
	private boolean chooseLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());
		getIntentExtras();
		setContentView(R.layout.activity_show_location);
		setUpActionBar();
		setUpMapLayout();
	}

	@Override
	public final void onResume() {
		super.onResume();
		MeetingUpdaterService.startMeetingUpdaterTask();
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

	private void getIntentExtras() {
		currentMeeting = GsonUtil.getJsonObjectFromIntentExtras(getIntent(), Meeting.class,
				Meeting.class.getSimpleName());
		chooseLocation = getIntent().getBooleanExtra(CHOOSE_LOCATION_ARG, false);
	}

	private void setUpActionBar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		assert actionBar != null;
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.layout_search_bar);
		actionBar.setDisplayShowTitleEnabled(false);
		EditText editSearch =
				(EditText) actionBar.getCustomView().findViewById(R.id.search_edittext);
		editSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				customMapFragment.searchMap(v.getText().toString());
				return true;
			}
			return false;
		});
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void setUpMapLayout() {
		customMapFragment = CustomMapFragment.newInstance();
		customMapFragment.setIsClickableMap(false);
		customMapFragment.addParticipantMarkers(currentMeeting.getParticipants());
		getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment)
				.commit();

		if (currentMeeting.getLocationType() == LocationType.SPECIFIC_LOCATION &&
				currentMeeting.getLocation() != null) {
			customMapFragment.addMarker(new MarkerData(currentMeeting));
		}

		if (chooseLocation) {
			// TODO
		}
	}

}
