package ee.juhan.meetingorganizer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;

public class ShowLocationActivity extends AppCompatActivity {

	public static final String MARKER_LATITUDE = "marker-latitude";
	public static final String MARKER_LONGITUDE = "marker-longitude";
	private CustomMapFragment customMapFragment = new CustomMapFragment();
	private LatLng markerLocation;

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	private void getIntentExtras() {
		double markerLatitude = getIntent().getDoubleExtra(MARKER_LATITUDE, 0);
		double markerLongitude = getIntent().getDoubleExtra(MARKER_LONGITUDE, 0);
		if (markerLatitude != 0 && markerLongitude != 0) {
			markerLocation = new LatLng(markerLatitude, markerLongitude);
		}

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
		addParticipantMarkers();
		getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment)
				.commit();

		if (markerLocation != null) {
			customMapFragment.setMarkerLocations(Collections.singletonList(markerLocation));
		}
	}

	private void addParticipantMarkers() {
		for (Participant participant : MeetingInfoFragment.getMeeting().getParticipants()) {
			if (participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND ||
					participant.getLocation() == null) {
				continue;
			}
			customMapFragment.addParticipantMarkerOptions(participant.getId(),
					participant.getMarkerOptions());
		}
	}

}
