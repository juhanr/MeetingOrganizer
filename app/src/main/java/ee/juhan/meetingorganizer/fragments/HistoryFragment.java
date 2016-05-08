package ee.juhan.meetingorganizer.fragments;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;

public class HistoryFragment extends MeetingsListFragment {

	public static final String PAST_MEETINGS = "past-meetings";

	public HistoryFragment() {
		super("History", PAST_MEETINGS);
	}

	@Override
	public void checkDrawerItem() {
		((MainActivity) getActivity()).checkDrawerItem(R.id.nav_history);
	}
}
