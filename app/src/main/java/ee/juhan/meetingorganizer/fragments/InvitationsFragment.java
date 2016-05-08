package ee.juhan.meetingorganizer.fragments;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;

public class InvitationsFragment extends MeetingsListFragment {

	public static final String INVITATIONS = "invitations";

	public InvitationsFragment() {
		super("Invitations", INVITATIONS);
	}

	@Override
	public void checkDrawerItem() {
		((MainActivity) getActivity()).checkDrawerItem(R.id.nav_invitations);
	}
}
